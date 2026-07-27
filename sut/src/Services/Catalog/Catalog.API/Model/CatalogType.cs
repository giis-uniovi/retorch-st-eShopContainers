namespace Microsoft.eShopOnContainers.Services.Catalog.API.Model;

public class CatalogType
{
    [Required, System.Text.Json.Serialization.JsonRequired]
    public int Id { get; set; }

    public string Type { get; set; }
}
