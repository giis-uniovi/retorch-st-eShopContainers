using System;
using Microsoft.AspNetCore.Mvc.Testing;

namespace Catalog.FunctionalTests;

public class CatalogScenariosBase 
{
    private class CatalogApplication : WebApplicationFactory<Program>
    {
        protected override IHost CreateHost(IHostBuilder builder)
        {
            builder.ConfigureAppConfiguration(c =>
            {
                var directory = Path.GetDirectoryName(typeof(CatalogScenariosBase).Assembly.Location)!;

                c.AddJsonFile(Path.Combine(directory, "appsettings.Catalog.json"), optional: false, reloadOnChange: false);
            });

            return base.CreateHost(builder);
        }
    }

    public static TestServer CreateServer()
    {
        var factory = new CatalogApplication();
        return factory.Server;
    }

    private const string ItemsRoute = "api/v1/catalog/items";

    public static class Get
    {
        private const int PageIndex = 0;
        private const int PageCount = 4;

        public static string Items(bool paginated = false)
        {
            return paginated
                ? ItemsRoute + Paginated(PageIndex, PageCount)
                : ItemsRoute;
        }

        public static string ItemById(int id)
        {
            return $"{ItemsRoute}/{id}";
        }

        public static string ItemByName(string name, bool paginated = false)
        {
            return paginated
                ? $"{ItemsRoute}/withname/{name}" + Paginated(PageIndex, PageCount)
                : $"{ItemsRoute}/withname/{name}";
        }

        public const string Types = "api/v1/catalog/catalogtypes";

        public const string Brands = "api/v1/catalog/catalogbrands";

        public static string Filtered(int catalogTypeId, int catalogBrandId, bool paginated = false)
        {
            return paginated
                ? $"{ItemsRoute}/type/{catalogTypeId}/brand/{catalogBrandId}" + Paginated(PageIndex, PageCount)
                : $"{ItemsRoute}/type/{catalogTypeId}/brand/{catalogBrandId}";
        }

        private static string Paginated(int pageIndex, int pageCount)
        {
            return $"?pageIndex={pageIndex}&pageSize={pageCount}";
        }
    }

    public static class Put
    {
        public const string UpdateCatalogProduct = ItemsRoute;
    }
}
