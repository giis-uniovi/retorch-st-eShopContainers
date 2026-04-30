package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.retorch.annotations.AccessMode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * The {@code OrderingAPIGatewayAPITests} validates the Ordering API endpoints reached through
 * the Desktop BFF (webshoppingagg) YARP reverse proxy at {@code /ordering-api/...}.
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>GET /ordering-api/api/v1/orders/cardtypes — static list of accepted card types</li>
 *   <li>GET /ordering-api/api/v1/orders          — authenticated user's order list</li>
 *   <li>GET /ordering-api/api/v1/orders/{id}     — single order by ID (negative path)</li>
 * </ul>
 */
class OrderingAPIGatewayAPITests extends BaseAPIClass {

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testGetCardTypesWebAgg")
    void testGetCardTypes() throws IOException {
        // The ordering service seeds exactly three card types: Amex (1), Visa (2), MasterCard (3).
        String result = getCardTypes();
        Assertions.assertFalse(result.isEmpty(), "Card types response must not be empty");
        com.google.gson.JsonArray cardTypes = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertEquals(3, cardTypes.size(), "Expected exactly 3 card types (Amex, Visa, MasterCard)");
        Assertions.assertTrue(result.contains("Amex"), "Expected card type 'Amex' in response");
        Assertions.assertTrue(result.contains("Visa"), "Expected card type 'Visa' in response");
        Assertions.assertTrue(result.contains("MasterCard"), "Expected card type 'MasterCard' in response");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("testGetOrdersForUserWebAgg")
    void testGetOrdersForUser() throws IOException {
        // The list may be empty in a fresh environment; the key assertion is HTTP 200 + valid JSON array.
        int statusCode = getOrdersStatusCode();
        Assertions.assertEquals(200, statusCode, "Expected HTTP 200 from orders list endpoint");
        String result = getOrders();
        Assertions.assertFalse(result.isEmpty(), "Orders response body must not be empty");
        com.google.gson.JsonArray orders = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertNotNull(orders, "Orders response must be a valid JSON array");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testGetOrderByNonExistentIdWebAgg")
    void testGetOrderByNonExistentId() throws IOException {
        // Order ID 0 can never exist (IDs start at 1) — must return HTTP 404.
        int statusCode = getOrderByIdStatusCode(0);
        Assertions.assertEquals(404, statusCode, "Expected HTTP 404 for a non-existent order ID");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    public String getCardTypes() throws IOException {
        return getOrderingProxyBody("/cardtypes");
    }

    public String getOrders() throws IOException {
        return getOrderingProxyBody("");
    }

    public int getOrdersStatusCode() throws IOException {
        return getOrderingProxyStatusCode("");
    }

    public int getOrderByIdStatusCode(int orderId) throws IOException {
        return getOrderingProxyStatusCode("/" + orderId);
    }

    private String getOrderingProxyBody(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFOrderingURL() + path);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + tokenOrdering);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : "";
        }
    }

    private int getOrderingProxyStatusCode(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFOrderingURL() + path);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + tokenOrdering);
            HttpResponse response = httpClient.execute(request);
            return response.getStatusLine().getStatusCode();
        }
    }
}
