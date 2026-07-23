using Basket.FunctionalTests.Base;
using Ordering.FunctionalTests;

namespace FunctionalTests.Services.Ordering;

public class OrderingScenarios
{
    private static readonly JsonSerializerOptions s_jsonOptions = new() { PropertyNameCaseInsensitive = true };
    private const string JsonMediaType = "application/json";
    private const string RequestIdHeader = "x-requestid";

    [Fact]
    public async Task Cancel_basket_and_check_order_status_cancelled()
    {
        using var orderServer = OrderingScenarioBase.CreateServer();
        using var basketServer = BasketScenarioBase.CreateServer();
        // Expected data
        var cityExpected = $"city-{Guid.NewGuid()}";
        var orderStatusExpected = "cancelled";

        var basketClient = basketServer.CreateClient();
        var orderClient = orderServer.CreateClient();

        // GIVEN a basket is created 
        var contentBasket = new StringContent(BuildBasket(), UTF8Encoding.UTF8, JsonMediaType)
        {
            Headers = { { RequestIdHeader, Guid.NewGuid().ToString() } }
        };
        await basketClient.PostAsync(BasketScenarioBase.Post.Basket, contentBasket);

        // AND basket checkout is sent
        await basketClient.PostAsync(
            BasketScenarioBase.Post.CheckoutOrder,
            new StringContent(BuildCheckout(cityExpected), UTF8Encoding.UTF8, JsonMediaType)
            {
                Headers = { { RequestIdHeader, Guid.NewGuid().ToString() } }
            });

        // WHEN Order is created in Ordering.api
        var newOrder = await TryGetNewOrderCreated(cityExpected, orderClient);
        Assert.NotNull(newOrder);

        // AND Order is cancelled in Ordering.api
        await orderClient.PutAsync(OrderingScenarioBase.Put.CancelOrder, new StringContent(BuildCancelOrder(newOrder.OrderNumber), UTF8Encoding.UTF8, JsonMediaType)
        {
            Headers = { { RequestIdHeader, Guid.NewGuid().ToString() } }
        });

        // AND the requested order is retrieved
        var order = await TryGetOrder(newOrder.OrderNumber, orderClient);

        // THEN check status
        Assert.Equal(orderStatusExpected, order.Status);
    }

    private static async Task<Order> TryGetOrder(string orderNumber, HttpClient orderClient)
    {
        var ordersGetResponse = await orderClient.GetStringAsync(OrderingScenarioBase.Get.Orders);
        var orders = JsonSerializer.Deserialize<List<Order>>(ordersGetResponse, s_jsonOptions);

        return orders.Single(o => o.OrderNumber == orderNumber);
    }

    private static async Task<Order> TryGetNewOrderCreated(string city, HttpClient orderClient)
    {
        var counter = 0;
        Order order = null;

        while (counter < 20)
        {
            //get the orders and verify that the new order has been created
            var ordersGetResponse = await orderClient.GetStringAsync(OrderingScenarioBase.Get.Orders);
            var orders = JsonSerializer.Deserialize<List<Order>>(ordersGetResponse, s_jsonOptions);

            if (orders == null || orders.Count == 0)
            {
                counter++;
                await Task.Delay(100);
                continue;
            }

            var lastOrder = orders.OrderByDescending(o => o.Date).First();
            _ = int.TryParse(lastOrder.OrderNumber, out int id);
            var orderDetails = await orderClient.GetStringAsync(OrderingScenarioBase.Get.OrderBy(id));
            order = JsonSerializer.Deserialize<Order>(orderDetails, s_jsonOptions);

            order.City = city;

            if (IsOrderCreated(order, city))
            {
                break;
            }
        }

        return order;
    }

    private static bool IsOrderCreated(Order order, string city)
    {
        return order.City == city;
    }

    private static string BuildBasket()
    {
        var order = new CustomerBasket("9e3163b9-1ae6-4652-9dc6-7898ab7b7a00");
        order.Items = new List<Microsoft.eShopOnContainers.Services.Basket.API.Model.BasketItem>()
        {
            new Microsoft.eShopOnContainers.Services.Basket.API.Model.BasketItem()
            {
                Id = "1",
                ProductName = "ProductName",
                ProductId = 1,
                UnitPrice = 10,
                Quantity = 1
            }
        };
        return JsonSerializer.Serialize(order);
    }

    private static string BuildCancelOrder(string orderId)
    {
        var order = new OrderDto()
        {
            OrderNumber = orderId
        };
        return JsonSerializer.Serialize(order);
    }

    private static string BuildCheckout(string cityExpected)
    {
        var checkoutBasket = new BasketDto()
        {
            City = cityExpected,
            Street = "street",
            State = "state",
            Country = "coutry",
            ZipCode = "zipcode",
            CardNumber = "1111111111111",
            CardHolderName = "CardHolderName",
            CardExpiration = DateTime.Now.AddYears(1),
            CardSecurityNumber = "123",
            CardTypeId = 1,
            Buyer = "Buyer",
            RequestId = Guid.NewGuid()
        };

        return JsonSerializer.Serialize(checkoutBasket);
    }
}
