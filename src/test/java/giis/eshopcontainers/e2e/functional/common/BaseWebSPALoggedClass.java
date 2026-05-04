package giis.eshopcontainers.e2e.functional.common;

import com.google.gson.JsonParser;
import giis.eshopcontainers.e2e.functional.utils.Click;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Base class for WebSPA (Angular) end-to-end tests. Runs AFTER {@link BaseLoggedClass#setupAll()}
 * has finished and simply overwrites {@code sutUrl} with the WebSPA URL so all inherited
 * browser setup, DB readiness checks, and per-test lifecycle hooks are reused without duplication.
 *
 * <p>Each test starts with an empty basket — a {@code @BeforeEach} hook deletes the user's basket
 * via the BFF basket-api proxy before the browser is initialised for the test.
 *
 * <p>Login and logout are overridden to use the Angular identity component selectors. The OIDC
 * redirect to the Identity server (Username / Password form) is identical to WebMVC and not
 * duplicated.
 */
public class BaseWebSPALoggedClass extends BaseLoggedClass {

    // -----------------------------------------------------------------------
    // @BeforeAll — override sutUrl after the parent has finished
    // -----------------------------------------------------------------------

    @BeforeAll
    static void setupSPAUrl() {
        String envUrl = System.getProperty("SUT_URL") != null
                ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        if (envUrl == null) {
            sutUrl = properties.getProperty("LOCALHOST_SPA_URL");
        } else {
            sutUrl = "http://webspa_" + tJobName + ":80";
        }
        log.info("WebSPA tests will connect to: {}", sutUrl);
    }

    // -----------------------------------------------------------------------
    // @BeforeEach — clear basket so tests start with a predictable cart state
    // -----------------------------------------------------------------------

    /**
     * Deletes the test user's basket via the BFF YARP basket-api proxy before each test.
     * This prevents item accumulation across test classes and repeated test runs.
     * Errors are swallowed so a cleanup failure does not abort the test itself.
     */
    @BeforeEach
    void clearUserBasket() {
        try {
            String token = getBasketTokenForCleanup();
            // The WebSPA keys baskets by the user's Identity 'sub' (GUID), not by username.
            // Decode the JWT payload (middle segment) to extract the sub claim.
            String[] jwtParts = token.split("\\.");
            String rawPayload = jwtParts[1];
            int paddingNeeded = (4 - rawPayload.length() % 4) % 4;
            String padded = paddingNeeded == 1 ? rawPayload + "=" : paddingNeeded == 2 ? rawPayload + "==" : rawPayload;
            String payloadJson = new String(Base64.getUrlDecoder().decode(padded));
            String sub = JsonParser.parseString(payloadJson).getAsJsonObject().get("sub").getAsString();
            String deleteUrl = resolveBffBaseUrl() + "/basket-api/api/v1/basket/" + sub;
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpDelete req = new HttpDelete(deleteUrl);
                req.addHeader("Authorization", "Bearer " + token);
                client.execute(req);
                log.debug("Basket cleared for sub={} before WebSPA test", sub);
            }
        } catch (Exception e) {
            log.debug("Could not clear basket before WebSPA test (continuing): {}", e.getMessage());
        }
    }

    protected String resolveBffBaseUrl() {
        String envUrl = System.getProperty("SUT_URL") != null
                ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        return envUrl == null
                ? properties.getProperty("LOCALHOST_DESKTOP_BFF_URL").replace("/api/v1", "")
                : "http://webshoppingagg_" + tJobName + ":80";
    }

    private String resolveIdentityUrl() {
        String envUrl = System.getProperty("SUT_URL") != null
                ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        return envUrl == null
                ? properties.getProperty("LOCALHOST_IDENTITY_URL")
                : "http://identity_api_" + tJobName + ":80";
    }

    private String getBasketTokenForCleanup() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
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

    // -----------------------------------------------------------------------
    // Login / logout — SPA-specific selectors
    // -----------------------------------------------------------------------

    /**
     * Logs in via the WebSPA Angular identity component. After the OIDC redirect back to the SPA,
     * waits for {@code .esh-identity-drop} (only rendered when {@code authenticated=true}) using a
     * {@link WebDriverWait} that ignores {@link WebDriverException} — Chrome DevTools Protocol
     * commands can be aborted mid-navigation and must be retried.
     */
    @Override
    protected void login() throws ElementNotFoundException {
        log.debug("WebSPA login for user: {}", userName);
        By loginSelector = By.cssSelector(".esh-identity-section .esh-identity-name");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginSelector),
                "WebSPA LOGIN button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginSelector));

        // Fill the shared Identity server login form
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")),
                "Username field not present");
        driver.findElement(By.id("Username")).sendKeys(userName);
        driver.findElement(By.id("Password")).sendKeys(password);
        By loginButtonLocator = By.xpath("//button[contains(.,'Login')]");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginButtonLocator),
                "Login button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginButtonLocator));

        // After the OIDC redirect Chrome is still mid-navigation; ignoring WebDriverException
        // ensures the wait retries instead of propagating the "aborted by navigation" error.
        By dropLocator = By.className("esh-identity-drop");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .ignoring(WebDriverException.class)
                .until(ExpectedConditions.presenceOfElementLocated(dropLocator));

        // Re-find with a retry in case Angular re-renders the element while we read it
        String displayedName = "";
        for (int retry = 0; retry < 3 && displayedName.isEmpty(); retry++) {
            try {
                displayedName = driver.findElement(loginSelector).getText();
            } catch (Exception e) {
                log.debug("Stale element on login name read, retrying ({})...", retry + 1);
            }
        }
        Assertions.assertFalse(displayedName.isEmpty(),
                "Displayed name after WebSPA login must not be empty");
        isLogged = true;
        log.debug("WebSPA login successful, identity displays: {}", displayedName);
    }

    /**
     * Logs out via the WebSPA Angular identity drop-down using a JS click to bypass the
     * CSS hover-gated visibility that Selenium's standard click check enforces.
     */
    @Override
    protected void logout() throws ElementNotFoundException {
        log.debug("WebSPA logout");
        By logoutLocator = By.xpath(
                "//*[contains(@class,'esh-identity-item')]//*[normalize-space(text())='Log Out']");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(logoutLocator),
                "WebSPA 'Log Out' item not found in DOM");
        Click.byJS(driver, driver.findElement(logoutLocator));
        isLogged = false;
        log.debug("WebSPA logout successful");
    }
}
