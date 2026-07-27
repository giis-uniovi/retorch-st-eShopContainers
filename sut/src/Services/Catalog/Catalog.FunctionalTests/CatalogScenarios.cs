namespace Catalog.FunctionalTests;

public class CatalogScenarios
    : CatalogScenariosBase
{
    [Fact]
    public async Task Get_get_all_catalogitems_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.Items());

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_catalogitem_by_id_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.ItemById(1));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_catalogitem_by_id_and_response_bad_request_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.ItemById(int.MinValue));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task Get_get_catalogitem_by_id_and_response_not_found_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.ItemById(int.MaxValue));

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task Get_get_catalogitem_by_name_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.ItemByName(".NET"));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_paginated_catalogitem_by_name_and_response_ok_status_code()
    {
        using var server = CreateServer();
        const bool paginated = true;
        var response = await server.CreateClient()
            .GetAsync(Get.ItemByName(".NET", paginated));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_paginated_catalog_items_and_response_ok_status_code()
    {
        using var server = CreateServer();
        const bool paginated = true;
        var response = await server.CreateClient()
            .GetAsync(Get.Items(paginated));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_filtered_catalog_items_and_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.Filtered(1, 1));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_get_paginated_filtered_catalog_items_and_response_ok_status_code()
    {
        using var server = CreateServer();
        const bool paginated = true;
        var response = await server.CreateClient()
            .GetAsync(Get.Filtered(1, 1, paginated));

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_catalog_types_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.Types);

        Assert.True(response.IsSuccessStatusCode);
    }

    [Fact]
    public async Task Get_catalog_brands_response_ok_status_code()
    {
        using var server = CreateServer();
        var response = await server.CreateClient()
            .GetAsync(Get.Brands);

        Assert.True(response.IsSuccessStatusCode);
    }
}
