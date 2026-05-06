package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.eshopcontainers.e2e.functional.model.CatalogItem;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * The {@code CatalogAPITests} validates the Catalog API endpoints reached through the Desktop BFF
 * (webshoppingagg). Each request lands on the BFF gateway which strips the {@code /catalog-api}
 *  prefix and forwards to the catalog service.
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>GET /catalog-api/api/v1/catalog/items                           — paginated item list</li>
 *   <li>GET /catalog-api/api/v1/catalog/items/{id}                      — item by ID</li>
 *   <li>GET /catalog-api/api/v1/catalog/items/withname/{name}           — items by name</li>
 *   <li>GET /catalog-api/api/v1/catalog/items/type/{tId}/brand/{bId}    — items filtered by type/brand</li>
 *   <li>GET /catalog-api/api/v1/catalog/catalogtypes                    — catalog type list</li>
 *   <li>GET /catalog-api/api/v1/catalog/catalogbrands                   — catalog brand list</li>
 * </ul>
 */
class CatalogAPITests extends BaseAPIClass {

    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GetCatalogItemsCatalogAPI")
    void getCatalogItemsAPI() throws IOException {
        String result = getCatalogProxyBody("items");
        Assertions.assertFalse(result.isEmpty(), "Response from catalog items API must not be empty");
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        Assertions.assertTrue(json.get("count").getAsInt() > 0, "Expected at least one catalog item in the count");
        Assertions.assertFalse(json.get("data").getAsJsonArray().isEmpty(), "Catalog items data array must not be empty");
    }

    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GetCatalogItemByIdCatalogAPI")
    void getCatalogItemByIdAPI() throws IOException {
        Gson gson = new Gson();
        String result = getCatalogProxyBody("items/5");
        Assertions.assertFalse(result.isEmpty(), "Response from catalog API must not be empty");
        CatalogItem item = gson.fromJson(result, CatalogItem.class);
        Assertions.assertEquals(5, item.getId(), "Catalog item id mismatch");
        Assertions.assertEquals("Roslyn Red Pin", item.getName(), "Catalog item name mismatch");
        Assertions.assertEquals(8.5, item.getPrice(), 0.001, "Catalog item price mismatch");
    }

    /** Test the paginated list endpoint, retrieving the elements that start with a given prefix*/
    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GetCatalogItemsByNameCatalogAPI")
    void getCatalogItemsByNameAPI() throws IOException {
        String result = getCatalogProxyBody("items/withname/Roslyn");
        Assertions.assertFalse(result.isEmpty(), "Response from items-by-name endpoint must not be empty");
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        Assertions.assertTrue(json.get("count").getAsInt() >= 1, "Expected at least one matching item for 'Roslyn'");
        JsonArray data = json.get("data").getAsJsonArray();
        Assertions.assertFalse(data.isEmpty(), "Data array must contain at least one item");
        Assertions.assertTrue(result.contains("Roslyn Red Pin"), "Expected 'Roslyn Red Pin' in matching items");
    }
    /** Test the paginated list endpoint, retrieving the elements filtered by brand and type*/
    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("getCatalogItemsByTypeAndBrandCatalogAPI")
    void getCatalogItemsByTypeAndBrandAPI() throws IOException {
        String result = getCatalogProxyBody("items/type/1/brand/1");
        Assertions.assertFalse(result.isEmpty(), "Response from type/brand filter must not be empty");
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        Assertions.assertTrue(json.has("count"), "Response must include a count field");
        Assertions.assertTrue(json.has("data"), "Response must include a data field");
    }

    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("getCatalogTypesCatalogAPI")
    void getCatalogTypesAPI() throws IOException {
        String result = getCatalogProxyBody("catalogtypes");
        Assertions.assertFalse(result.isEmpty(), "Catalog types response must not be empty");
        JsonArray types = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertFalse(types.isEmpty(), "Catalog types list must not be empty");
    }

    @AccessMode(resID = "catalog-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testGetCatalogBrandsCatalogAPI")
    void getCatalogBrandsAPI() throws IOException {
        String result = getCatalogProxyBody("catalogbrands");
        Assertions.assertFalse(result.isEmpty(), "Catalog brands response must not be empty");
        JsonArray brands = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertFalse(brands.isEmpty(), "Catalog brands list must not be empty");
    }
}