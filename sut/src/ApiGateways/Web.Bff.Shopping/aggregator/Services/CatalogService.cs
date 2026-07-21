namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Services;

public class CatalogService : ICatalogService
{
    private readonly Catalog.CatalogClient _client;
    private readonly ILogger<CatalogService> _logger;

    public CatalogService(Catalog.CatalogClient client, ILogger<CatalogService> logger)
    {
        _client = client;
        _logger = logger;
    }

    public async Task<CatalogItem> GetCatalogItemAsync(int id)
    {
        var request = new CatalogItemRequest { Id = id };
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("grpc request {@Request}", request);
        var response = await _client.GetItemByIdAsync(request);
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("grpc response {@Response}", response);
        return MapToCatalogItemResponse(response);

    }

    public async Task<IEnumerable<CatalogItem>> GetCatalogItemsAsync(IEnumerable<int> ids)
    {
        var request = new CatalogItemsRequest { Ids = string.Join(",", ids), PageIndex = 1, PageSize = 10 };
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("grpc request {@Request}", request);
        var response = await _client.GetItemsByIdsAsync(request);
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("grpc response {@Response}", response);
        return response.Data.Select(MapToCatalogItemResponse);

    }

    private static CatalogItem MapToCatalogItemResponse(CatalogItemResponse catalogItemResponse)
    {
        return new CatalogItem
        {
            Id = catalogItemResponse.Id,
            Name = catalogItemResponse.Name,
            PictureUri = catalogItemResponse.PictureUri,
            Price = (decimal)catalogItemResponse.Price
        };
    }
}
