package giis.eshopcontainers.e2e.functional.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * The {@code BaseAPIClass} implements all the common methods required to perform the API testing
 * of the different microservices that compose eShopOnContainers. Has the necessary operations to
 * perform the oAuth2 authentication against the identity_service as well as the common set-up and
 * tear-down methods.
*/
public class BaseAPIClass {

    public static final Logger log = LoggerFactory.getLogger(BaseAPIClass.class);
    protected static String tokenAPI;
    protected static Properties properties;
    protected static String tJobName;
    private static String desktopBFFURL;
    private static String identityURL;
    private static String user;

    public static String getUser() {return user;}
    public String getIdentityURL() {return identityURL;}
    public String getDesktopBFFURLOrders() {return desktopBFFURL+"/Order/draft/";}
    public String getDesktopBFFURLBasket() {return desktopBFFURL+"/Basket";}

    @BeforeAll
    static void setupAll() {
        log.info("Starting Global Set-up for all the Test Cases");
        try {
            log.info("Starting Global Set-up for all the Test Cases");
            properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
            tJobName = System.getProperty("tjob_name");
            user = properties.getProperty("USER_ESHOP");
            String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
            if (envUrl == null) {
                // If the envURL still being null, means that we are in local, so retrieve the identity URL and SUT url
                identityURL = properties.getProperty("LOCALHOST_IDENTITY_URL");
                desktopBFFURL = properties.getProperty("LOCALHOST_DESKTOP_BFF_URL");
                log.debug("Configuring to connect a local identity_api, whose URL is (SUT) at: {}", identityURL);
            } else {
                identityURL = "http://identity_api_" + tJobName + ":80";
                desktopBFFURL = "http://webshoppingagg_" + tJobName + ":80/api/v1";
                log.debug("Configuring the API test to use docker network connectivity, identity api at following URL: {}", identityURL);
            }
            tokenAPI = getTokenWithPasswd(identityURL);
            log.info("The token is: {}", tokenAPI);
        } catch (IOException e) {
            log.error("Failed to setup tests.", e);
        }
        log.info("Ending global setup for all test cases.");
    }

    /**
     * Performs a basic oAuth2 authentication to get the Bearer token against the identity_service of
     * eShopOnContainers. Creates one HTTP Request with the necessary headers using a custom scope and a secret.
     * @param identityURI String with the URL of the identity API
     * @return The bearer token to authenticate against the rest of the services
     */
    public static String getTokenWithPasswd(String identityURI) throws IOException {
        log.debug("Starting creating the http request with URI: {}/connect/token", identityURI);
        // Create HTTP client and POST request to send start the auth handshake
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(identityURI + "/connect/token");
        // Start adding the necessary heathers for auth
        httppost.addHeader("content-type", "application/x-www-form-urlencoded");
        // Add parameters for the authentication that would be attached to the request
        List<BasicNameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("grant_type", properties.getProperty("API_GRANT_TYPE")));
        params.add(new BasicNameValuePair("client_id", properties.getProperty("API_CLIENT_ID")));
        params.add(new BasicNameValuePair("username", properties.getProperty("USER_ESHOP")));
        params.add(new BasicNameValuePair("password", properties.getProperty("USER_ESHOP_PASSWORD")));
        params.add(new BasicNameValuePair("client_secret", properties.getProperty("API_SCOPE_SECRET")));
        params.add(new BasicNameValuePair("scope", properties.getProperty("API_SCOPE")));
        // Set parameters for HTTP request and define the response handler
        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        log.debug("Performing the request...");
        // Execute HTTP request and handle response and parse JSON response to retrieve easily the access token
        String httpResponseString = httpclient.execute(httppost, responseHandler);
        JsonObject jsonObject = JsonParser.parseString(httpResponseString).getAsJsonObject();
        log.debug("The JSON output is: {}", jsonObject);

        return jsonObject.get("access_token").getAsString();
    }
}