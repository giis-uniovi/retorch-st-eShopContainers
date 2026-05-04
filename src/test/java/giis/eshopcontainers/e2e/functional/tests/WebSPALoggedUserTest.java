package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Verifies that login/logout work correctly in the WebSPA (Angular) frontend and that
 * the catalog product tiles correctly reflect the authentication state.
 */
class WebSPALoggedUserTest extends BaseWebSPALoggedClass {

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, sharing = false, accessMode = "READONLY")
    @Test
    @DisplayName("BasicLoginTestSPA")
    void loginTestSPA() throws ElementNotFoundException {
        // Before login: catalog items must carry the "is-disabled" modifier class
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 0),
                "No catalog items rendered before login");
        WebElement firstItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertTrue(firstItem.getAttribute("class").contains("is-disabled"),
                "Catalog item should be disabled before login, class was: " + firstItem.getAttribute("class"));

        this.login();

        // After login: the SPA identity section shows the user's email address (not the username)
        // and catalog items switch from disabled to enabled
        WebElement loggedUser = driver.findElement(
                By.cssSelector(".esh-identity-section .esh-identity-name"));
        String displayedName = loggedUser.getText();
        Assertions.assertFalse(displayedName.isEmpty(), "Displayed name after login must not be empty");
        Assertions.assertNotEquals("LOGIN", displayedName, "Identity section still shows LOGIN after login");

        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 0),
                "No catalog items rendered after login");
        WebElement enabledItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertFalse(enabledItem.getAttribute("class").contains("is-disabled"),
                "Catalog item should be enabled after login, class was: " + enabledItem.getAttribute("class"));

        this.logout();
    }
}
