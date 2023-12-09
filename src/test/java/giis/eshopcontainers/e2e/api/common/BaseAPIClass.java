package giis.eshopcontainers.e2e.api.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BaseAPIClass {
    public static final Logger log = LoggerFactory.getLogger(BaseAPIClass.class);
    protected static String sutUrl;
    protected static String tokenAPI;
    protected static Properties properties;
    private boolean isLogged = false;
    protected String userName = "alice";
    protected static String tjob_name;

    @BeforeAll()
    static void setupAll() throws IOException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        // load a properties file for reading
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        String envUrl = System.getProperty("SUT_URL");
        String envParameterUrl = System.getenv("SUT_URL");
        tjob_name = System.getProperty("tjob_name") != null ? System.getProperty("tjob_name") : System.getenv("tjob_name");

        if (envUrl == null & envParameterUrl == null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
            log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: " + sutUrl);

            tokenAPI=getTokenWithPasswd("http://156.35.119.57:5201");
        } else {
            sutUrl = envUrl != null ? "http://" + envUrl + "/" : "http://" + envParameterUrl ;
            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: " + sutUrl);
            tokenAPI=getTokenWithPasswd("http://156.35.119.57:5201");
        }


        log.info("Ending global setup for all test cases.");

    }


    public static String getTokenWithPasswd(String identityURI) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(identityURI+"/connect/token");

        httppost.addHeader("content-type", "application/x-www-form-urlencoded");

        List<BasicNameValuePair> params = new ArrayList<>(3);

        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("client_id", "testalice"));
        params.add(new BasicNameValuePair("username", "alice"));
        params.add(new BasicNameValuePair("password", "Pass123$"));
        params.add(new BasicNameValuePair("client_secret", "secret"));
        params.add(new BasicNameValuePair("scope", "basket"));


        httppost.setEntity(new UrlEncodedFormEntity(params, Charset.defaultCharset()));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responsestr = httpclient.execute(httppost, responseHandler);


        JsonObject jsonObject = JsonParser.parseString(responsestr).getAsJsonObject();

        return jsonObject.get("access_token").toString().replace("\"", "");

    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws ElementNotFoundException {
        log.info("Disposing user token {}", testInfo.getDisplayName());
        if (isLogged) {
            log.debug("Logging out user: {}", this.userName);
            this.logout();
        }
    }

    /**
     * Logs out the currently logged-in user.
     */
    protected void logout() {
        // Navigate to the main menu

        log.debug("Some loggoutactions");
    }
}