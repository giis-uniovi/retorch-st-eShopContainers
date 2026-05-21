package giis.eshopcontainers.e2e.functional.tests.webspa;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Test class for Basket-related functionalities on the WebSPA frontend.
 */
class WebSPABasketTests extends BaseWebSPALoggedClass {

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddProductsToBasketSPA")
    void addProductsToBasketSPA() throws ElementNotFoundException {
        // Before login catalog items must be disabled
        basketHelper.checkProductButtonDisabled(driver, waiter);

        this.login();
        basketHelper.addProductToBasket(driver, waiter, 1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter, 3, "Hoodie");
        basketHelper.addProductToBasket(driver, waiter, 6, "Pin");

        this.logout();
        // After logout items must be disabled again
        basketHelper.checkProductButtonDisabled(driver, waiter);
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("TestBasketContentsVisibleSPA")
    void testBasketContentsVisibleSPA() throws ElementNotFoundException {
        login();
        // Add two products to the basket
        basketHelper.addProductToBasket(driver, waiter, 1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter, 3, "Hoodie");

        // Navigate to the basket page
        navHelper.navigateToBasket(driver, waiter);

        // Wait for basket items to render and count them
        // The SPA basket shows only product rows (no separate total summary row unlike WebMVC)
        // class verified in basket.component.html: <article class="esh-basket-item"> inside *ngFor
        By basketItemsLocator = By.className("esh-basket-item");
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(basketItemsLocator, 0),
                "No basket items rendered after navigation");
        List<WebElement> basketItems = driver.findElements(basketItemsLocator);
        Assertions.assertEquals(2, basketItems.size(),
                "Expected 2 rows in basket (one per product)");

        logout();
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("TestRemoveItemFromBasketSPA")
    void testRemoveItemFromBasketSPA() throws ElementNotFoundException {
        login();
        // Add two products to the basket
        basketHelper.addProductToBasket(driver, waiter, 2, ".NET Blue Hoodie");
        basketHelper.addProductToBasket(driver, waiter, 4, ".NET Foundation Pin");

        // Navigate to the basket page
        navHelper.navigateToBasket(driver, waiter);

        // Wait for delete buttons to appear and click the first one
        // class verified in basket.component.html: <div class="esh-basket-delete" (click)="deleteItem(...)"> inside each esh-basket-item
        By deleteButtonsLocator = By.className("esh-basket-delete");
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(deleteButtonsLocator, 0),
                "No delete buttons found in the basket page");
        List<WebElement> deleteButtons = driver.findElements(deleteButtonsLocator);
        Assertions.assertFalse(deleteButtons.isEmpty(), "No delete buttons found in the basket page");
        Click.element(driver, waiter, deleteButtons.get(0));

        // After deletion, the basket badge should reflect 1 remaining item
        waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "1"),
                "Basket badge should show 1 item after removing one product");

        // Verify only one item row remains
        List<WebElement> basketItemsAfterDelete = driver.findElements(By.className("esh-basket-item"));
        Assertions.assertEquals(1, basketItemsAfterDelete.size(),
                "Expected 1 row in basket after deletion");

        logout();
    }
}
