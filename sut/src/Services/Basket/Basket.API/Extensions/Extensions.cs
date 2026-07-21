namespace Microsoft.eShopOnContainers.Services.Basket.API.Extensions;

public static class Extensions
{
    private const string RedisConnectionName = "redis";
    private static readonly string[] s_readyTags = ["ready", "liveness"];

    public static IServiceCollection AddHealthChecks(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddHealthChecks()
            .AddRedis(_ => configuration.GetRequiredConnectionString(RedisConnectionName), RedisConnectionName, tags: s_readyTags);

        return services;
    }

    public static IServiceCollection AddRedis(this IServiceCollection services, IConfiguration configuration)
    {
        return services.AddSingleton(sp =>
        {
            var redisConfig = ConfigurationOptions.Parse(configuration.GetRequiredConnectionString(RedisConnectionName), true);

            return ConnectionMultiplexer.Connect(redisConfig);
        });
    }
}
