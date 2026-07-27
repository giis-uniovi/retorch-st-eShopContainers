namespace Microsoft.eShopOnContainers.WebMVC.Services;

using Microsoft.eShopOnContainers.WebMVC.ViewModels;

public class OrderingService : IOrderingService
{
    private readonly HttpClient _httpClient;
    private readonly string _remoteServiceBaseUrl;

    public OrderingService(HttpClient httpClient, IOptions<AppSettings> settings)
    {
        _httpClient = httpClient;
        _remoteServiceBaseUrl = $"{settings.Value.PurchaseUrl}/o/api/v1/orders";
    }

    async public Task<Order> GetOrder(ApplicationUser user, string orderId)
    {
        var uri = Api.Order.GetOrder(_remoteServiceBaseUrl, orderId);

        var responseString = await _httpClient.GetStringAsync(uri);

        var response = JsonSerializer.Deserialize<Order>(responseString, JsonDefaults.CaseInsensitiveOptions);

        return response;
    }

    async public Task<List<Order>> GetMyOrders(ApplicationUser user)
    {
        var uri = Api.Order.GetAllMyOrders(_remoteServiceBaseUrl);

        var responseString = await _httpClient.GetStringAsync(uri);

        var response = JsonSerializer.Deserialize<List<Order>>(responseString, JsonDefaults.CaseInsensitiveOptions);

        return response;
    }



    async public Task CancelOrder(string orderId)
    {
        var order = new OrderDto()
        {
            OrderNumber = orderId
        };

        var uri = Api.Order.CancelOrder(_remoteServiceBaseUrl);
        var orderContent = new StringContent(JsonSerializer.Serialize(order), Encoding.UTF8, "application/json");

        var response = await _httpClient.PutAsync(uri, orderContent);

        if (response.StatusCode == System.Net.HttpStatusCode.InternalServerError)
        {
            throw new HttpRequestException("Error cancelling order, try later.");
        }

        if (!response.IsSuccessStatusCode)
        {
            var errorContent = await response.Content.ReadAsStringAsync();
            throw new HttpRequestException($"Error cancelling order ({response.StatusCode}): {errorContent}");
        }
    }

    async public Task ShipOrder(string orderId)
    {
        var order = new OrderDto()
        {
            OrderNumber = orderId
        };

        var uri = Api.Order.ShipOrder(_remoteServiceBaseUrl);
        var orderContent = new StringContent(JsonSerializer.Serialize(order), Encoding.UTF8, "application/json");

        var response = await _httpClient.PutAsync(uri, orderContent);

        if (response.StatusCode == System.Net.HttpStatusCode.InternalServerError)
        {
            throw new HttpRequestException("Error in ship order process, try later.");
        }

        response.EnsureSuccessStatusCode();
    }

    public void OverrideUserInfoIntoOrder(Order original, Order destination)
    {
        destination.City = original.City;
        destination.Street = original.Street;
        destination.State = original.State;
        destination.Country = original.Country;
        destination.ZipCode = original.ZipCode;

        destination.CardNumber = original.CardNumber;
        destination.CardHolderName = original.CardHolderName;
        destination.CardExpiration = original.CardExpiration;
        destination.CardSecurityNumber = original.CardSecurityNumber;
    }

    public Order MapUserInfoIntoOrder(ApplicationUser user, Order order)
    {
        order.City = user.City;
        order.Street = user.Street;
        order.State = user.State;
        order.Country = user.Country;
        order.ZipCode = user.ZipCode;

        order.CardNumber = user.CardNumber;
        order.CardHolderName = user.CardHolderName;
        order.CardExpiration = new DateTime(int.Parse("20" + user.Expiration.Split('/')[1]), int.Parse(user.Expiration.Split('/')[0]), 1, 0, 0, 0, DateTimeKind.Unspecified);
        order.CardSecurityNumber = user.SecurityNumber;

        return order;
    }

    public BasketDto MapOrderToBasket(Order order)
    {
        order.CardExpirationApiFormat();

        return new BasketDto()
        {
            City = order.City,
            Street = order.Street,
            State = order.State,
            Country = order.Country,
            ZipCode = order.ZipCode,
            CardNumber = order.CardNumber,
            CardHolderName = order.CardHolderName,
            CardExpiration = order.CardExpiration.GetValueOrDefault(),
            CardSecurityNumber = order.CardSecurityNumber,
            CardTypeId = 1,
            Buyer = order.Buyer,
            RequestId = order.RequestId.GetValueOrDefault()
        };
    }
}
