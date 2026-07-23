namespace UnitTest.Ordering.Application;

public class IdentifiedCommandHandlerTest
{
    private readonly Mock<IRequestManager> _requestManager;
    private readonly Mock<IMediator> _mediator;
    private readonly Mock<ILogger<CreateOrderIdentifiedCommandHandler>> _loggerMock;

    public IdentifiedCommandHandlerTest()
    {
        _requestManager = new Mock<IRequestManager>();
        _mediator = new Mock<IMediator>();
        _loggerMock = new Mock<ILogger<CreateOrderIdentifiedCommandHandler>>();
    }

    [Fact]
    public async Task Handler_sends_command_when_order_no_exists()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeOrderCmd = new IdentifiedCommand<CreateOrderCommand, bool>(FakeOrderRequest(), fakeGuid);

        _requestManager.Setup(x => x.ExistAsync(It.IsAny<Guid>()))
            .Returns(Task.FromResult(false));

        _mediator.Setup(x => x.Send(It.IsAny<IRequest<bool>>(), default))
            .Returns(Task.FromResult(true));

        // Act
        var handler = new CreateOrderIdentifiedCommandHandler(_mediator.Object, _requestManager.Object, _loggerMock.Object);
        var result = await handler.Handle(fakeOrderCmd, CancellationToken.None);

        // Assert
        Assert.True(result);
        _mediator.Verify(x => x.Send(It.IsAny<IRequest<bool>>(), default), Times.Once());
    }

    [Fact]
    public async Task Handler_sends_no_command_when_order_already_exists()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeOrderCmd = new IdentifiedCommand<CreateOrderCommand, bool>(FakeOrderRequest(), fakeGuid);

        _requestManager.Setup(x => x.ExistAsync(It.IsAny<Guid>()))
            .Returns(Task.FromResult(true));

        _mediator.Setup(x => x.Send(It.IsAny<IRequest<bool>>(), default))
            .Returns(Task.FromResult(true));

        // Act
        var handler = new CreateOrderIdentifiedCommandHandler(_mediator.Object, _requestManager.Object, _loggerMock.Object);
        var result = await handler.Handle(fakeOrderCmd, CancellationToken.None);

        // Assert
        _mediator.Verify(x => x.Send(It.IsAny<IRequest<bool>>(), default), Times.Never());
    }

    private static CreateOrderCommand FakeOrderRequest(Dictionary<string, object> args = null)
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
