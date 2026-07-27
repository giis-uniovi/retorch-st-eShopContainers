namespace Microsoft.eShopOnContainers.Services.Catalog.API.Model;

public static class CatalogItemExtensions
{
    public static void FillProductUrl(this CatalogItem item, string picBaseUrl, bool azureStorageEnabled)
    {
        if (item != null)
        {
            item.PictureUri = azureStorageEnabled
                ? picBaseUrl + item.PictureFileName
                : picBaseUrl.Replace("[0]", item.Id.ToString());
        }
    }

    public static List<CatalogItem> FillProductUrls(this List<CatalogItem> items, string picBaseUrl, bool azureStorageEnabled)
    {
        foreach (var item in items)
            item.FillProductUrl(picBaseUrl, azureStorageEnabled);
        return items;
    }

    public static async Task<List<CatalogItem>> GetItemsByIdsAsync(this CatalogContext context, string ids, string picBaseUrl, bool azureStorageEnabled)
    {
        var numIds = ids.Split(',').Select(id => (Ok: int.TryParse(id, out int x), Value: x));
        if (!numIds.All(nid => nid.Ok))
            return new List<CatalogItem>();
        var idsToSelect = numIds.Select(id => id.Value);
        var items = await context.CatalogItems.Where(ci => idsToSelect.Contains(ci.Id)).ToListAsync();
        return items.FillProductUrls(picBaseUrl, azureStorageEnabled);
    }

    public static Task<(long totalItems, List<CatalogItem> itemsOnPage)> GetAllItemsPagedAsync(this CatalogContext context, int pageIndex, int pageSize, string picBaseUrl, bool azureStorageEnabled)
        => context.CatalogItems.OrderBy(c => c.Name).GetPagedAsync(pageIndex, pageSize, picBaseUrl, azureStorageEnabled);

    public static async Task<(long totalItems, List<CatalogItem> itemsOnPage)> GetPagedAsync(this IQueryable<CatalogItem> query, int pageIndex, int pageSize, string picBaseUrl, bool azureStorageEnabled)
    {
        var totalItems = await query.LongCountAsync();
        var itemsOnPage = await query.Skip(pageSize * pageIndex).Take(pageSize).ToListAsync();
        return (totalItems, itemsOnPage.FillProductUrls(picBaseUrl, azureStorageEnabled));
    }
}
