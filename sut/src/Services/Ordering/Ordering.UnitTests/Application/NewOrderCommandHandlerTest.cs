using Microsoft.eShopOnContainers.Services.Ordering.API.Application.IntegrationEvents;

namespace UnitTest.Ordering.Application;

using Microsoft.eShopOnContainers.Services.Ordering.Domain.AggregatesModel.OrderAggregate;

public class NewOrderRequestHandlerTest
{
    private readonly Mock<IOrderRepository> _orderRepositoryMock;
    private readonly Mock<IIdentityService> _identityServiceMock;
    private readonly Mock<IMediator> _mediator;
    private readonly Mock<IOrderingIntegrationEventService> _orderingIntegrationEventService;

    public NewOrderRequestHandlerTest()
    {

        _orderRepositoryMock = new Mock<IOrderRepository>();
        _identityServiceMock = new Mock<IIdentityService>();
        _orderingIntegrationEventService = new Mock<IOrderingIntegrationEventService>();
        _mediator = new Mock<IMediator>();
    }

    [Fact]
    public async Task Handle_return_false_if_order_is_not_persisted()
    {
        var buyerId = "1234";

        var fakeOrderCmd = FakeOrderRequestWithBuyer(new Dictionary<string, object>
        { ["cardExpiration"] = DateTime.Now.AddYears(1) });

        _orderRepositoryMock.Setup(orderRepo => orderRepo.GetAsync(It.IsAny<int>()))
            .Returns(Task.FromResult<Order>(FakeOrder()));

        _orderRepositoryMock.Setup(buyerRepo => buyerRepo.UnitOfWork.SaveChangesAsync(default))
            .Returns(Task.FromResult(1));

        _identityServiceMock.Setup(svc => svc.GetUserIdentity()).Returns(buyerId);

        var LoggerMock = new Mock<ILogger<CreateOrderCommandHandler>>();
        //Act
        var handler = new CreateOrderCommandHandler(_orderingIntegrationEventService.Object, _orderRepositoryMock.Object, LoggerMock.Object);
        var cltToken = new System.Threading.CancellationToken();
        var result = await handler.Handle(fakeOrderCmd, cltToken);

        //Assert
        Assert.False(result);
    }

    [Fact]
    public void Handle_throws_exception_when_no_buyerId()
    {
        //Assert
        Assert.Throws<ArgumentNullException>(() => new Buyer(string.Empty, string.Empty));
    }

    private static Buyer FakeBuyer()
    {
        return new Buyer(Guid.NewGuid().ToString(), "1");
    }

    private static Order FakeOrder()
    {
        return new Order("1", "fakeName", new Address("street", "city", "state", "country", "zipcode"), 1, "12", "111", "fakeName", DateTime.Now.AddYears(1));
    }

    private static CreateOrderCommand FakeOrderRequestWithBuyer(Dictionary<string, object> args = null)
    {
        return new CreateOrderCommand(
            new List<BasketItem>(),
            userId: args != null && args.TryGetValue("userId", out var userId) ? (string)userId : null,
            userName: args != null && args.TryGetValue("userName", out var userName) ? (string)userName : null,
            city: args != null && args.TryGetValue("city", out var city) ? (string)city : null,
            street: args != null && args.TryGetValue("street", out var street) ? (string)street : null,
            state: args != null && args.TryGetValue("state", out var state) ? (string)state : null,
            country: args != null && args.TryGetValue("country", out var country) ? (string)country : null,
            zipcode: args != null && args.TryGetValue("zipcode", out var zipcode) ? (string)zipcode : null,
            cardNumber: args != null && args.TryGetValue("cardNumber", out var cardNumber) ? (string)cardNumber : "1234",
            cardExpiration: args != null && args.TryGetValue("cardExpiration", out var cardExpiration) ? (DateTime)cardExpiration : DateTime.MinValue,
            cardSecurityNumber: args != null && args.TryGetValue("cardSecurityNumber", out var cardSecurityNumber) ? (string)cardSecurityNumber : "123",
            cardHolderName: args != null && args.TryGetValue("cardHolderName", out var cardHolderName) ? (string)cardHolderName : "XXX",
            cardTypeId: args != null && args.TryGetValue("cardTypeId", out var cardTypeId) ? (int)cardTypeId : 0);
    }
}
