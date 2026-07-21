namespace Microsoft.eShopOnContainers.WebMVC.Services;

using Microsoft.eShopOnContainers.WebMVC.ViewModels;

public class BasketService : IBasketService
{
    private readonly HttpClient _apiClient;
    private readonly ILogger<BasketService> _logger;
    private const string ApplicationJsonMediaType = "application/json";
    private readonly string _basketByPassUrl;
    private readonly string _purchaseUrl;

    public BasketService(HttpClient httpClient, IOptions<AppSettings> settings, ILogger<BasketService> logger)
    {
        _apiClient = httpClient;
        _logger = logger;

        _basketByPassUrl = $"{settings.Value.PurchaseUrl}/b/api/v1/basket";
        _purchaseUrl = $"{settings.Value.PurchaseUrl}/api/v1";
    }

    public async Task<Basket> GetBasket(ApplicationUser user)
    {
        var uri = Api.Basket.GetBasket(_basketByPassUrl, user.Id);
        if (_logger.IsEnabled(LogLevel.Debug)) _logger.LogDebug("[GetBasket] -> Calling {Uri} to get the basket", uri);
        var response = await _apiClient.GetAsync(uri);
        if (_logger.IsEnabled(LogLevel.Debug)) _logger.LogDebug("[GetBasket] -> response code {StatusCode}", response.StatusCode);
        var responseString = await response.Content.ReadAsStringAsync();
        return string.IsNullOrEmpty(responseString) ?
            new Basket() { BuyerId = user.Id } :
            JsonSerializer.Deserialize<Basket>(responseString, JsonDefaults.CaseInsensitiveOptions);
    }

    public async Task<Basket> UpdateBasket(Basket basket)
    {
        var uri = Api.Basket.UpdateBasket(_basketByPassUrl);

        var basketContent = new StringContent(JsonSerializer.Serialize(basket), Encoding.UTF8, ApplicationJsonMediaType);

        var response = await _apiClient.PostAsync(uri, basketContent);

        response.EnsureSuccessStatusCode();

        return basket;
    }

    public async Task Checkout(BasketDto basket)
    {
        var uri = Api.Basket.CheckoutBasket(_basketByPassUrl);
        var basketContent = new StringContent(JsonSerializer.Serialize(basket), Encoding.UTF8, ApplicationJsonMediaType);

        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Uri checkout {Uri}", uri);

        var response = await _apiClient.PostAsync(uri, basketContent);

        response.EnsureSuccessStatusCode();
    }

    public async Task<Basket> SetQuantities(ApplicationUser user, Dictionary<string, int> quantities)
    {
        var uri = Api.Purchase.UpdateBasketItem(_purchaseUrl);

        var basketUpdate = new
        {
            BasketId = user.Id,
            Updates = quantities.Select(kvp => new
            {
                BasketItemId = kvp.Key,
                NewQty = kvp.Value
            }).ToArray()
        };

        var basketContent = new StringContent(JsonSerializer.Serialize(basketUpdate), Encoding.UTF8, ApplicationJsonMediaType);

        var response = await _apiClient.PutAsync(uri, basketContent);

        response.EnsureSuccessStatusCode();

        var jsonResponse = await response.Content.ReadAsStringAsync();

        return JsonSerializer.Deserialize<Basket>(jsonResponse, JsonDefaults.CaseInsensitiveOptions);
    }

    public async Task<Order> GetOrderDraft(string basketId)
    {
        var uri = Api.Purchase.GetOrderDraft(_purchaseUrl, basketId);

        var responseString = await _apiClient.GetStringAsync(uri);

        var response = JsonSerializer.Deserialize<Order>(responseString, JsonDefaults.CaseInsensitiveOptions);

        return response;
    }

    public async Task AddItemToBasket(ApplicationUser user, int productId)
    {
        var uri = Api.Purchase.AddItemToBasket(_purchaseUrl);

        var newItem = new
        {
            CatalogItemId = productId,
            BasketId = user.Id,
            Quantity = 1
        };

        var basketContent = new StringContent(JsonSerializer.Serialize(newItem), Encoding.UTF8, ApplicationJsonMediaType);

        await _apiClient.PostAsync(uri, basketContent);
    }
}
