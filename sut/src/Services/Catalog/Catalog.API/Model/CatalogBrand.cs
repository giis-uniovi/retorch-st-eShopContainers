namespace Microsoft.eShopOnContainers.Services.Catalog.API.Model;

public class CatalogBrand
{
    [Required, System.Text.Json.Serialization.JsonRequired]
    public int Id { get; set; }

    public string Brand { get; set; }
}
