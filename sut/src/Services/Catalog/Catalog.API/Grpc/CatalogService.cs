using CatalogApi;
using static CatalogApi.Catalog;

namespace Microsoft.eShopOnContainers.Services.Catalog.API.Grpc;

public class CatalogService : CatalogBase
{
    private readonly CatalogContext _catalogContext;
    private readonly CatalogSettings _settings;
    private readonly ILogger<CatalogService> _logger;

    public CatalogService(CatalogContext dbContext, IOptions<CatalogSettings> settings, ILogger<CatalogService> logger)
    {
        _settings = settings.Value;
        _catalogContext = dbContext ?? throw new ArgumentNullException(nameof(dbContext));
        _logger = logger;
    }

    public override async Task<CatalogItemResponse> GetItemById(CatalogItemRequest request, ServerCallContext context)
    {
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Begin grpc call CatalogService.GetItemById for product id {Id}", request.Id);
        if (request.Id <= 0)
        {
            context.Status = new Status(StatusCode.FailedPrecondition, $"Id must be > 0 (received {request.Id})");
            return null;
        }

        var item = await _catalogContext.CatalogItems.SingleOrDefaultAsync(ci => ci.Id == request.Id);
        var baseUri = _settings.PicBaseUrl;
        var azureStorageEnabled = _settings.AzureStorageEnabled;
        item.FillProductUrl(baseUri, azureStorageEnabled: azureStorageEnabled);

        if (item != null)
        {
            return new CatalogItemResponse()
            {
                AvailableStock = item.AvailableStock,
                Description = item.Description,
                Id = item.Id,
                MaxStockThreshold = item.MaxStockThreshold,
                Name = item.Name,
                OnReorder = item.OnReorder,
                PictureFileName = item.PictureFileName,
                PictureUri = item.PictureUri,
                Price = (double)item.Price,
                RestockThreshold = item.RestockThreshold
            };
        }

        context.Status = new Status(StatusCode.NotFound, $"Product with id {request.Id} do not exist");
        return null;
    }

    public override async Task<PaginatedItemsResponse> GetItemsByIds(CatalogItemsRequest request, ServerCallContext context)
    {
        if (!string.IsNullOrEmpty(request.Ids))
        {
            var items = await _catalogContext.GetItemsByIdsAsync(request.Ids, _settings.PicBaseUrl, _settings.AzureStorageEnabled);

            context.Status = items.Count == 0 ?
                new Status(StatusCode.NotFound, $"ids value invalid. Must be comma-separated list of numbers") :
                new Status(StatusCode.OK, string.Empty);

            return MapToResponse(items);
        }

        var (totalItems, itemsOnPage) = await _catalogContext.GetAllItemsPagedAsync(request.PageIndex, request.PageSize, _settings.PicBaseUrl, _settings.AzureStorageEnabled);

        var model = MapToResponse(itemsOnPage, totalItems, request.PageIndex, request.PageSize);
        context.Status = new Status(StatusCode.OK, string.Empty);

        return model;
    }

    private static PaginatedItemsResponse MapToResponse(List<CatalogItem> items)
    {
        return MapToResponse(items, items.Count, 1, items.Count);
    }

    private static PaginatedItemsResponse MapToResponse(List<CatalogItem> items, long count, int pageIndex, int pageSize)
    {
        var result = new PaginatedItemsResponse()
        {
            Count = count,
            PageIndex = pageIndex,
            PageSize = pageSize,
        };

        items.ForEach(i =>
        {
            var brand = i.CatalogBrand == null
                        ? null
                        : new CatalogApi.CatalogBrand()
                        {
                            Id = i.CatalogBrand.Id,
                            Name = i.CatalogBrand.Brand,
                        };
            var catalogType = i.CatalogType == null
                                ? null
                                : new CatalogApi.CatalogType()
                                {
                                    Id = i.CatalogType.Id,
                                    Type = i.CatalogType.Type,
                                };

            result.Data.Add(new CatalogItemResponse()
            {
                AvailableStock = i.AvailableStock,
                Description = i.Description,
                Id = i.Id,
                MaxStockThreshold = i.MaxStockThreshold,
                Name = i.Name,
                OnReorder = i.OnReorder,
                PictureFileName = i.PictureFileName,
                PictureUri = i.PictureUri,
                RestockThreshold = i.RestockThreshold,
                CatalogBrand = brand,
                CatalogType = catalogType,
                Price = (double)i.Price,
            });
        });

        return result;
    }


}
