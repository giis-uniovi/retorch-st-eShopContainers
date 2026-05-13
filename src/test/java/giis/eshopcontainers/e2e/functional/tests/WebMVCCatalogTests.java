package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;


class WebMVCCatalogTests extends BaseLoggedClass {

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30,sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddProductsToBasketMVC")
    void addProductsToBasketMVC() throws ElementNotFoundException {
        log.debug("Before login, checking that the product buttons are disabled");
        // Verify that the product cup button is disabled before login
        checkProductButtonDisabled();
        // Perform login
        this.login();
        // Add products to the basket
        basketHelper.addProductToBasket(driver, waiter,1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter,3, "Hoodie");
        basketHelper.addProductToBasket(driver, waiter,6, "Pin");
        // Perform logout
        this.logout();
        // Verify that the product cup button is disabled after logout
        checkProductButtonDisabled();
    }

    /**
     * Checks that the product buttons are disabled in MVC frontend.
     */
    private void checkProductButtonDisabled() throws ElementNotFoundException {
        //Navigate to main menu.
        navHelper.toMainMenu(driver, waiter);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"),4),"The number of elements was less than 4 (items)");
        WebElement productCupButton;
        log.debug("Checking that the product buttons are disabled");
        // Verify that the product cup button is disabled
        productCupButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[1]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button is-disabled", productCupButton.getAttribute("class"),
                "The eShop product button was expected to be disabled but was enabled");
    }

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("FilterProductsByBrandMVC")
    void FilterProductsByBrandTypeMVC() throws ElementNotFoundException {
        // Define test data
        int[] brands = {1, 2, 3};
        int[] types = {1, 2, 3, 4};
        int[][] expectedNumItems = {{14, 4, 7, 3}, {7, 2, 3, 2}, {7, 2, 4, 1}};
        // Iterate over brands
        for (int brand : brands) {
            basketHelper.selectBrandFilter(driver,waiter,brand);
            // Iterate over types
            for (int type : types) {
                basketHelper.selectTypeFilter(driver,waiter,type);
                // Verify the number of displayed items
                String brandName = new String[]{"All Brands", "Net Core", "Others"}[brand - 1];
                String typeName = new String[]{"All Types", "Mug", "TShirt", "Pin"}[type - 1];
                int expectedItems = expectedNumItems[brand - 1][type - 1];
                Assertions.assertEquals(expectedItems, basketHelper.numberCatalogDisplayedItems(driver,waiter),
                        "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expectedItems);
            }
        }
    }
}