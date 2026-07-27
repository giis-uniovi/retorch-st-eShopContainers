namespace Microsoft.eShopOnContainers.Services.Ordering.API.Infrastructure;

using Microsoft.eShopOnContainers.Services.Ordering.Domain.AggregatesModel.BuyerAggregate;

public class OrderingContextSeed
{
    protected OrderingContextSeed() { }

    public static async Task SeedAsync(OrderingContext context, IWebHostEnvironment env, IOptions<OrderingSettings> settings, ILogger<OrderingContextSeed> logger)
    {
        var policy = CreatePolicy(logger, nameof(OrderingContextSeed));

        await policy.ExecuteAsync(async () =>
        {

            var useCustomizationData = settings.Value
            .UseCustomizationData;

            var contentRootPath = env.ContentRootPath;


            using (context)
            {
                await context.Database.MigrateAsync();

                if (!await context.CardTypes.AnyAsync())
                {
                    context.CardTypes.AddRange(useCustomizationData
                                            ? GetCardTypesFromFile(contentRootPath, logger)
                                            : GetPredefinedCardTypes());

                    await context.SaveChangesAsync();
                }

                if (!await context.OrderStatus.AnyAsync())
                {
                    context.OrderStatus.AddRange(useCustomizationData
                                            ? GetOrderStatusFromFile(contentRootPath, logger)
                                            : GetPredefinedOrderStatus());
                }

                await context.SaveChangesAsync();
            }
        });
    }

    private static IEnumerable<CardType> GetCardTypesFromFile(string contentRootPath, ILogger<OrderingContextSeed> log)
    {
        string csvFileCardTypes = Path.Combine(contentRootPath, "Setup", "CardTypes.csv");

        if (!File.Exists(csvFileCardTypes))
        {
            return GetPredefinedCardTypes();
        }

        try
        {
            string[] requiredHeaders = { "CardType" };
            GetHeaders(requiredHeaders, csvFileCardTypes);
        }
        catch (Exception ex)
        {
            log.LogError(ex, "Error reading CSV headers");
            return GetPredefinedCardTypes();
        }

        int id = 1;
        return File.ReadAllLines(csvFileCardTypes)
                                    .Skip(1) // skip header column
                                    .SelectTry(x => CreateCardType(x, ref id))
                                    .OnCaughtException(ex => { log.LogError(ex, "Error creating card while seeding database"); return null; })
                                    .Where(x => x != null);
    }

    private static CardType CreateCardType(string value, ref int id)
    {
        if (string.IsNullOrEmpty(value))
        {
            throw new InvalidOperationException("Orderstatus is null or empty");
        }

        return new CardType(id++, value.Trim('"').Trim());
    }

    private static IEnumerable<CardType> GetPredefinedCardTypes()
    {
        return Enumeration.GetAll<CardType>();
    }

    private static IEnumerable<OrderStatus> GetOrderStatusFromFile(string contentRootPath, ILogger<OrderingContextSeed> log)
    {
        string csvFileOrderStatus = Path.Combine(contentRootPath, "Setup", "OrderStatus.csv");

        if (!File.Exists(csvFileOrderStatus))
        {
            return GetPredefinedOrderStatus();
        }

        try
        {
            string[] requiredHeaders = { "OrderStatus" };
            GetHeaders(requiredHeaders, csvFileOrderStatus);
        }
        catch (Exception ex)
        {
            log.LogError(ex, "Error reading CSV headers");
            return GetPredefinedOrderStatus();
        }

        int id = 1;
        return File.ReadAllLines(csvFileOrderStatus)
                                    .Skip(1) // skip header row
                                    .SelectTry(x => CreateOrderStatus(x, ref id))
                                    .OnCaughtException(ex => { log.LogError(ex, "Error creating order status while seeding database"); return null; })
                                    .Where(x => x != null);
    }

    private static OrderStatus CreateOrderStatus(string value, ref int id)
    {
        if (string.IsNullOrEmpty(value))
        {
            throw new InvalidOperationException("Orderstatus is null or empty");
        }

        return new OrderStatus(id++, value.Trim('"').Trim().ToLowerInvariant());
    }

    private static List<OrderStatus> GetPredefinedOrderStatus()
    {
        return new List<OrderStatus>()
        {
            OrderStatus.Submitted,
            OrderStatus.AwaitingValidation,
            OrderStatus.StockConfirmed,
            OrderStatus.Paid,
            OrderStatus.Shipped,
            OrderStatus.Cancelled
        };
    }

    private static void GetHeaders(string[] requiredHeaders, string csvfile)
    {
        string[] csvheaders = File.ReadLines(csvfile).First().ToLowerInvariant().Split(',');

        if (csvheaders.Length != requiredHeaders.Length)
        {
            throw new InvalidOperationException($"requiredHeader count '{requiredHeaders.Length}' is different then read header '{csvheaders.Length}'");
        }

        var missingHeader = requiredHeaders.FirstOrDefault(h => !csvheaders.Contains(h));
        if (missingHeader != null)
            throw new InvalidOperationException($"does not contain required header '{missingHeader}'");
    }


    private static AsyncRetryPolicy CreatePolicy(ILogger<OrderingContextSeed> logger, string prefix, int retries = 3)
    {
        return Policy.Handle<SqlException>().
            WaitAndRetryAsync(
                retryCount: retries,
                sleepDurationProvider: retry => TimeSpan.FromSeconds(5),
                onRetry: (exception, timeSpan, retry, ctx) =>
                {
                    logger.LogWarning(exception, "[{Prefix}] Error seeding database (attempt {Retry} of {Retries})", prefix, retry, retries);
                }
            );
    }
}
