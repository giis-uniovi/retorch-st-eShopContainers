using GrpcOrdering;

using OrderDraftDTO = GrpcOrdering.OrderDraftDTO;
using CreateOrderDraftCommand = GrpcOrdering.CreateOrderDraftCommand;
using BasketItem = GrpcOrdering.BasketItem;
using OrderItemDTO = GrpcOrdering.OrderItemDTO;

namespace Microsoft.eShopOnContainers.Services.Ordering.API.Grpc;

public class OrderingService : OrderingGrpc.OrderingGrpcBase
{
    private readonly IMediator _mediator;
    private readonly ILogger<OrderingService> _logger;

    public OrderingService(IMediator mediator, ILogger<OrderingService> logger)
    {
        _mediator = mediator;
        _logger = logger;
    }

    public override async Task<OrderDraftDTO> CreateOrderDraftFromBasketData(CreateOrderDraftCommand request, ServerCallContext context)
    {
        if (_logger.IsEnabled(LogLevel.Information))
            _logger.LogInformation("Begin grpc call from method {Method} for ordering get order draft {CreateOrderDraftCommand}", context.Method, request);
        if (_logger.IsEnabled(LogLevel.Trace))
            _logger.LogTrace(
                "Sending command: {CommandName} - {IdProperty}: {CommandId} ({@Command})",
                request.GetGenericTypeName(),
                nameof(request.BuyerId),
                request.BuyerId,
                request);

        var command = new AppCommand.CreateOrderDraftCommand(
                        request.BuyerId,
                        MapBasketItems(request.Items));

        var data = await _mediator.Send(command);

        if (data != null)
        {
            context.Status = new Status(StatusCode.OK, $" ordering get order draft {request} do exist");

            return MapResponse(data);
        }
        else
        {
            context.Status = new Status(StatusCode.NotFound, $" ordering get order draft {request} do not exist");
        }

        return new OrderDraftDTO();
    }

    public static OrderDraftDTO MapResponse(AppCommand.OrderDraftDto order)
    {
        var result = new OrderDraftDTO()
        {
            Total = (double)order.Total,
        };

        order.OrderItems.ToList().ForEach(i => result.OrderItems.Add(new OrderItemDTO()
        {
            Discount = (double)i.Discount,
            PictureUrl = i.PictureUrl,
            ProductId = i.ProductId,
            ProductName = i.ProductName,
            UnitPrice = (double)i.UnitPrice,
            Units = i.Units,
        }));

        return result;
    }

    public static IEnumerable<ApiModels.BasketItem> MapBasketItems(RepeatedField<BasketItem> items)
    {
        return items.Select(x => new ApiModels.BasketItem()
        {
            Id = x.Id,
            ProductId = x.ProductId,
            ProductName = x.ProductName,
            UnitPrice = (decimal)x.UnitPrice,
            OldUnitPrice = (decimal)x.OldUnitPrice,
            Quantity = x.Quantity,
            PictureUrl = x.PictureUrl,
        });
    }
}
