namespace Basket.FunctionalTests;

public class BasketScenarios :
    BasketScenarioBase
{
    private const string JsonMediaType = "application/json";

    [Fact]
    public async Task Post_basket_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var content = new StringContent(BuildBasket(), UTF8Encoding.UTF8, JsonMediaType);
        var uri = "/api/v1/basket/";
        var response = await server.CreateClient().PostAsync(uri, content);
        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_basket_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.GetBasket(1));
        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Send_Checkout_basket_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var contentBasket = new StringContent(BuildBasket(), UTF8Encoding.UTF8, JsonMediaType);

        await server.CreateClient()
            .PostAsync(Post.Basket, contentBasket);

        var contentCheckout = new StringContent(BuildCheckout(), UTF8Encoding.UTF8, JsonMediaType)
        {
             Headers = { { "x-requestid", Guid.NewGuid().ToString() } }
        };

        var response = await server.CreateClient()
            .PostAsync(Post.CheckoutOrder, contentCheckout);

        Assert.True(response.IsSuccessStatusCode);
    }

    static string BuildBasket()
    {
        var order = new CustomerBasket(AutoAuthorizeMiddleware.IDENTITY_ID);

        order.Items.Add(new BasketItem
        {
            ProductId = 1,
            ProductName = ".NET Bot Black Hoodie",
            UnitPrice = 10,
            Quantity = 1
        });

        return JsonSerializer.Serialize(order);
    }

    static string BuildCheckout()
    {
        var checkoutBasket = new
        {
            City = "city",
            Street = "street",
            State = "state",
            Country = "coutry",
            ZipCode = "zipcode",
            CardNumber = "1234567890123456",
            CardHolderName = "CardHolderName",
            CardExpiration = DateTime.UtcNow.AddDays(1),
            CardSecurityNumber = "123",
            CardTypeId = 1,
            Buyer = "Buyer",
            RequestId = Guid.NewGuid()
        };

        return JsonSerializer.Serialize(checkoutBasket);
    }
}
