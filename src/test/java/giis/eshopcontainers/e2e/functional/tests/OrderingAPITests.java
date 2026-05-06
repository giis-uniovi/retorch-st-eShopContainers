package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.retorch.annotations.AccessMode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * The {@code OrderingAPITests} validates the Ordering API endpoints reached through
 * the Desktop BFF (webshoppingagg).
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>GET /ordering-api/api/v1/orders/cardtypes — static list of accepted card types</li>
 *   <li>GET /ordering-api/api/v1/orders          — authenticated user's order list</li>
 *   <li>GET /ordering-api/api/v1/orders/{id}     — single order by ID</li>
 * </ul>
 */
class OrderingAPITests extends BaseAPIClass {

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GetCardTypesAPI")
    void getCardTypesAPI() throws IOException {
        String result = getCardTypes();
        Assertions.assertFalse(result.isEmpty(), "Card types response must not be empty");
        com.google.gson.JsonArray cardTypes = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertEquals(3, cardTypes.size(), "Expected exactly 3 card types (Amex, Visa, MasterCard)");
        Assertions.assertTrue(result.contains("Amex"), "Expected card type 'Amex' in response");
        Assertions.assertTrue(result.contains("Visa"), "Expected card type 'Visa' in response");
        Assertions.assertTrue(result.contains("MasterCard"), "Expected card type 'MasterCard' in response");
    }

    /** Check that the list of orders for a user, differentiating between code 200 + orders JSON,empty in a clean env*/
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("GetOrdersForUserAPI")
    void getOrdersForUserAPI() throws IOException {
        int statusCode = getOrdersStatusCode();
        Assertions.assertEquals(200, statusCode, "Expected HTTP 200 from orders list endpoint");
        String result = getOrders();
        Assertions.assertFalse(result.isEmpty(), "Orders response body must not be empty");
        com.google.gson.JsonArray orders = JsonParser.parseString(result).getAsJsonArray();
        Assertions.assertNotNull(orders, "Orders response must be a valid JSON array");
    }

    /** Test that checks that against and non-existent order id the API raises a 404 HTTP status code*/
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GetOrderByNonExistentIdAPI")
    void getOrderByNonExistentIdAPI() throws IOException {
        int statusCode = getOrderByIdStatusCode(0);
        Assertions.assertEquals(404, statusCode, "Expected HTTP 404 for a non-existent order ID");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READONLY")
    @Test
    @DisplayName("GetOrderDraftFromNonExistentBasketAPI")
    void getOrderDraftFromNonExistentBasketAPI() throws IOException {
        int statusCode = getOrderDraftStatusCode("nonexistent-basket-" + System.currentTimeMillis());
        Assertions.assertEquals(400, statusCode, "Expected HTTP 400 for a non-existent basket");
    }

    // Support methods employed to get the different bodies and status codes
    public String getCardTypes() throws IOException {return getOrderingProxyBody("/cardtypes");}
    public String getOrders() throws IOException {return getOrderingProxyBody("");}
    public int getOrdersStatusCode() throws IOException {return getOrderingProxyStatusCode("");}
    public int getOrderByIdStatusCode(int orderId) throws IOException {return getOrderingProxyStatusCode("/" + orderId);}
    public int getOrderDraftStatusCode(String basketId) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFURLOrders() + basketId);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + tokenAPI);
            HttpResponse response = httpClient.execute(request);
            return response.getStatusLine().getStatusCode();
        }
    }
}