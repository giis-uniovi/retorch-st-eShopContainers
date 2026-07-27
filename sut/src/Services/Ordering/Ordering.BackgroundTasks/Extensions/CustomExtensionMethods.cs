namespace Ordering.BackgroundTasks.Extensions;

public static class CustomExtensionMethods
{
    private static readonly string[] LiveReadyTags = new string[] { "live", "ready" };

    public static IServiceCollection AddHealthChecks(this IServiceCollection services, IConfiguration configuration)
    {
        var hcBuilder = services.AddHealthChecks();

        hcBuilder.AddSqlServer(_ =>
                configuration.GetRequiredConnectionString("OrderingDB"),
                name: "OrderingTaskDB-check",
                tags: LiveReadyTags);

        return services;
    }

    public static IServiceCollection AddApplicationOptions(this IServiceCollection services, IConfiguration configuration)
    {
        return services.Configure<BackgroundTaskSettings>(configuration)
                .Configure<BackgroundTaskSettings>(o =>
        {
            o.ConnectionString = configuration.GetRequiredConnectionString("OrderingDB");
        });
    }
}
