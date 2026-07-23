namespace eShopOnContainers.WebSPA.Extensions;

internal static class Extensions
{
    private static readonly string[] IdentityApiTags = new string[] { "identityapi" };

    public static IServiceCollection AddHealthChecks(this IServiceCollection services, IConfiguration configuration)
    {
        var hcBuilder = services.AddHealthChecks();

        hcBuilder
            .AddUrlGroup(_ => new Uri(configuration["IdentityUrlHC"]), name: "identityapi-check", tags: IdentityApiTags);

        return services;
    }
}
