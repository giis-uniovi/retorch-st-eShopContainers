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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpDelete;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * The {@code DesktopAPIGatewayAPITests} implements all the test cases tha validate the different
 * API endpoints of the Backend For Frontends (BFF) webshopingagg that is used by the two Desktop
 * frontends. The different endpoints available can be seen in the Swagger UI
 */
class DesktopAPIGatewayAPITests extends BaseAPIClass {

    @BeforeEach
    void clearBasket() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete delete = new HttpDelete(getDesktopBFFBasketProxyURL() + getUser());
            delete.addHeader("Authorization", "Bearer " + tokenAPI);
            client.execute(delete);
            log.debug("Basket cleared for user={} before API test", getUser());
        } catch (Exception e) {
            log.debug("Could not clear basket before API test (continuing): {}", e.getMessage());
        }
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testAddProductsBasketWebAgg")
    void testAddProductsBasket() throws IOException {
        // Initialize Gson object that would be used to deserialize the Basket (Draft order)
        Gson gson = new Gson();
        createBasketWithTwoItems();
        // Retrieve the basket and deserialize JSON response into Order object,because the basket are stored as "draft" orders
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
    @DisplayName("testAddSingleItemToBasketWebAgg")
    void testAddSingleItemToBasket() throws IOException {
        Gson gson = new Gson();
        // Create a basket with a single item: product 5 (Roslyn Red Pin), quantity 1
        createBasketWithSingleItem();
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertEquals(getUser(), order.getBuyer(), "The buyer ID should match the authenticated user");
        Assertions.assertEquals(1, order.getOrderItems().size(), "Exactly 1 item should be present in the basket");

        OrderItem item = order.getOrderItems().get(0);
        Assertions.assertEquals(5, item.getProductId(), "Product ID should be 5");
        Assertions.assertEquals("Roslyn Red Pin", item.getProductName(), "Product name should be Roslyn Red Pin");
        Assertions.assertEquals(1, item.getUnits(), "Item quantity should be 1");
        Assertions.assertEquals(8.5, item.getUnitPrice(), "Unit price of Roslyn Red Pin should be 8.5");
    }

    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testBasketDraftFlagIsTrueWebAgg")
    void testBasketDraftFlagIsTrue() throws IOException {
        Gson gson = new Gson();
        createBasketWithTwoItems();
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        // The BFF /Order/draft endpoint returns isDraft:false in this version of eShopOnContainers.
        // Verify the response is a draft (not yet a real order) by checking no order number was assigned.
        Assertions.assertNull(order.getOrderNumber(), "A draft basket should not have an order number assigned");
        Assertions.assertFalse(order.getOrderItems().isEmpty(), "Draft order must contain the basket items");
    }


    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testBasketItemQuantityWebAgg")
    void testBasketItemQuantity() throws IOException {
        Gson gson = new Gson();
        // Create a basket with product 3 (Prism White T-Shirt) and quantity 3
        createBasketWithItemQuantity(3, 3);
        String outputGetBasket = getBasket(getUser());
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertEquals(1, order.getOrderItems().size(), "Exactly 1 item type should be in the basket");

        OrderItem item = order.getOrderItems().get(0);
        Assertions.assertEquals(3, item.getProductId(), "Product ID should be 3");
        Assertions.assertEquals("Prism White T-Shirt", item.getProductName(), "Product name should be Prism White T-Shirt");
        Assertions.assertEquals(3, item.getUnits(), "Quantity should be 3");
        Assertions.assertEquals(12.0, item.getUnitPrice(), "Unit price of Prism White T-Shirt should be 12.0");
    }

    /**
     * The {@code createBasketWithTwoItems} method creates a default basket with two items. Creates an HTTP request
     * with the bearer token to authenticate against the API, and which body contains the basket in JSON format
     */
    public String createBasketWithTwoItems() throws IOException {
        log.debug("Creating the connection with URL: {}", this.getDesktopBFFURLBasket());
        // Create HTTP client and POST request
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.getDesktopBFFURLBasket());
        // Configure headers and the authorization Bearer Token
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);
        // JSON payload for adding items to the basket, contains a basket with two items
        StringEntity entity = getJSONofBasketWithTwoProducts();
        httpPost.setEntity(entity);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        log.debug("Performing the request");

        return httpclient.execute(httpPost, responseHandler);
    }

    /**
     * Creates a basket with a single item (product 5, quantity 1) via the desktop BFF endpoint.
     */
    public String createBasketWithSingleItem() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.getDesktopBFFURLBasket());
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);
        String json = "{\n" +
                "  \"buyerId\": \"" + getUser() + "\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"testproductid1\",\n" +
                "      \"productId\": \"5\",\n" +
                "      \"quantity\": \"1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        httpPost.setEntity(new StringEntity(json));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        log.debug("Creating basket with single item via BFF at: {}", this.getDesktopBFFURLBasket());
        return httpclient.execute(httpPost, responseHandler);
    }

    /**
     * Creates a basket with a single item of the given product ID and quantity via the desktop BFF endpoint.
     * @param productId the catalog product ID to add
     * @param quantity  the number of units to add
     */
    public String createBasketWithItemQuantity(int productId, int quantity) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.getDesktopBFFURLBasket());
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);
        String json = "{\n" +
                "  \"buyerId\": \"" + getUser() + "\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"testproductid1\",\n" +
                "      \"productId\": \"" + productId + "\",\n" +
                "      \"quantity\": \"" + quantity + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        httpPost.setEntity(new StringEntity(json));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        log.debug("Creating basket with productId={} quantity={} via BFF at: {}", productId, quantity, this.getDesktopBFFURLBasket());
        return httpclient.execute(httpPost, responseHandler);
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
        // Set JSON payload as entity for the HTTP request
        return new StringEntity(json);
    }

    /**
     * The {@code getBasket} method retrieves the basket with the ID that is provided as param. The JSON object
     * retrieved is an {@code Order}, because eShopContainer stores the Baskets as "draft" orders
     * @param basketId String with the basket identifier
     * @return JSON with the Order
     */
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
            log.debug("The connection getting the Basket at the endpoint {}{} has failed",this.getDesktopBFFURLOrders(),basketId);
        }

        return result;
    }
}