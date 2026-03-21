package giis.eshopcontainers.e2e.functional.common;

import giis.eshopcontainers.e2e.functional.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for WebSPA end-to-end tests. Runs after the {@link BaseLoggedClass#setupAll()}
 * has finished overwriting the {@code sutUrl} with the WebSPA URL, allowing that all the
 * inherited browser setup, DB readiness checks, and per-test lifecycle hooks are reused
 * without duplicating them.
 *
 * <p>The basket is cleared before each test by {@link BaseLoggedClass#clearUserBasket()},
 * which is inherited and shared with the WebMVC tests. Login and logout are overridden
 * to use the WebSPA component selectors.
 */
public class BaseWebSPALoggedClass extends BaseLoggedClass {

    private static String buildContainerUrl(String containerPattern) {
        String port = properties.getProperty("CONTAINER_PORT", "80");
        return "http://" + containerPattern + "_" + tJobName + ":" + port;
    }

    private static String resolveUrl(String envKey, String localPropKey, String containerPattern) {
        String envUrl = System.getProperty(envKey) != null
                ? System.getProperty(envKey) : System.getenv(envKey);
        return envUrl == null
                ? properties.getProperty(localPropKey)
                : buildContainerUrl(containerPattern);
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
