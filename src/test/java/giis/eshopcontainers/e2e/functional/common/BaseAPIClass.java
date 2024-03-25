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

public class BaseAPIClass {
    // Logger for logging messages
    public static final Logger log = LoggerFactory.getLogger(BaseAPIClass.class);

    // Static fields for storing configuration properties and token
    protected static String sutUrl;
    protected static String tokenAPI;
    protected static Properties properties;
    protected static String tJobName;

    // Method to setup before all test cases
    @BeforeAll
    static void setupAll() {
        // Log start of global setup
        log.info("Starting Global Set-up for all the Test Cases");
        try {
            log.info("Starting Global Set-up for all the Test Cases");
            properties = new Properties();
            // load a properties file for reading
            properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
            // Retrieve test job name
            tJobName = System.getProperty("tjob_name");
            String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
            if (envUrl == null) {
                // Outside CI
                sutUrl = properties.getProperty("LOCALHOST_URL");
                log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: {}", sutUrl);
            } else {
                sutUrl = envUrl + ":" + (System.getProperty("SUT_PORT") != null ? System.getProperty("SUT_PORT") : System.getenv("SUT_PORT")) + "/";
                log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: {}", sutUrl);
            }
            // Get token for authentication
            String url="http://identity_api_" + tJobName + ":80";
            //temporal fix
            //url="http://localhost:5022";
            tokenAPI = getTokenWithPasswd(url);
            log.info("The token is: {}", tokenAPI);
        } catch (IOException e) {
            // Log error if setup fails
            log.error("Failed to setup tests.", e);
        }
        // Log end of global setup
        log.info("Ending global setup for all test cases.");
    }

    // Method to retrieve token using HTTP request
    public static String getTokenWithPasswd(String identityURI) throws IOException {
        // Log start of token retrieval process
        log.debug("Starting creating the http request with URI: {}/connect/token", identityURI);

        // Create HTTP client and POST request
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(identityURI + "/connect/token");

        // Add headers
        httppost.addHeader("content-type", "application/x-www-form-urlencoded");

        // Add parameters for authentication
        List<BasicNameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("client_id", "testalice"));
        params.add(new BasicNameValuePair("username", "alice"));
        params.add(new BasicNameValuePair("password", "Pass123$"));
        params.add(new BasicNameValuePair("client_secret", "secret"));
        params.add(new BasicNameValuePair("scope", "basket"));

        // Set parameters for HTTP request
        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        // Define response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        // Log performing the request
        log.debug("Performing the request...");

        // Execute HTTP request and handle response
        String responsestr = httpclient.execute(httppost, responseHandler);

        // Parse JSON response
        JsonObject jsonObject = JsonParser.parseString(responsestr).getAsJsonObject();

        // Log JSON output
        log.debug("The JSON output is: {}", jsonObject.toString());

        // Return access token from JSON response
        return jsonObject.get("access_token").getAsString();
    }
}
