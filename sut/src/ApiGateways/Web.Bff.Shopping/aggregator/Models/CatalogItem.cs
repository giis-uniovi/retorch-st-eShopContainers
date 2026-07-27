namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Models;

public class CatalogItem
{
    [Required]
    public int Id { get; set; }

    public string Name { get; set; }

    [Required]
    public decimal Price { get; set; }

    public string PictureUri { get; set; }
}


