package giis.eshopcontainers.e2e.functional.tests.webspa;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.BasketWebSPA;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Contains all the test cases that validates catalog browsing features of the WebSPA frontend
 * including the functionalities of add a new product to the basket and filtering by brand/type.
 *
 */
class WebSPACatalogTests extends BaseWebSPALoggedClass {

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @ParameterizedTest(name = "FilterProductsByBrandTypeSPA brand={1}, type={3}, expected={4}")
    @CsvFileSource(resources = "/catalog-filter-combinations.csv", numLinesToSkip = 1)
    void filterProductsByBrandTypeSPA(int brand, String brandName, int type, String typeName, int expected) throws ElementNotFoundException {
        basketHelper.selectBrandFilter(driver, waiter, brand);
        basketHelper.selectTypeFilter(driver, waiter, type);
        Assertions.assertEquals(expected, basketHelper.numberCatalogDisplayedItems(driver, waiter),
                "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expected);
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("TestCatalogPaginationSPA")
    void testCatalogPaginationSPA() throws ElementNotFoundException {
        BasketWebSPA spaBasket = (BasketWebSPA) basketHelper;

        // Navigate to the main menu (default: All Brands, All Types)
        navHelper.toMainMenu(driver, waiter);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(BasketWebSPA.PAGER_INFO_LOCATOR),
                "Pager info not present after navigation to main menu");

        // Count items on the first page
        int firstPageCount = driver.findElements(By.className("esh-catalog-item")).size();
        log.debug("First page item count: {}", firstPageCount);
        Assertions.assertTrue(firstPageCount > 0, "First page should show at least one item");

        // The Next button should be visible since there are 14 items total but only 10 per page
        WebElement nextButton = driver.findElement(By.id("Next"));
        Assertions.assertTrue(nextButton.isDisplayed(), "Next button should be visible on the first page");

        // Navigate to the second page and wait for the pager text to update
        String pagerTextPage1 = driver.findElement(BasketWebSPA.PAGER_INFO_LOCATOR).getText();
        Click.element(driver, waiter, nextButton);
        spaBasket.waitForPagerUpdate(driver, waiter, pagerTextPage1);

        int secondPageCount = driver.findElements(By.className("esh-catalog-item")).size();
        log.debug("Second page item count: {}", secondPageCount);
        Assertions.assertTrue(secondPageCount > 0, "Second page should show at least one item");
        Assertions.assertEquals(14, firstPageCount + secondPageCount,
                "Total catalog items across both pages should be 14");

        // The Previous button should be visible on the second page
        WebElement previousButton = driver.findElement(By.id("Previous"));
        Assertions.assertTrue(previousButton.isDisplayed(), "Previous button should be visible on the second page");

        // Navigate back to the first page and wait for the pager text to update
        String pagerTextPage2 = driver.findElement(BasketWebSPA.PAGER_INFO_LOCATOR).getText();
        Click.element(driver, waiter, previousButton);
        spaBasket.waitForPagerUpdate(driver, waiter, pagerTextPage2);

        int backToFirstCount = driver.findElements(By.className("esh-catalog-item")).size();
        Assertions.assertEquals(firstPageCount, backToFirstCount,
                "Item count after navigating back should match the initial first page count");
    }

}
