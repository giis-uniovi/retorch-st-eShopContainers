package giis.eshopcontainers.e2e.api;

import com.google.gson.Gson;
import giis.eshopcontainers.e2e.api.model.Order;
import giis.eshopcontainers.e2e.api.common.BaseAPIClass;
import giis.eshopcontainers.e2e.api.model.OrderItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CatalogAPITests extends BaseAPIClass {


    //https://auth0.com/docs/get-started/authentication-and-authorization-flow/resource-owner-password-flow/call-your-api-using-resource-owner-password-flow#request-tokens
    @Test
    void getsCorrectlyTheProductCatalogTest() throws IOException {

        addItemsToBasket();
        String outputGetBasket = getBasket("testuserid");


        Gson gson = new Gson();
        Order order = gson.fromJson(outputGetBasket, Order.class);

        Assertions.assertEquals("testuserid", order.getBuyer(), "The user id doesn't match");
        Assertions.assertEquals(2, order.getOrderItems().size(), "More than 2 items where found in the order");
        List<OrderItem> listitems = order.getOrderItems();
        Assertions.assertEquals(5, listitems.get(0).getProductId());
        Assertions.assertEquals("Roslyn Red Pin", listitems.get(0).getProductName());
        Assertions.assertEquals(2, listitems.get(0).getUnits());
        Assertions.assertEquals(8.5, listitems.get(0).getUnitPrice());

        Assertions.assertEquals(3, listitems.get(1).getProductId());
        Assertions.assertEquals("Prism White T-Shirt", listitems.get(1).getProductName());
        Assertions.assertEquals(1, listitems.get(1).getUnits());
        Assertions.assertEquals(12, listitems.get(1).getUnitPrice());

        System.out.println(order);
    }

    public String addItemsToBasket() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(sutUrl + ":5209/api/v1/Basket");

        // Configure Headers
        httpPut.addHeader("accept", "text/plain");
        httpPut.addHeader("content-type", "application/json");
        httpPut.addHeader("Authorization", "Bearer " + tokenAPI);

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


        StringEntity entity = new StringEntity(json);
        httpPut.setEntity(entity);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responsestr = httpclient.execute(httpPut, responseHandler);


        return responsestr;
    }


    public String getBasket(String basketId) throws IOException {
        String requestURL = sutUrl + ":5209/api/v1/Order/draft/" + basketId;
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the HTTP Get Request
            HttpGet request = new HttpGet(requestURL);

            //Configure headers
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
            e.printStackTrace();
        }

        return result;


    }


}
