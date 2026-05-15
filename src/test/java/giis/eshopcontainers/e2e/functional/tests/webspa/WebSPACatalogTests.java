package giis.eshopcontainers.e2e.functional.tests.webspa;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    @ParameterizedTest(name = "filterProductsByBrandTypeSPA brand={1}, type={3}, expected={4}")
    @CsvSource({
        "1, 'All Brands', 1, 'All Types', 14",
        "1, 'All Brands', 2, 'Mug',        4",
        "1, 'All Brands', 3, 'TShirt',     7",
        "1, 'All Brands', 4, 'Pin',        3",
        "2, 'Net Core',   1, 'All Types',  7",
        "2, 'Net Core',   2, 'Mug',        2",
        "2, 'Net Core',   3, 'TShirt',     3",
        "2, 'Net Core',   4, 'Pin',        2",
        "3, 'Others',     1, 'All Types',  7",
        "3, 'Others',     2, 'Mug',        2",
        "3, 'Others',     3, 'TShirt',     4",
        "3, 'Others',     4, 'Pin',        1"
    })
    void filterProductsByBrandTypeSPA(int brand, String brandName, int type, String typeName, int expected) throws ElementNotFoundException {
        basketHelper.selectBrandFilter(driver, waiter, brand);
        basketHelper.selectTypeFilter(driver, waiter, type);
        Assertions.assertEquals(expected, basketHelper.numberCatalogDisplayedItems(driver, waiter),
                "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expected);
    }

}
