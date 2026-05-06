package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.Gson;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.eshopcontainers.e2e.functional.model.Order;
import giis.eshopcontainers.e2e.functional.model.OrderItem;
import giis.retorch.annotations.AccessMode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * The {@code BasketAPITests} validates the Basket-related endpoints reached by the
 * Desktop BFF (webshoppingagg). The basket aggregator endpoints are served directly by BFF
 * controllers; the Order/draft endpoint composes data from the Basket and Catalog services.
 *
 * <p>Endpoints under test (all reached through the gateway at {@code desktopBFFURL}):
 * <ul>
 *   <li>POST  /api/v1/Basket              — replace/update basket   (UpdateAllBasket)</li>
 *   <li>POST  /api/v1/Basket/items        — add a single item        (AddBasketItem)</li>
 *   <li>PUT   /api/v1/Basket/items        — update item quantities   (UpdateQuantities)</li>
 * </ul>
 */
class BasketAPITests extends BaseAPIClass {

    @BeforeEach
    void setUp() {
        log.info("Starting Global Set-up for all the Test Cases");
        clearBasket(getUser());
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddProductsBasketAPI")
    void addProductsBasketAPI() throws IOException {
        Gson gson = new Gson();
        createBasketWithTwoItems();
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertEquals(getUser(), order.getBuyer(), "The user id doesn't match");
        Assertions.assertEquals(2, order.getOrderItems().size(), "More than 2 items were found in the order");

        List<OrderItem> listItems = order.getOrderItems();
        Assertions.assertEquals(5, listItems.get(0).getProductId());
        Assertions.assertEquals("Roslyn Red Pin", listItems.get(0).getProductName());
        Assertions.assertEquals(2, listItems.get(0).getUnits());
        Assertions.assertEquals(8.5, listItems.get(0).getUnitPrice());
        Assertions.assertEquals(3, listItems.get(1).getProductId());
        Assertions.assertEquals("Prism White T-Shirt", listItems.get(1).getProductName());
        Assertions.assertEquals(1, listItems.get(1).getUnits());
        Assertions.assertEquals(12, listItems.get(1).getUnitPrice());
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddSingleBasketItemAPI")
    void addSingleBasketItemAPI() throws IOException {
        Gson gson = new Gson();
        addItemToBasket(getUser(), 5, 3);
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertNotNull(order, "Draft order must not be null");
        Assertions.assertFalse(order.getOrderItems().isEmpty(), "No items found in the draft order");
        OrderItem item = order.getOrderItems().stream()
                .filter(i -> i.getProductId() == 5)
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(item, "Product 5 (Roslyn Red Pin) not found in the basket");
        Assertions.assertEquals("Roslyn Red Pin", item.getProductName());
        Assertions.assertEquals(3, item.getUnits());
        Assertions.assertEquals(8.5, item.getUnitPrice());
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("UpdateBasketItemQuantitiesAPI")
    void updateBasketItemQuantitiesAPI() throws IOException {
        Gson gson = new Gson();
        createBasketWithTwoItems();
        updateBasketItemQuantities(getUser(), "testproductid1", 5, "testproductid2", 3);
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertNotNull(order, "Draft order must not be null");
        Assertions.assertEquals(2, order.getOrderItems().size(), "Expected exactly 2 items in the order");

        OrderItem product5 = order.getOrderItems().stream()
                .filter(i -> i.getProductId() == 5).findFirst().orElse(null);
        OrderItem product3 = order.getOrderItems().stream()
                .filter(i -> i.getProductId() == 3).findFirst().orElse(null);

        Assertions.assertNotNull(product5, "Product 5 not found after quantity update");
        Assertions.assertEquals(5, product5.getUnits(), "Product 5 quantity not updated correctly");
        Assertions.assertNotNull(product3, "Product 3 not found after quantity update");
        Assertions.assertEquals(3, product3.getUnits(), "Product 3 quantity not updated correctly");
    }

    public String createBasketWithTwoItems() throws IOException {
        log.debug("Creating the connection with URL: {}", this.getDesktopBFFURLBasket());
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.getDesktopBFFURLBasket());
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);
        httpPost.setEntity(getJSONofBasketWithTwoProducts());
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return httpclient.execute(httpPost, responseHandler);
    }

    public String addItemToBasket(String basketId, int catalogItemId, int quantity) throws IOException {
        log.debug("Adding item {} (qty={}) to basket {}", catalogItemId, quantity, basketId);
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.getDesktopBFFURLBasketItems());
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);
        String json = "{\n" +
                "  \"basketId\": \"" + basketId + "\",\n" +
                "  \"catalogItemId\": " + catalogItemId + ",\n" +
                "  \"quantity\": " + quantity + "\n" +
                "}";
        httpPost.setEntity(new StringEntity(json));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return httpclient.execute(httpPost, responseHandler);
    }

    public String updateBasketItemQuantities(String basketId,
                                             String item1Id, int newQty1,
                                             String item2Id, int newQty2) throws IOException {
        log.debug("Updating quantities in basket {}", basketId);
        HttpClient httpclient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(this.getDesktopBFFURLBasketItems());
        httpPut.addHeader("accept", "text/plain");
        httpPut.addHeader("content-type", "application/json");
        httpPut.addHeader("Authorization", "Bearer " + tokenAPI);
        String json = "{\n" +
                "  \"basketId\": \"" + basketId + "\",\n" +
                "  \"updates\": [\n" +
                "    {\"basketItemId\": \"" + item1Id + "\", \"newQty\": " + newQty1 + "},\n" +
                "    {\"basketItemId\": \"" + item2Id + "\", \"newQty\": " + newQty2 + "}\n" +
                "  ]\n" +
                "}";
        httpPut.setEntity(new StringEntity(json));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return httpclient.execute(httpPut, responseHandler);
    }

    public String getBasket(String basketId) {
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFURLOrders() + basketId);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + tokenAPI);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            log.debug("Failed to GET draft order at {}{}", this.getDesktopBFFURLOrders(), basketId);
        }
        return result;
    }

    /**
     * Clears the user's basket via DELETE on the basket service through the BFF YARP proxy.
     * Ignores errors so it is safe to call from {@code @BeforeEach}.
     */
    private void clearBasket(String userId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(this.getDesktopBFFBasketProxyURL() + userId);
            request.addHeader("Authorization", "Bearer " + tokenAPI);
            httpClient.execute(request);
            log.debug("Basket cleared for user {}", userId);
        } catch (IOException e) {
            log.debug("Failed to clear basket for user {} — will continue anyway", userId);
        }
    }

    private static StringEntity getJSONofBasketWithTwoProducts() throws UnsupportedEncodingException {
        String json = "{\n" +
                "  \"buyerId\": \"" + getUser() + "\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"testproductid1\",\n" +
                "      \"productId\": \"5\",\n" +
                "      \"quantity\": \"2\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"testproductid2\",\n" +
                "      \"productId\": \"3\",\n" +
                "      \"quantity\": \"1\"\n" +
                "    }\n" +
                "\n" +
                "  ]\n" +
                "}";
        return new StringEntity(json);
    }
}
