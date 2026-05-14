package giis.eshopcontainers.e2e.functional.tests.webspa;

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
 * Verifies that login/logout work correctly in the WebSPA frontend, checking that the
 * catalog items are disabled and the menus display the correct state.
 */
class WebSPALoggedUserTest extends BaseWebSPALoggedClass {

    /**
     * Test the login functionality in the WebSPA frontend, checking that the information of the user appears in
     * the menu and the buttons are disabled.
     */
    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, sharing = false, accessMode = "READONLY")
    @Test
    @DisplayName("BasicLoginTestSPA")
    void loginTestSPA() throws ElementNotFoundException {
        // Check that the Catalog products are disabled before the logging
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 0), "No catalog items rendered before login");
        WebElement firstItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertTrue(firstItem.getAttribute("class").contains("is-disabled"), "Catalog item should be disabled before login, class was: " + firstItem.getAttribute("class"));

        this.login();
        // After login: Checks that the username is displayed and the catalog products are enabled.
        WebElement loggedUser = driver.findElement(By.cssSelector(".esh-identity-section .esh-identity-name"));
        String displayedName = loggedUser.getText();
        Assertions.assertFalse(displayedName.isEmpty(), "Displayed name after login must not be empty");
        Assertions.assertNotEquals("LOGIN", displayedName, "Identity section still shows LOGIN after login");

        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 0), "No catalog items rendered after login");
        WebElement enabledItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertFalse(enabledItem.getAttribute("class").contains("is-disabled"), "Catalog item should be enabled after login, class was: " + enabledItem.getAttribute("class"));

        this.logout();
    }
}