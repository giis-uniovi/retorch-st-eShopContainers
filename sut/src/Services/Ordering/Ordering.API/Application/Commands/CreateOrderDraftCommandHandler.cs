namespace Microsoft.eShopOnContainers.Services.Ordering.API.Application.Commands;

using Microsoft.eShopOnContainers.Services.Ordering.API.Extensions;
using Microsoft.eShopOnContainers.Services.Ordering.Domain.AggregatesModel.OrderAggregate;

// Regular CommandHandler
public class CreateOrderDraftCommandHandler
    : IRequestHandler<CreateOrderDraftCommand, OrderDraftDto>
{
    public Task<OrderDraftDto> Handle(CreateOrderDraftCommand message, CancellationToken cancellationToken)
    {

        var order = Order.NewDraft();
        var orderItems = message.Items.Select(i => i.ToOrderItemDto());
        foreach (var item in orderItems)
        {
            order.AddOrderItem(item.ProductId, item.ProductName, item.UnitPrice, item.Discount, item.PictureUrl, item.Units);
        }

        return Task.FromResult(OrderDraftDto.FromOrder(order));
    }
}

public record OrderDraftDto
{
    public IEnumerable<OrderItemDto> OrderItems { get; init; }
    public decimal Total { get; init; }

    public static OrderDraftDto FromOrder(Order order)
    {
        return new OrderDraftDto()
        {
            OrderItems = order.OrderItems.Select(oi => new OrderItemDto
            {
                Discount = oi.GetCurrentDiscount(),
                ProductId = oi.ProductId,
                UnitPrice = oi.GetUnitPrice(),
                PictureUrl = oi.GetPictureUri(),
                Units = oi.GetUnits(),
                ProductName = oi.GetOrderItemProductName()
            }),
            Total = order.GetTotal()
        };
    }
}

public record OrderItemDto
{
    public int ProductId { get; init; }

    public string ProductName { get; init; }

    public decimal UnitPrice { get; init; }

    public decimal Discount { get; init; }

    public int Units { get; init; }

    public string PictureUrl { get; init; }
}
