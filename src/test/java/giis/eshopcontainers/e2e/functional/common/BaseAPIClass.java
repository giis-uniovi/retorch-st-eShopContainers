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
    protected static String tokenOrdering;
    protected static Properties properties;
    protected static String tJobName;
    private static String desktopBFFURL;
    private static String desktopBFFBaseURL;
    private static String identityURL;
    private static String paymentURL;
    private static String user;

    public static String getUser() {return user;}
    public String getIdentityURL() {return identityURL;}
    public String getPaymentURL() {return paymentURL;}
    public String getDesktopBFFURLOrders() {return desktopBFFURL + "/Order/draft/";}
    public String getDesktopBFFURLBasket() {return desktopBFFURL + "/Basket";}
    public String getDesktopBFFURLBasketItems() {return desktopBFFURL + "/Basket/items";}
    public String getDesktopBFFCatalogURL() {return desktopBFFBaseURL + "/catalog-api/api/v1/catalog/";}
    public String getDesktopBFFOrderingURL() {return desktopBFFBaseURL + "/ordering-api/api/v1/orders";}
    public String getDesktopBFFBasketProxyURL() {return desktopBFFBaseURL + "/basket-api/api/v1/basket/";}
    public String getDesktopBFFPaymentURL() {return desktopBFFBaseURL + "/payment-api";}
    public String getDesktopBFFIdentityURL() {return desktopBFFBaseURL + "/identity-api";}

    @BeforeAll
    static void setupAll() {
        log.info("Starting Global Set-up for all the Test Cases");
        try {
            properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
            tJobName = System.getProperty("TJOB_NAME");
            user = properties.getProperty("USER_ESHOP");
            String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
            if (envUrl == null) {
                identityURL = properties.getProperty("LOCALHOST_IDENTITY_URL");
                desktopBFFURL = properties.getProperty("LOCALHOST_DESKTOP_BFF_URL");
                desktopBFFBaseURL = desktopBFFURL.replace("/api/v1", "");
                paymentURL = properties.getProperty("LOCALHOST_PAYMENT_URL");
                log.debug("Configuring to connect a local identity_api, whose URL is (SUT) at: {}", identityURL);
            } else {
                identityURL = "http://identity_api_" + tJobName + ":80";
                desktopBFFURL = "http://webshoppingagg_" + tJobName + ":80/api/v1";
                desktopBFFBaseURL = "http://webshoppingagg_" + tJobName + ":80";
                paymentURL = "http://payment_api_" + tJobName + ":80";
                log.debug("Configuring the API test to use docker network connectivity, identity api at following URL: {}", identityURL);
            }
            tokenAPI = getTokenWithPasswd(identityURL);
            tokenOrdering = getTokenForScope(identityURL, "orders");
            log.info("Basket token: {}", tokenAPI);
            log.info("Ordering token: {}", tokenOrdering);
        } catch (IOException e) {
            log.error("Failed to setup tests.", e);
        }
        log.info("Ending global setup for all test cases.");
    }

    /**
     * Performs an OAuth2 Resource Owner Password Credentials (ROPC) flow to obtain a Bearer token
     * for the scope configured in {@code API_SCOPE} (test.properties).
     *
     * @param identityURI base URL of the identity/token endpoint
     * @return access token string
     */
    public static String getTokenWithPasswd(String identityURI) throws IOException {
        return getTokenForScope(identityURI, properties.getProperty("API_SCOPE"));
    }

    /**
     * Performs an OAuth2 ROPC flow using the test user credentials, requesting the given scope.
     *
     * @param identityURI base URL of the identity/token endpoint
     * @param scope       space-separated list of scopes to request (e.g. {@code "orders"}, {@code "openid profile"})
     * @return access token string
     */
    public static String getTokenForScope(String identityURI, String scope) throws IOException {
        log.debug("Requesting token with scope='{}' from {}/connect/token", scope, identityURI);
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(identityURI + "/connect/token");
        httppost.addHeader("content-type", "application/x-www-form-urlencoded");
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", properties.getProperty("API_GRANT_TYPE")));
        params.add(new BasicNameValuePair("client_id", properties.getProperty("API_CLIENT_ID")));
        params.add(new BasicNameValuePair("username", properties.getProperty("USER_ESHOP")));
        params.add(new BasicNameValuePair("password", properties.getProperty("USER_ESHOP_PASSWORD")));
        params.add(new BasicNameValuePair("client_secret", properties.getProperty("API_SCOPE_SECRET")));
        params.add(new BasicNameValuePair("scope", scope));
        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String httpResponseString = httpclient.execute(httppost, responseHandler);
        JsonObject jsonObject = JsonParser.parseString(httpResponseString).getAsJsonObject();
        log.debug("Token response JSON: {}", jsonObject);
        return jsonObject.get("access_token").getAsString();
    }
}
