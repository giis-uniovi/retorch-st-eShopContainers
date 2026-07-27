namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Config;

public class UrlsConfig
{

    public static class CatalogOperations
    {
        // grpc call under REST must go trough port 80
        public static string GetItemById(int id) => $"/api/v1/catalog/items/{id}";

        public static string GetItemById(string ids) => $"/api/v1/catalog/items/ids/{string.Join(',', ids)}";

        // REST call standard must go through port 5000
        public static string GetItemsById(IEnumerable<int> ids) => $":5000/api/v1/catalog/items?ids={string.Join(',', ids)}";
    }

    public static class BasketOperations
    {
        public static string GetItemById(string id) => $"/api/v1/basket/{id}";

        public const string UpdateBasket = "/api/v1/basket";
    }

    public static class OrdersOperations
    {
        public const string GetOrderDraft = "/api/v1/orders/draft";
    }

    public string Basket { get; set; }

    public string Catalog { get; set; }

    public string Orders { get; set; }

    public string GrpcBasket { get; set; }

    public string GrpcCatalog { get; set; }

    public string GrpcOrdering { get; set; }
}

