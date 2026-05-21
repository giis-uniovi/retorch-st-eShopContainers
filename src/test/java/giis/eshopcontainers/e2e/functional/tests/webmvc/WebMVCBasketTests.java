package giis.eshopcontainers.e2e.functional.tests.webmvc;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Test class for Basket-related functionalities on the WebMVC frontend.
 */
class WebMVCBasketTests extends BaseLoggedClass {

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddProductsToBasketMVC")
    void addProductsToBasketMVC() throws ElementNotFoundException {
        log.debug("Before login, checking that the product buttons are disabled");
        basketHelper.checkProductButtonDisabled(driver, waiter);
        this.login();
        basketHelper.addProductToBasket(driver, waiter, 1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter, 3, "Hoodie");
        basketHelper.addProductToBasket(driver, waiter, 6, "Pin");
        this.logout();
        basketHelper.checkProductButtonDisabled(driver, waiter);
    }

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("TestBasketContentsVisibleMVC")
    void testBasketContentsVisibleMVC() throws ElementNotFoundException {
        login();
        // Add two products to the basket
        basketHelper.addProductToBasket(driver, waiter, 1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter, 3, "Hoodie");

        // Navigate to the basket page
        navHelper.navigateToBasket(driver, waiter);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("cartForm")),
                "Basket form not loaded after navigation");

        // Count row divs inside the basket form: N items + 1 total summary row
        List<WebElement> basketRows = driver.findElements(
                By.xpath("//*[@id='cartForm']/div/div[2]/div"));
        Assertions.assertEquals(3, basketRows.size(),
                "Expected 3 rows in basket (2 product items + 1 total summary row)");

        logout();
    }

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Disabled("WebMVC basket page has no per-item delete buttons in this version")
    @Test
    @DisplayName("TestRemoveItemFromBasketMVC")
    void testRemoveItemFromBasketMVC() throws ElementNotFoundException {
        login();
        // Add two products to the basket
        basketHelper.addProductToBasket(driver, waiter, 2, ".NET Blue Hoodie");
        basketHelper.addProductToBasket(driver, waiter, 4, ".NET Foundation Pin");

        // Navigate to the basket page
        navHelper.navigateToBasket(driver, waiter);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("cartForm")),
                "Basket form not loaded after navigation");

        // Delete the first product in the basket
        List<WebElement> deleteButtons = driver.findElements(By.className("esh-basket-delete"));
        Assertions.assertFalse(deleteButtons.isEmpty(), "No delete buttons found in the basket page");
        Click.element(driver, waiter, deleteButtons.get(0));

        // After deletion, the basket badge should reflect 1 remaining item
        waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "1"),
                "Basket badge should show 1 item after removing one product");

        // Verify only one item row remains plus the total row (= 2 rows)
        List<WebElement> basketRowsAfterDelete = driver.findElements(
                By.xpath("//*[@id='cartForm']/div/div[2]/div"));
        Assertions.assertEquals(2, basketRowsAfterDelete.size(),
                "Expected 2 rows in basket (1 product item + 1 total summary row) after deletion");

        logout();
    }

}