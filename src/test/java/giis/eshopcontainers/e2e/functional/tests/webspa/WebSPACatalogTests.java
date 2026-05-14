package giis.eshopcontainers.e2e.functional.tests.webspa;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contains all the test cases that validates catalog browsing features of the WebSPA frontend
 * including the functionalities of add a new product to the basket and filtering by brand/type.
 *
 */
class WebSPACatalogTests extends BaseWebSPALoggedClass {

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
        basketHelper.checkProductButtonDisabled(driver,waiter);

        this.login();
        basketHelper.addProductToBasket(driver, waiter,1, "NetCore Cup");
        basketHelper.addProductToBasket(driver, waiter,3, "Hoodie");
        basketHelper.addProductToBasket(driver, waiter,6, "Pin");

        this.logout();
        // After logout items must be disabled again
        basketHelper.checkProductButtonDisabled(driver,waiter);
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("FilterProductsByBrandSPA")
    void filterProductsByBrandTypeSPA() throws ElementNotFoundException {
        // Option indices are 1-based: 1=All, 2=second brand, 3=third brand
        int[] brands = {1, 2, 3};
        int[] types = {1, 2, 3, 4};
        int[][] expectedNumItems = {{14, 4, 7, 3}, {7, 2, 3, 2}, {7, 2, 4, 1}};

        for (int brand : brands) {
            basketHelper.selectBrandFilter(driver,waiter,brand);
            for (int type : types) {
                basketHelper.selectTypeFilter(driver,waiter,type);
                String brandName = new String[]{"All Brands", "Net Core", "Others"}[brand - 1];
                String typeName = new String[]{"All Types", "Mug", "TShirt", "Pin"}[type - 1];
                int expected = expectedNumItems[brand - 1][type - 1];
                Assertions.assertEquals(expected, basketHelper.numberCatalogDisplayedItems(driver,waiter), "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expected);
            }
        }
    }

}
