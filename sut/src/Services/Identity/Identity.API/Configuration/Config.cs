namespace Microsoft.eShopOnContainers.Services.Identity.API.Configuration
{
    public static class Config
    {
        private const string OrdersScope = "orders";
        private const string BasketScope = "basket";
        private const string WebShoppingAggScope = "webshoppingagg";
        private const string MobileShoppingAggScope = "mobileshoppingagg";
        private const string WebhooksScope = "webhooks";
        private const string OrdersSignalrHubScope = "orders.signalrhub";
        private const string MvcClientKey = "MvcClient";
        private const string SpaClientKey = "SpaClient";
        private const string WebhooksWebClientKey = "WebhooksWebClient";
        private const string SharedSecret = "secret";
        private const string SwaggerOAuth2RedirectSuffix = "/swagger/oauth2-redirect.html";
        private const string SwaggerSuffix = "/swagger/";

        // ApiResources define the apis in your system
        public static IEnumerable<ApiResource> GetApis()
        {
            return new List<ApiResource>
            {
                new ApiResource(OrdersScope, "Orders Service"),
                new ApiResource(BasketScope, "Basket Service"),
                new ApiResource(MobileShoppingAggScope, "Mobile Shopping Aggregator"),
                new ApiResource(WebShoppingAggScope, "Web Shopping Aggregator"),
                new ApiResource(OrdersSignalrHubScope, "Ordering Signalr Hub"),
                new ApiResource(WebhooksScope, "Webhooks registration Service"),
            };
        }

        // ApiScope is used to protect the API 
        //The effect is the same as that of API resources in IdentityServer 3.x
        public static IEnumerable<ApiScope> GetApiScopes()
        {
            return new List<ApiScope>
            {
                new ApiScope(OrdersScope, "Orders Service"),
                new ApiScope(BasketScope, "Basket Service"),
                new ApiScope(MobileShoppingAggScope, "Mobile Shopping Aggregator"),
                new ApiScope(WebShoppingAggScope, "Web Shopping Aggregator"),
                new ApiScope(OrdersSignalrHubScope, "Ordering Signalr Hub"),
                new ApiScope(WebhooksScope, "Webhooks registration Service"),
            };
        }

        // Identity resources are data like user ID, name, or email address of a user
        // see: http://docs.identityserver.io/en/release/configuration/resources.html
        public static IEnumerable<IdentityResource> GetResources()
        {
            return new List<IdentityResource>
            {
                new IdentityResources.OpenId(),
                new IdentityResources.Profile()
            };
        }

        // client want to access resources (aka scopes)
        public static IEnumerable<Client> GetClients(IConfiguration configuration)
        {
            return new List<Client>
            {
                // JavaScript Client
                new Client
                {
                    ClientId = "js",
                    ClientName = "eShop SPA OpenId Client",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,
                    RedirectUris =           { $"{configuration[SpaClientKey]}/" },
                    RequireConsent = false,
                    PostLogoutRedirectUris = { $"{configuration[SpaClientKey]}/" },
                    AllowedCorsOrigins =     { $"{configuration[SpaClientKey]}" },
                    AllowedScopes =
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        OrdersScope,
                        BasketScope,
                        WebShoppingAggScope,
                        OrdersSignalrHubScope,
                        WebhooksScope
                    },
                },
                new Client
                {
                    ClientId = "xamarin",
                    ClientName = "eShop Xamarin OpenId Client",
                    AllowedGrantTypes = GrantTypes.Hybrid,                    
                    //Used to retrieve the access token on the back channel.
                    ClientSecrets =
                    {
                        new Secret(SharedSecret.Sha256())
                    },
                    RedirectUris = { configuration["XamarinCallback"] },
                    RequireConsent = false,
                    RequirePkce = true,
                    PostLogoutRedirectUris = { $"{configuration["XamarinCallback"]}/Account/Redirecting" },
                    //AllowedCorsOrigins = { "http://eshopxamarin" },
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        OrdersScope,
                        BasketScope,
                        MobileShoppingAggScope,
                        WebhooksScope
                    },
                    //Allow requesting refresh tokens for long lived API access
                    AllowOfflineAccess = true,
                    AllowAccessTokensViaBrowser = true
                },
                new Client
                {
                    ClientId = "mvc",
                    ClientName = "MVC Client",
                    ClientSecrets = new List<Secret>
                    {

                        new Secret(SharedSecret.Sha256())
                    },
                    ClientUri = $"{configuration[MvcClientKey]}",                             // public uri of the client
                    AllowedGrantTypes = GrantTypes.Code,
                    AllowAccessTokensViaBrowser = false,
                    RequireConsent = false,
                    AllowOfflineAccess = true,
                    AlwaysIncludeUserClaimsInIdToken = true,
                    RequirePkce = false,
                    RedirectUris = new List<string>
                    {
                        $"{configuration[MvcClientKey]}/signin-oidc"
                    },
                    PostLogoutRedirectUris = new List<string>
                    {
                        $"{configuration[MvcClientKey]}/signout-callback-oidc"
                    },
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        OrdersScope,
                        BasketScope,
                        WebShoppingAggScope,
                        OrdersSignalrHubScope,
                        WebhooksScope
                    },
                    AccessTokenLifetime = 60*60*2, // 2 hours
                    IdentityTokenLifetime= 60*60*2 // 2 hours
                },
                new Client
                {
                    ClientId = "webhooksclient",
                    ClientName = "Webhooks Client",
                    ClientSecrets = new List<Secret>
                    {
                        new Secret(SharedSecret.Sha256())
                    },
                    ClientUri = $"{configuration[WebhooksWebClientKey]}",                             // public uri of the client
                    AllowedGrantTypes = GrantTypes.Code,
                    AllowAccessTokensViaBrowser = false,
                    RequireConsent = false,
                    AllowOfflineAccess = true,
                    AlwaysIncludeUserClaimsInIdToken = true,
                    RedirectUris = new List<string>
                    {
                        $"{configuration[WebhooksWebClientKey]}/signin-oidc"
                    },
                    PostLogoutRedirectUris = new List<string>
                    {
                        $"{configuration[WebhooksWebClientKey]}/signout-callback-oidc"
                    },
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        WebhooksScope
                    },
                    AccessTokenLifetime = 60*60*2, // 2 hours
                    IdentityTokenLifetime= 60*60*2 // 2 hours
                },
                new Client
                {
                    ClientId = "mvctest",
                    ClientName = "MVC Client Test",
                    ClientSecrets = new List<Secret>
                    {
                        new Secret(SharedSecret.Sha256())
                    },
                    ClientUri = $"{configuration["Mvc"]}",                             // public uri of the client
                    AllowedGrantTypes = GrantTypes.Code,
                    AllowAccessTokensViaBrowser = true,
                    RequireConsent = false,
                    AllowOfflineAccess = true,
                    RedirectUris = new List<string>
                    {
                        $"{configuration[MvcClientKey]}/signin-oidc"
                    },
                    PostLogoutRedirectUris = new List<string>
                    {
                        $"{configuration[MvcClientKey]}/signout-callback-oidc"
                    },
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        OrdersScope,
                        BasketScope,
                        WebShoppingAggScope,
                        WebhooksScope
                    },
                },
                new Client
                {
                    ClientId = "basketswaggerui",
                    ClientName = "Basket Swagger UI",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,

                    RedirectUris = { $"{configuration["BasketApiClient"]}{SwaggerOAuth2RedirectSuffix}" },
                    PostLogoutRedirectUris = { $"{configuration["BasketApiClient"]}{SwaggerSuffix}" },

                    AllowedScopes =
                    {
                        BasketScope
                    }
                },
                new Client
                {
                    ClientId = "orderingswaggerui",
                    ClientName = "Ordering Swagger UI",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,

                    RedirectUris = { $"{configuration["OrderingApiClient"]}{SwaggerOAuth2RedirectSuffix}" },
                    PostLogoutRedirectUris = { $"{configuration["OrderingApiClient"]}{SwaggerSuffix}" },

                    AllowedScopes =
                    {
                        OrdersScope
                    }
                },
                new Client
                {
                    ClientId = "mobileshoppingaggswaggerui",
                    ClientName = "Mobile Shopping Aggregattor Swagger UI",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,

                    RedirectUris = { $"{configuration["MobileShoppingAggClient"]}{SwaggerOAuth2RedirectSuffix}" },
                    PostLogoutRedirectUris = { $"{configuration["MobileShoppingAggClient"]}{SwaggerSuffix}" },

                    AllowedScopes =
                    {
                        MobileShoppingAggScope
                    }
                },
                new Client
                {
                    ClientId = "webshoppingaggswaggerui",
                    ClientName = "Web Shopping Aggregattor Swagger UI",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,

                    RedirectUris = { $"{configuration["WebShoppingAggClient"]}{SwaggerOAuth2RedirectSuffix}" },
                    PostLogoutRedirectUris = { $"{configuration["WebShoppingAggClient"]}{SwaggerSuffix}" },

                    AllowedScopes =
                    {
                        WebShoppingAggScope,
                        BasketScope
                    }
                },
                new Client
                {
                    ClientId = "webhooksswaggerui",
                    ClientName = "WebHooks Service Swagger UI",
                    AllowedGrantTypes = GrantTypes.Implicit,
                    AllowAccessTokensViaBrowser = true,

                    RedirectUris = { $"{configuration["WebhooksApiClient"]}{SwaggerOAuth2RedirectSuffix}" },
                    PostLogoutRedirectUris = { $"{configuration["WebhooksApiClient"]}{SwaggerSuffix}" },

                    AllowedScopes =
                    {
                        WebhooksScope
                    }
                },
                 new Client
                {
                    ClientId = "retorch",
                    ClientName = "MVC Client Test",
                    ClientSecrets = new List<Secret>
                    {
                        new Secret(SharedSecret.Sha256())
                    },                        
                    AllowedGrantTypes = GrantTypes.ClientCredentials,
                    AllowAccessTokensViaBrowser = true,
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        OrdersScope,
                        BasketScope,
                        WebShoppingAggScope,
                        WebhooksScope
                    },
                },
                new Client
                {
                    ClientId = "testalice",
                    ClientName = "MVC Alice Client Test",
                    ClientSecrets = new List<Secret>
                    {
                        new Secret(SharedSecret.Sha256())
                    },
                    AllowedGrantTypes = GrantTypes.ResourceOwnerPassword,
                    AllowAccessTokensViaBrowser = true,
                    AllowedScopes = new List<string>
                    {
                        IdentityServerConstants.StandardScopes.OpenId,
                        IdentityServerConstants.StandardScopes.Profile,
                        IdentityServerConstants.StandardScopes.OfflineAccess,
                        OrdersScope,
                        BasketScope,
                        WebShoppingAggScope,
                        WebhooksScope
                    },
                }

            };
        }
    }
}
