namespace Microsoft.eShopOnContainers.Services.Ordering.API.Extensions;

public static class BasketItemExtensions
{
    public static IEnumerable<OrderItemDto> ToOrderItemsDto(this IEnumerable<BasketItem> basketItems)
    {
        foreach (var item in basketItems)
        {
            yield return item.ToOrderItemDto();
        }
    }

    public static OrderItemDto ToOrderItemDto(this BasketItem item)
    {
        return new OrderItemDto()
        {
            ProductId = item.ProductId,
            ProductName = item.ProductName,
            PictureUrl = item.PictureUrl,
            UnitPrice = item.UnitPrice,
            Units = item.Quantity
        };
    }
}
