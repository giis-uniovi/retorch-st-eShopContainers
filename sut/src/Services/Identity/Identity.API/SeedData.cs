namespace Microsoft.eShopOnContainers.Services.Identity.API;

public static class SeedData
{
    public static async Task EnsureSeedData(IServiceScope scope, IConfiguration configuration, ILogger logger)
    {
        var retryPolicy = CreateRetryPolicy(configuration, logger);
        var context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();

        await retryPolicy.ExecuteAsync(async () =>
        {
        //RETORCH: Increasing the timeout to avoid flakys when its parallelized the system. Added ConfigureAwait(false)
            context.Database.SetCommandTimeout(150);
            await context.Database.MigrateAsync().ConfigureAwait(false);

            var userMgr = scope.ServiceProvider.GetRequiredService<UserManager<ApplicationUser>>();
            var alice = await userMgr.FindByNameAsync("alice");

            if (alice == null)
            {
                alice = new ApplicationUser
                {
                    UserName = "alice",
                    Email = "AliceSmith@email.com",
                    EmailConfirmed = true,
                    CardHolderName = "Alice Smith",
                    CardNumber = "4012888888881881",
                    CardType = 1,
                    City = "Redmond",
                    Country = "U.S.",
                    Expiration = "12/24",
                    Id = Guid.NewGuid().ToString(),
                    LastName = "Smith",
                    Name = "Alice",
                    PhoneNumber = "1234567890",
                    ZipCode = "98052",
                    State = "WA",
                    Street = "15703 NE 61st Ct",
                    SecurityNumber = "123"
                };

                var result = await userMgr.CreateAsync(alice, "Pass123$");

                if (!result.Succeeded)
                {
                    throw new InvalidOperationException(result.Errors.First().Description);
                }

                logger.LogDebug("alice created");
            }
            else
            {
                logger.LogDebug("alice already exists");
            }

            var bob = await userMgr.FindByNameAsync("bob");

            if (bob == null)
            {
                bob = new ApplicationUser
                {
                    UserName = "bob",
                    Email = "BobSmith@email.com",
                    EmailConfirmed = true,
                    CardHolderName = "Bob Smith",
                    CardNumber = "4012888888881881",
                    CardType = 1,
                    City = "Redmond",
                    Country = "U.S.",
                    Expiration = "12/24",
                    Id = Guid.NewGuid().ToString(),
                    LastName = "Smith",
                    Name = "Bob",
                    PhoneNumber = "1234567890",
                    ZipCode = "98052",
                    State = "WA",
                    Street = "15703 NE 61st Ct",
                    SecurityNumber = "456"
                };

                var result = await userMgr.CreateAsync(bob, "Pass123$");

                if (!result.Succeeded)
                {
                    throw new InvalidOperationException(result.Errors.First().Description);
                }

                logger.LogDebug("bob created");
            }
            else
            {
                logger.LogDebug("bob already exists");
            }
        });
    }

    private static AsyncPolicy CreateRetryPolicy(IConfiguration configuration, Microsoft.Extensions.Logging.ILogger logger)
    {
        var retryMigrations = configuration.GetValue<bool>("RetryMigrations");

        // Only use a retry policy if configured to do so.
        // When running in an orchestrator/K8s, it will take care of restarting failed services.
        if (retryMigrations)
        {
            return Policy.Handle<Exception>().
                WaitAndRetryForeverAsync(
                    sleepDurationProvider: retry => TimeSpan.FromSeconds(5),
                    onRetry: (exception, retry, timeSpan) => logger.LogWarning(exception, "Error migrating database (retry attempt {Retry})", retry));
        }

        return Policy.NoOpAsync();
    }
}
