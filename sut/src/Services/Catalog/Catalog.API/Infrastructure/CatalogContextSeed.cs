namespace Microsoft.eShopOnContainers.Services.Catalog.API.Infrastructure;

public class CatalogContextSeed
{
    private const string ErrorReadingCsvHeaders = "Error reading CSV headers";
    private const string SetupFolder = "Setup";
    private static readonly Regex s_csvSplitRegex = new(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", RegexOptions.Compiled, TimeSpan.FromSeconds(1)); // NOSONAR SYSLIB1045

    protected CatalogContextSeed() { }

    public static async Task SeedAsync(CatalogContext context, IWebHostEnvironment env, IOptions<CatalogSettings> settings, ILogger<CatalogContextSeed> logger)
    {
        var policy = CreatePolicy(logger, nameof(CatalogContextSeed));

        await policy.ExecuteAsync(async () =>
        {
            var useCustomizationData = settings.Value.UseCustomizationData;
            var contentRootPath = env.ContentRootPath;
            var picturePath = env.WebRootPath;

            if (!await context.CatalogBrands.AnyAsync())
            {
                await context.CatalogBrands.AddRangeAsync(useCustomizationData
                    ? GetCatalogBrandsFromFile(contentRootPath, logger)
                    : GetPreconfiguredCatalogBrands());

                await context.SaveChangesAsync();
            }

            if (!await context.CatalogTypes.AnyAsync())
            {
                await context.CatalogTypes.AddRangeAsync(useCustomizationData
                    ? GetCatalogTypesFromFile(contentRootPath, logger)
                    : GetPreconfiguredCatalogTypes());

                await context.SaveChangesAsync();
            }

            if (!await context.CatalogItems.AnyAsync())
            {
                await context.CatalogItems.AddRangeAsync(useCustomizationData
                    ? GetCatalogItemsFromFile(contentRootPath, context, logger)
                    : GetPreconfiguredItems());

                await context.SaveChangesAsync();

                GetCatalogItemPictures(contentRootPath, picturePath);
            }
        });
    }

    private static IEnumerable<CatalogBrand> GetCatalogBrandsFromFile(string contentRootPath, ILogger<CatalogContextSeed> logger)
    {
        string csvFileCatalogBrands = Path.Combine(contentRootPath, SetupFolder, "CatalogBrands.csv");

        if (!File.Exists(csvFileCatalogBrands))
        {
            return GetPreconfiguredCatalogBrands();
        }

        try
        {
            string[] requiredHeaders = { "catalogbrand" };
            GetHeaders(csvFileCatalogBrands, requiredHeaders);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, ErrorReadingCsvHeaders);
            return GetPreconfiguredCatalogBrands();
        }

        return File.ReadAllLines(csvFileCatalogBrands)
                                    .Skip(1) // skip header row
                                    .SelectTry(CreateCatalogBrand)
                                    .OnCaughtException(ex => { logger.LogError(ex, "Error creating brand while seeding database"); return null; })
                                    .Where(x => x != null);
    }

    private static CatalogBrand CreateCatalogBrand(string brand)
    {
        brand = brand.Trim('"').Trim();

        if (string.IsNullOrEmpty(brand))
        {
            throw new InvalidOperationException("Catalog Brand Name is empty");
        }

        return new CatalogBrand
        {
            Brand = brand,
        };
    }

    private static List<CatalogBrand> GetPreconfiguredCatalogBrands()
    {
        return new List<CatalogBrand>()
        {
            new() { Brand = "Azure"},
            new() { Brand = ".NET" },
            new() { Brand = "Visual Studio" },
            new() { Brand = "SQL Server" },
            new() { Brand = "Other" }
        };
    }

    private static IEnumerable<CatalogType> GetCatalogTypesFromFile(string contentRootPath, ILogger<CatalogContextSeed> logger)
    {
        string csvFileCatalogTypes = Path.Combine(contentRootPath, SetupFolder, "CatalogTypes.csv");

        if (!File.Exists(csvFileCatalogTypes))
        {
            return GetPreconfiguredCatalogTypes();
        }

        try
        {
            string[] requiredHeaders = { "catalogtype" };
            GetHeaders(csvFileCatalogTypes, requiredHeaders);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, ErrorReadingCsvHeaders);
            return GetPreconfiguredCatalogTypes();
        }

        return File.ReadAllLines(csvFileCatalogTypes)
                                    .Skip(1) // skip header row
                                    .SelectTry(x => CreateCatalogType(x))
                                    .OnCaughtException(ex => { logger.LogError(ex, "Error creating catalog type while seeding database"); return null; })
                                    .Where(x => x != null);
    }

    private static CatalogType CreateCatalogType(string type)
    {
        type = type.Trim('"').Trim();

        if (string.IsNullOrEmpty(type))
        {
            throw new InvalidOperationException("catalog Type Name is empty");
        }

        return new CatalogType
        {
            Type = type,
        };
    }

    private static List<CatalogType> GetPreconfiguredCatalogTypes()
    {
        return new List<CatalogType>()
        {
            new() { Type = "Mug"},
            new() { Type = "T-Shirt" },
            new() { Type = "Sheet" },
            new() { Type = "USB Memory Stick" }
        };
    }

    private static IEnumerable<CatalogItem> GetCatalogItemsFromFile(string contentRootPath, CatalogContext context, ILogger<CatalogContextSeed> logger)
    {
        string csvFileCatalogItems = Path.Combine(contentRootPath, SetupFolder, "CatalogItems.csv");

        if (!File.Exists(csvFileCatalogItems))
        {
            return GetPreconfiguredItems();
        }

        string[] csvheaders;
        try
        {
            string[] requiredHeaders = { "catalogtypename", "catalogbrandname", "description", "name", "price", "picturefilename" };
            string[] optionalheaders = { "availablestock", "restockthreshold", "maxstockthreshold", "onreorder" };
            csvheaders = GetHeaders(csvFileCatalogItems, requiredHeaders, optionalheaders);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, ErrorReadingCsvHeaders);
            return GetPreconfiguredItems();
        }

        var catalogTypeIdLookup = context.CatalogTypes.ToDictionary(ct => ct.Type, ct => ct.Id);
        var catalogBrandIdLookup = context.CatalogBrands.ToDictionary(ct => ct.Brand, ct => ct.Id);

        return File.ReadAllLines(csvFileCatalogItems)
                    .Skip(1) // skip header row
                    .Select(row => s_csvSplitRegex.Split(row))
                    .SelectTry(column => CreateCatalogItem(column, csvheaders, catalogTypeIdLookup, catalogBrandIdLookup))
                    .OnCaughtException(ex => { logger.LogError(ex, "Error creating catalog item while seeding database"); return null; })
                    .Where(x => x != null);
    }

    private static CatalogItem CreateCatalogItem(string[] column, string[] headers, Dictionary<string, int> catalogTypeIdLookup, Dictionary<string, int> catalogBrandIdLookup) // NOSONAR S3776
    {
        if (column.Length != headers.Length)
        {
            throw new InvalidOperationException($"column count '{column.Length}' not the same as headers count'{headers.Length}'");
        }

        string catalogTypeName = column[Array.IndexOf(headers, "catalogtypename")].Trim('"').Trim();
        if (!catalogTypeIdLookup.TryGetValue(catalogTypeName, out int catalogTypeId))
        {
            throw new InvalidOperationException($"type={catalogTypeName} does not exist in catalogTypes");
        }

        string catalogBrandName = column[Array.IndexOf(headers, "catalogbrandname")].Trim('"').Trim();
        if (!catalogBrandIdLookup.TryGetValue(catalogBrandName, out int catalogBrandId))
        {
            throw new InvalidOperationException($"type={catalogBrandName} does not exist in catalogTypes");
        }

        string priceString = column[Array.IndexOf(headers, "price")].Trim('"').Trim();
        if (!decimal.TryParse(priceString, NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture, out decimal price))
        {
            throw new InvalidOperationException($"price={priceString}is not a valid decimal number");
        }

        var catalogItem = new CatalogItem()
        {
            CatalogTypeId = catalogTypeId,
            CatalogBrandId = catalogBrandId,
            Description = column[Array.IndexOf(headers, "description")].Trim('"').Trim(),
            Name = column[Array.IndexOf(headers, "name")].Trim('"').Trim(),
            Price = price,
            PictureFileName = column[Array.IndexOf(headers, "picturefilename")].Trim('"').Trim(),
        };

        int availableStockIndex = Array.IndexOf(headers, "availablestock");
        if (availableStockIndex != -1)
        {
            string availableStockString = column[availableStockIndex].Trim('"').Trim();
            if (!string.IsNullOrEmpty(availableStockString))
            {
                if (int.TryParse(availableStockString, out int availableStock))
                {
                    catalogItem.AvailableStock = availableStock;
                }
                else
                {
                    throw new InvalidOperationException($"availableStock={availableStockString} is not a valid integer");
                }
            }
        }

        int restockThresholdIndex = Array.IndexOf(headers, "restockthreshold");
        if (restockThresholdIndex != -1)
        {
            string restockThresholdString = column[restockThresholdIndex].Trim('"').Trim();
            if (!string.IsNullOrEmpty(restockThresholdString))
            {
                if (int.TryParse(restockThresholdString, out int restockThreshold))
                {
                    catalogItem.RestockThreshold = restockThreshold;
                }
                else
                {
                    throw new InvalidOperationException($"restockThreshold={restockThreshold} is not a valid integer");
                }
            }
        }

        int maxStockThresholdIndex = Array.IndexOf(headers, "maxstockthreshold");
        if (maxStockThresholdIndex != -1)
        {
            string maxStockThresholdString = column[maxStockThresholdIndex].Trim('"').Trim();
            if (!string.IsNullOrEmpty(maxStockThresholdString))
            {
                if (int.TryParse(maxStockThresholdString, out int maxStockThreshold))
                {
                    catalogItem.MaxStockThreshold = maxStockThreshold;
                }
                else
                {
                    throw new InvalidOperationException($"maxStockThreshold={maxStockThreshold} is not a valid integer");
                }
            }
        }

        int onReorderIndex = Array.IndexOf(headers, "onreorder");
        if (onReorderIndex != -1)
        {
            string onReorderString = column[onReorderIndex].Trim('"').Trim();
            if (!string.IsNullOrEmpty(onReorderString))
            {
                if (bool.TryParse(onReorderString, out bool onReorder))
                {
                    catalogItem.OnReorder = onReorder;
                }
                else
                {
                    throw new InvalidOperationException($"onReorder={onReorderString} is not a valid boolean");
                }
            }
        }

        return catalogItem;
    }

    private static List<CatalogItem> GetPreconfiguredItems()
    {
        return new List<CatalogItem>()
        {
            new() { CatalogTypeId = 2, CatalogBrandId = 2, AvailableStock = 100, Description = ".NET Bot Black Hoodie", Name = ".NET Bot Black Hoodie", Price = 19.5M, PictureFileName = "1.png" },
            new() { CatalogTypeId = 1, CatalogBrandId = 2, AvailableStock = 100, Description = ".NET Black & White Mug", Name = ".NET Black & White Mug", Price= 8.50M, PictureFileName = "2.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 5, AvailableStock = 100, Description = "Prism White T-Shirt", Name = "Prism White T-Shirt", Price = 12, PictureFileName = "3.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 2, AvailableStock = 100, Description = ".NET Foundation T-shirt", Name = ".NET Foundation T-shirt", Price = 12, PictureFileName = "4.png" },
            new() { CatalogTypeId = 3, CatalogBrandId = 5, AvailableStock = 100, Description = "Roslyn Red Sheet", Name = "Roslyn Red Sheet", Price = 8.5M, PictureFileName = "5.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 2, AvailableStock = 100, Description = ".NET Blue Hoodie", Name = ".NET Blue Hoodie", Price = 12, PictureFileName = "6.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 5, AvailableStock = 100, Description = "Roslyn Red T-Shirt", Name = "Roslyn Red T-Shirt", Price = 12, PictureFileName = "7.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 5, AvailableStock = 100, Description = "Kudu Purple Hoodie", Name = "Kudu Purple Hoodie", Price = 8.5M, PictureFileName = "8.png" },
            new() { CatalogTypeId = 1, CatalogBrandId = 5, AvailableStock = 100, Description = "Cup<T> White Mug", Name = "Cup<T> White Mug", Price = 12, PictureFileName = "9.png" },
            new() { CatalogTypeId = 3, CatalogBrandId = 2, AvailableStock = 100, Description = ".NET Foundation Sheet", Name = ".NET Foundation Sheet", Price = 12, PictureFileName = "10.png" },
            new() { CatalogTypeId = 3, CatalogBrandId = 2, AvailableStock = 100, Description = "Cup<T> Sheet", Name = "Cup<T> Sheet", Price = 8.5M, PictureFileName = "11.png" },
            new() { CatalogTypeId = 2, CatalogBrandId = 5, AvailableStock = 100, Description = "Prism White TShirt", Name = "Prism White TShirt", Price = 12, PictureFileName = "12.png" },
        };
    }

    private static string[] GetHeaders(string csvfile, string[] requiredHeaders, string[] optionalHeaders = null)
    {
        string[] csvheaders = File.ReadLines(csvfile).First().ToLowerInvariant().Split(',');

        if (csvheaders.Length < requiredHeaders.Length)
        {
            throw new InvalidOperationException($"requiredHeader count '{requiredHeaders.Length}' is bigger then csv header count '{csvheaders.Length}' ");
        }

        if (optionalHeaders != null && csvheaders.Length > (requiredHeaders.Length + optionalHeaders.Length))
        {
            throw new InvalidOperationException($"csv header count '{csvheaders.Length}'  is larger then required '{requiredHeaders.Length}' and optional '{optionalHeaders.Length}' headers count");
        }

        var missingHeader = requiredHeaders.FirstOrDefault(h => !csvheaders.Contains(h));
        if (missingHeader != null)
            throw new InvalidOperationException($"does not contain required header '{missingHeader}'");

        return csvheaders;
    }

    private static void GetCatalogItemPictures(string contentRootPath, string picturePath)
    {
        if (picturePath != null)
        {
            DirectoryInfo directory = new DirectoryInfo(picturePath);
            foreach (FileInfo file in directory.GetFiles())
            {
                file.Delete();
            }

            string zipFileCatalogItemPictures = Path.Combine(contentRootPath, SetupFolder, "CatalogItems.zip");
            ZipFile.ExtractToDirectory(zipFileCatalogItemPictures, picturePath);
        }
    }

    private static AsyncRetryPolicy CreatePolicy(ILogger<CatalogContextSeed> logger, string prefix, int retries = 3)
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
