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
import org.junit.jupiter.api.Assertions;
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