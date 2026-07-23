namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Services;

public class OrderingService : IOrderingService
{
    private readonly GrpcOrdering.OrderingGrpc.OrderingGrpcClient _orderingGrpcClient;
    private readonly ILogger<OrderingService> _logger;

    public OrderingService(GrpcOrdering.OrderingGrpc.OrderingGrpcClient orderingGrpcClient, ILogger<OrderingService> logger)
    {
        _orderingGrpcClient = orderingGrpcClient;
        _logger = logger;
    }

    public async Task<OrderData> GetOrderDraftAsync(BasketData basketData)
    {
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug(" grpc client created, basketData={@BasketData}", basketData);

        var command = MapToOrderDraftCommand(basketData);
        var response = await _orderingGrpcClient.CreateOrderDraftFromBasketDataAsync(command);
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug(" grpc response: {@Response}", response);

        return MapToResponse(response, basketData);
    }

    private static OrderData MapToResponse(GrpcOrdering.OrderDraftDTO orderDraft, BasketData basketData)
    {
        if (orderDraft == null)
        {
            return null;
        }

        var data = new OrderData
        {
            Buyer = basketData.BuyerId,
            Total = (decimal)orderDraft.Total,
        };

        orderDraft.OrderItems.ToList().ForEach(o => data.OrderItems.Add(new OrderItemData
        {
            Discount = (decimal)o.Discount,
            PictureUrl = o.PictureUrl,
            ProductId = o.ProductId,
            ProductName = o.ProductName,
            UnitPrice = (decimal)o.UnitPrice,
            Units = o.Units,
        }));

        return data;
    }

    private static GrpcOrdering.CreateOrderDraftCommand MapToOrderDraftCommand(BasketData basketData)
    {
        var command = new GrpcOrdering.CreateOrderDraftCommand
        {
            BuyerId = basketData.BuyerId,
        };

        basketData.Items.ForEach(i => command.Items.Add(new GrpcOrdering.BasketItem
        {
            Id = i.Id,
            OldUnitPrice = (double)i.OldUnitPrice,
            PictureUrl = i.PictureUrl,
            ProductId = i.ProductId,
            ProductName = i.ProductName,
            Quantity = i.Quantity,
            UnitPrice = (double)i.UnitPrice,
        }));

        return command;
    }

}
