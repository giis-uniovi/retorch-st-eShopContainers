package giis.eshopcontainers.e2e.functional.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.utils.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Base class for WebSPA end-to-end tests. Runs after the {@link BaseLoggedClass#setupAll()}
 * has finished overwriting the {@code sutUrl} with the WebSPA URL, allowing that all the
 * inherited browser setup, DB readiness checks, and per-test lifecycle hooks are reused
 * without duplicating them.
 *
 * <p>Before each test case browser initialization, the basket is cleared through its API,
 * and the login and logout methods are overridden to use the WebSPA component selectors.
 */
public class BaseWebSPALoggedClass extends BaseLoggedClass {


    private static String buildContainerUrl(String containerPattern) {
        return "http://" + containerPattern + "_" + tJobName + ":80";
    }

    private static String resolveUrl(String envKey, String localPropKey, String containerPattern) {
        String envUrl = System.getProperty(envKey) != null
                ? System.getProperty(envKey) : System.getenv(envKey);
        return envUrl == null
                ? properties.getProperty(localPropKey)
                : buildContainerUrl(containerPattern);
    }

    private static String addBase64Padding(String base64Url) {
        int mod = base64Url.length() % 4;
        switch (mod) {
            case 1: return base64Url + "===";
            case 2: return base64Url + "==";
            case 3: return base64Url + "=";
            default: return base64Url;
        }
    }

    /**
     * Overrides the setup of the base class, to the correct URL for the WebSPA frontend
     */
    @BeforeAll
    static void setupSPAUrl() {
        sutUrl = resolveUrl("SUT_URL", "LOCALHOST_SPA_URL", "webspa");
        log.info("WebSPA tests will connect to: {}", sutUrl);
    }

    /**
     * Overrides initialization methods to get the correct helpers for this case.
     */
    @Override
    protected void initializeHelpers() {
        this.navHelper = new NavigationWebSPA();
        this.basketHelper = new BasketWebSPA();
        this.orderHelper = new OrdersWebSPA();
    }

    /**
     * Deletes the test user's basket via API before each test, preventing test pollution.
     * The errors (e.g. the basket is empty) are ignored
     */
    @BeforeEach
    void clearUserBasket() {
        try {
            String token = getBasketTokenForCleanup();
            // The WebSPA keys baskets by the user's Identity 'sub' (GUID), not by username.
            // Decode the JWT payload (middle segment) to extract the sub claim.
            String[] jwtParts = token.split("\\.");
            String rawPayload = jwtParts[1];
            String payloadJson = new String(Base64.getUrlDecoder().decode(addBase64Padding(rawPayload)));
            JsonObject payloadObj = JsonParser.parseString(payloadJson).getAsJsonObject();
            if (!payloadObj.has("sub")) {
                log.warn("JWT payload does not contain 'sub' claim — cannot clear basket, continuing");
                return;
            }
            String sub = payloadObj.get("sub").getAsString();
            String deleteUrl = resolveBffBaseUrl() + "/basket-api/api/v1/basket/" + sub;
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpDelete req = new HttpDelete(deleteUrl);
                req.addHeader("Authorization", "Bearer " + token);
                try (CloseableHttpResponse response = client.execute(req)) {
                    log.debug("Basket cleared for sub={} before WebSPA test, HTTP {}", sub, response.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException | JsonParseException e) {
            log.warn("Could not clear basket before WebSPA test (continuing): {}", e.getMessage());
        }
    }

    protected String resolveBffBaseUrl() {
        return resolveUrl("SUT_URL", "LOCALHOST_BFF_URL", "webshoppingagg");
    }

    private String resolveIdentityUrl() {
        return resolveUrl("SUT_URL", "LOCALHOST_IDENTITY_URL", "identity_api");
    }

    private String getBasketTokenForCleanup() throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(resolveIdentityUrl() + "/connect/token");
            post.addHeader("content-type", "application/x-www-form-urlencoded");

            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", properties.getProperty("API_GRANT_TYPE")));
            params.add(new BasicNameValuePair("client_id", properties.getProperty("API_CLIENT_ID")));
            params.add(new BasicNameValuePair("username", properties.getProperty("USER_ESHOP")));
            params.add(new BasicNameValuePair("password", properties.getProperty("USER_ESHOP_PASSWORD")));
            params.add(new BasicNameValuePair("client_secret", properties.getProperty("API_SCOPE_SECRET")));
            params.add(new BasicNameValuePair("scope", "basket"));

            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            ResponseHandler<String> handler = new BasicResponseHandler();
            String response = httpclient.execute(post, handler);

            return JsonParser.parseString(response).getAsJsonObject().get("access_token").getAsString();
        }
    }

    /**
     * Override login, uses the WebSPA frontend, waiting for the {@code .esh-identity-drop}
     * only rendered when {@code authenticated=true}) retrying several times if the authentication takes
     * more time than expected.
     */
    @Override
    protected void login() throws ElementNotFoundException {
        log.debug("WebSPA login for user: {}", getUserName());
        By loginSelector = By.cssSelector(".esh-identity-section .esh-identity-name");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginSelector),
                "WebSPA LOGIN button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginSelector));

        // Fill the user/psswd form
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")),
                "Username field not present");
        driver.findElement(By.id("Username")).sendKeys(getUserName());
        driver.findElement(By.id("Password")).sendKeys(getPassword());
        By loginButtonLocator = By.xpath("//button[contains(.,'Login')]");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginButtonLocator),
                "Login button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginButtonLocator));

        // After the OIDC redirect Chrome is still mid-navigation; ignoring StaleElementReferenceException
        // ensures the wait retries instead of propagating the "aborted by navigation" error.
        By dropLocator = By.className("esh-identity-drop");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(dropLocator));

        // Re-find with a retry in case re-renders the element while we read it
        String displayedName = "";
        for (int retry = 0; retry < 3 && displayedName.isEmpty(); retry++) {
            try {
                displayedName = driver.findElement(loginSelector).getText();
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                log.debug("Stale element on login name read, retrying ({})...", retry + 1);
            }
        }
        Assertions.assertFalse(displayedName.isEmpty(),
                "Displayed name after WebSPA login must not be empty");
        isLogged = true;
        log.debug("WebSPA login successful, identity displays: {}", displayedName);
    }

    /**
     * Logs out via the WebSPA frontend using Actions hover + click to work around the
     * CSS hover-gated visibility that Selenium cannot otherwise interact with.
     */
    @Override
    protected void logout() throws ElementNotFoundException {
        log.debug("WebSPA logout");
        By identityDropLocator = By.className("esh-identity-drop");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(identityDropLocator),
                "WebSPA identity drop not found");
        WebElement identityDrop = driver.findElement(identityDropLocator);
        new Actions(driver)
                .moveToElement(identityDrop)
                .perform();
        By logoutLocator = By.xpath(
                "//*[contains(@class,'esh-identity-item')]//*[normalize-space(text())='Log Out']");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(logoutLocator),
                "WebSPA 'Log Out' item not visible after hover");
        Click.element(driver, waiter, driver.findElement(logoutLocator));
        isLogged = false;
        log.debug("WebSPA logout successful");
    }
}
