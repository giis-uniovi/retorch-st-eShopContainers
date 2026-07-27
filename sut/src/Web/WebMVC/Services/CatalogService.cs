namespace Microsoft.eShopOnContainers.WebMVC.Services;

public class CatalogService : ICatalogService
{
    private readonly HttpClient _httpClient;
    private readonly string _remoteServiceBaseUrl;

    public CatalogService(HttpClient httpClient, IOptions<AppSettings> settings)
    {
        _httpClient = httpClient;
        _remoteServiceBaseUrl = $"{settings.Value.PurchaseUrl}/c/api/v1/catalog/";
    }

    public async Task<Catalog> GetCatalogItems(int page, int take, int? brand, int? type)
    {
        var uri = Api.Catalog.GetAllCatalogItems(_remoteServiceBaseUrl, page, take, brand, type);

        var responseString = await _httpClient.GetStringAsync(uri);

        var catalog = JsonSerializer.Deserialize<Catalog>(responseString, JsonDefaults.CaseInsensitiveOptions);

        return catalog;
    }

    public async Task<IEnumerable<SelectListItem>> GetBrands()
    {
        var uri = Api.Catalog.GetAllBrands(_remoteServiceBaseUrl);

        var responseString = await _httpClient.GetStringAsync(uri);

        var items = new List<SelectListItem>();

        items.Add(new SelectListItem() { Value = null, Text = "All", Selected = true });

        using var brands = JsonDocument.Parse(responseString);

        foreach (JsonElement brand in brands.RootElement.EnumerateArray())
        {
            items.Add(new SelectListItem()
            {
                Value = brand.GetProperty("id").ToString(),
                Text = brand.GetProperty("brand").ToString()
            });
        }

        return items;
    }

    public async Task<IEnumerable<SelectListItem>> GetTypes()
    {
        var uri = Api.Catalog.GetAllTypes(_remoteServiceBaseUrl);

        var responseString = await _httpClient.GetStringAsync(uri);

        var items = new List<SelectListItem>();
        items.Add(new SelectListItem() { Value = null, Text = "All", Selected = true });

        using var catalogTypes = JsonDocument.Parse(responseString);

        foreach (JsonElement catalogType in catalogTypes.RootElement.EnumerateArray())
        {
            items.Add(new SelectListItem()
            {
                Value = catalogType.GetProperty("id").ToString(),
                Text = catalogType.GetProperty("type").ToString()
            });
        }

        return items;
    }
}
