package giis.eshopcontainers.e2e.functional.tests;

import com.google.gson.Gson;
import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.eshopcontainers.e2e.functional.model.Order;
import giis.eshopcontainers.e2e.functional.model.OrderItem;
import giis.retorch.annotations.AccessMode;
import giis.retorch.annotations.Resource;
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
import java.util.List;

public class WebAggAPITests extends BaseAPIClass {

    // Test to ensure items are added to the basket correctly
    @Resource(resID = "identity-api", replaceable = {})
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Resource(resID = "basket-api", replaceable = {})
    @AccessMode(resID = "basket-api", concurrency = 30,sharing = true, accessMode = "READWRITE")
    @Resource(resID = "eshopUser", replaceable = {})
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testAddProductsBasketWebAgg")
    void testAddProductsBasket() throws IOException, InterruptedException {
        // Initialize Gson for JSON serialization/deserialization
        Gson gson = new Gson();

        addItemsToBasket();

        // Retrieve the basket and deserialize JSON response into Order object
        String outputGetBasket = getBasket("testuserid");
        Order order = gson.fromJson(outputGetBasket, Order.class);

        // Assertions to validate basket content
        Assertions.assertEquals("testuserid", order.getBuyer(), "The user id doesn't match");
        Assertions.assertEquals(2, order.getOrderItems().size(), "More than 2 items were found in the order");

        List<OrderItem> listitems = order.getOrderItems();
        // Assertions for each order item
        Assertions.assertEquals(5, listitems.get(0).getProductId());
        Assertions.assertEquals("Roslyn Red Pin", listitems.get(0).getProductName());
        Assertions.assertEquals(2, listitems.get(0).getUnits());
        Assertions.assertEquals(8.5, listitems.get(0).getUnitPrice());

        Assertions.assertEquals(3, listitems.get(1).getProductId());
        Assertions.assertEquals("Prism White T-Shirt", listitems.get(1).getProductName());
        Assertions.assertEquals(1, listitems.get(1).getUnits());
        Assertions.assertEquals(12, listitems.get(1).getUnitPrice());
    }

    // Method to add items to the basket
    public String addItemsToBasket() throws IOException {
        // Debug log for creating connection
        log.debug("Creating the connection with URL: http://basket_api_" + tJobName + ":80/api/v1/Basket");
        // Create HTTP client and POST request
        HttpClient httpclient = HttpClients.createDefault();
        String requestURL ="http://webshoppingagg_" + tJobName + ":80/api/v1/Basket";
        //requestURL = "http://localhost:5016//api/v1/Basket";
        HttpPost httpPost = new HttpPost(requestURL);
        // Configure headers
        httpPost.addHeader("accept", "text/plain");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenAPI);

        // JSON payload for adding items to the basket
        String json = "{\n" +
                "  \"buyerId\": \"testuserid\",\n" +
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
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);

        // Define response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        // Debug log for performing the request
        log.debug("Performing the request");
        // Execute HTTP request and return response
        return httpclient.execute(httpPost, responseHandler);
    }

    // Method to get basket details
    public String getBasket(String basketId) {
        // Define request URL
        String requestURL = "http://webshoppingagg_" + tJobName + ":80/api/v1/Order/draft/" + basketId;
        //requestURL = "http://localhost:5016/api/v1/Order/draft/" + basketId;
        // Initialize result string
        String result = "";

        // Execute HTTP request and handle response
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the HTTP Get Request
            HttpGet request = new HttpGet(requestURL);

            // Configure headers
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + tokenAPI);

            // Execute the request and obtain the response
            HttpResponse response = httpClient.execute(request);

            // Obtain the body answer
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            // Print stack trace for IOException
            log.debug("The connection failed");
        }

        // Return response result
        return result;
    }
}
