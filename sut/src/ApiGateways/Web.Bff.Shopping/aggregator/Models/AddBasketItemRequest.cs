namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Models;

public class AddBasketItemRequest
{
    [Required]
    public int? CatalogItemId { get; set; }

    public string BasketId { get; set; }

    [Required]
    public int? Quantity { get; set; }

    public AddBasketItemRequest()
    {
        Quantity = 1;
    }
}

