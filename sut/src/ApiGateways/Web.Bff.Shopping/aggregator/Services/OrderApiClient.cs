namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Services;

public class OrderApiClient : IOrderApiClient
{
    private readonly HttpClient _apiClient;
    private readonly UrlsConfig _urls;

    public OrderApiClient(HttpClient httpClient, IOptions<UrlsConfig> config)
    {
        _apiClient = httpClient;
        _urls = config.Value;
    }

    public async Task<OrderData> GetOrderDraftFromBasketAsync(BasketData basket)
    {
        var url = $"{_urls.Orders}{UrlsConfig.OrdersOperations.GetOrderDraft}";
        var content = new StringContent(JsonSerializer.Serialize(basket), System.Text.Encoding.UTF8, "application/json");
        var response = await _apiClient.PostAsync(url, content);

        response.EnsureSuccessStatusCode();

        var ordersDraftResponse = await response.Content.ReadAsStringAsync();

        return JsonSerializer.Deserialize<OrderData>(ordersDraftResponse, JsonDefaults.CaseInsensitiveOptions);
    }
}
