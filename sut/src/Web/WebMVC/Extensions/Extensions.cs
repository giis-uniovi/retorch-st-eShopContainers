// Fix samesite issue when running eShop from docker-compose locally as by default http protocol is being used
// Refer to https://github.com/dotnet-architecture/eShopOnContainers/issues/1391
using Yarp.ReverseProxy.Forwarder;

internal static class Extensions
{
    public static void AddHealthChecks(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddHealthChecks()
            .AddUrlGroup(_ => new Uri(configuration["IdentityUrlHC"]), name: "identityapi-check", tags: new string[] { "identityapi" });
    }

    public static void AddApplicationServices(this IServiceCollection services, IConfiguration configuration)
    {
        services.Configure<AppSettings>(configuration);

        if (configuration["DPConnectionString"] is string redisConnection)
        {
            services.AddDataProtection(opts =>
            {
                opts.ApplicationDiscriminator = "eshop.webmvc";
            })
            .PersistKeysToStackExchangeRedis(ConnectionMultiplexer.Connect(redisConnection), "DataProtection-Keys");
        }
    }

    // Adds all Http client services
    public static void AddHttpClientServices(this IServiceCollection services)
    {
        // Register delegating handlers
        services.AddTransient<HttpClientAuthorizationDelegatingHandler>()
                .AddTransient<HttpClientRequestIdDelegatingHandler>();

        // Add http client services
        services.AddHttpClient<IBasketService, BasketService>()
                .SetHandlerLifetime(TimeSpan.FromMinutes(5))  //Sample. Default lifetime is 2 minutes
                .AddHttpMessageHandler<HttpClientAuthorizationDelegatingHandler>();

        services.AddHttpClient<ICatalogService, CatalogService>();

        services.AddHttpClient<IOrderingService, OrderingService>()
                .AddHttpMessageHandler<HttpClientAuthorizationDelegatingHandler>()
                .AddHttpMessageHandler<HttpClientRequestIdDelegatingHandler>();

        // Add custom application services
        services.AddTransient<IIdentityParser<ApplicationUser>, IdentityParser>();
    }

    public static void AddAuthenticationServices(this IServiceCollection services, IConfiguration configuration)
    {
        JwtSecurityTokenHandler.DefaultInboundClaimTypeMap.Remove("sub");

        var identityUrl = configuration.GetRequiredValue("IdentityUrl");
        var callBackUrl = configuration.GetRequiredValue("CallBackUrl");
        var sessionCookieLifetime = configuration.GetValue("SessionCookieLifetimeMinutes", 60);

        // ExternalIdentityUrl rewrites the browser-facing OAuth redirect to a URL the browser
        // can actually reach. Needed for local Docker Desktop deployments where port mappings
        // only bind to 127.0.0.1 and IdentityUrl uses Docker-internal DNS (e.g. identity_api_local).
        // When not set it defaults to IdentityUrl so CI parallel deployments are unaffected.
        var externalIdentityUrl = configuration.GetValue<string>("ExternalIdentityUrl") ?? identityUrl;

        // Add Authentication services
        services.AddAuthentication(options =>
        {
            options.DefaultScheme = CookieAuthenticationDefaults.AuthenticationScheme;
            options.DefaultChallengeScheme = OpenIdConnectDefaults.AuthenticationScheme;
        })
        .AddCookie(options => options.ExpireTimeSpan = TimeSpan.FromMinutes(sessionCookieLifetime))
        .AddOpenIdConnect(options =>
        {
            options.SignInScheme = CookieAuthenticationDefaults.AuthenticationScheme;
            options.Authority = identityUrl;
            options.SignedOutRedirectUri = callBackUrl;
            options.ClientId = "mvc";
            options.ClientSecret = "secret";
            options.ResponseType = "code";
            options.ResponseMode = "query";
            options.SaveTokens = true;
            options.GetClaimsFromUserInfoEndpoint = true;
            options.RequireHttpsMetadata = false;
            options.Scope.Add("openid");
            options.Scope.Add("profile");
            options.Scope.Add("orders");
            options.Scope.Add("basket");
            options.Scope.Add("webshoppingagg");
            options.Scope.Add("orders.signalrhub");
            options.Scope.Add("webhooks");
            // Fix SameSite/Secure cookie issue when running over HTTP (docker-compose locally)
            // UseCookiePolicy middleware does not reliably intercept OIDC cookies in .NET 8+
            // SameSite=None without Secure is rejected by Chrome 100+, so use Lax instead
            options.CorrelationCookie.SameSite = SameSiteMode.Lax;
            options.CorrelationCookie.SecurePolicy = CookieSecurePolicy.None;
            options.NonceCookie.SameSite = SameSiteMode.Lax;
            options.NonceCookie.SecurePolicy = CookieSecurePolicy.None;
            // .NET 8+ removed preferred_username from the default ClaimActions, so the
            // _LoginPartial.cshtml check for this claim fails and hides the whole
            // authenticated section (username + basket icon)
            options.ClaimActions.MapUniqueJsonKey("preferred_username", "preferred_username");
            // In .NET 8+, the OIDC handler uses JsonWebTokenHandler which ignores
            // JwtSecurityTokenHandler.DefaultInboundClaimTypeMap. Without this, "sub" gets
            // remapped to ClaimTypes.NameIdentifier and IdentityParser.Parse() returns Id = ""
            options.MapInboundClaims = false;

            // When ExternalIdentityUrl differs from IdentityUrl, replace the internal Docker
            // DNS hostname in the browser-facing redirect with the localhost-accessible URL.
            // Token exchange and userinfo calls are server-side and keep using IdentityUrl.
            if (!string.Equals(identityUrl, externalIdentityUrl, StringComparison.OrdinalIgnoreCase))
            {
                options.Events.OnRedirectToIdentityProvider = ctx =>
                {
                    ctx.ProtocolMessage.IssuerAddress = ctx.ProtocolMessage.IssuerAddress
                        .Replace(identityUrl, externalIdentityUrl, StringComparison.OrdinalIgnoreCase);
                    return Task.CompletedTask;
                };
                options.Events.OnRedirectToIdentityProviderForSignOut = ctx =>
                {
                    ctx.ProtocolMessage.IssuerAddress = ctx.ProtocolMessage.IssuerAddress
                        .Replace(identityUrl, externalIdentityUrl, StringComparison.OrdinalIgnoreCase);
                    return Task.CompletedTask;
                };
            }
        });
    }

    public static IEndpointConventionBuilder MapForwardSignalR(this WebApplication app)
    {
        // Forward the SignalR traffic to the bff
        var destination = app.Configuration.GetRequiredValue("PurchaseUrl");
        var authTransformer = new BffAuthTransformer();
        var requestConfig = new ForwarderRequestConfig();

        return app.MapForwarder("/hub/notificationhub/{**any}", destination, requestConfig, authTransformer);
    }

    private sealed class BffAuthTransformer : HttpTransformer
    {
        public override async ValueTask TransformRequestAsync(HttpContext httpContext, HttpRequestMessage proxyRequest, string destinationPrefix, CancellationToken cancellationToken)
        {
            // Set the access token as a bearer token for the outgoing request
            var accessToken = await httpContext.GetTokenAsync("access_token");

            if (accessToken is not null)
            {
                proxyRequest.Headers.Authorization = new("Bearer", accessToken);
            }

            await base.TransformRequestAsync(httpContext, proxyRequest, destinationPrefix, cancellationToken);
        }
    }
}
