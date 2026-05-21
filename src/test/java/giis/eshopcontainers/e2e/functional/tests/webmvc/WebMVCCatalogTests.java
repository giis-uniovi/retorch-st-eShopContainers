package giis.eshopcontainers.e2e.functional.tests.webmvc;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
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
 * Test class for Catalog-related functionalities.
 */
class WebMVCCatalogTests extends BaseLoggedClass {

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @ParameterizedTest(name = "TestFilterProductsByBrandTypeMVC brand={1}, type={3}, expected={4}")
    @CsvFileSource(resources = "/catalog-filter-combinations.csv", numLinesToSkip = 1)
    void testFilterProductsByBrandTypeMVC(int brand, String brandName, int type, String typeName, int expected) throws ElementNotFoundException {
        basketHelper.selectBrandFilter(driver, waiter, brand);
        basketHelper.selectTypeFilter(driver, waiter, type);
        Assertions.assertEquals(expected, basketHelper.numberCatalogDisplayedItems(driver, waiter),
                "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expected);
    }

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("TestCatalogPagination")
    void testCatalogPagination() throws ElementNotFoundException {
        // Navigate to the main menu (default: All Brands, All Types)
        navHelper.toMainMenu(driver, waiter);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 4),
                "The number of catalog items on the first page was less than expected");

        // Count items on the first page
        WebElement firstPageFirstItem = driver.findElement(By.className("esh-catalog-item"));
        int firstPageCount = driver.findElements(By.className("esh-catalog-item")).size();
        log.debug("First page item count: {}", firstPageCount);
        Assertions.assertTrue(firstPageCount > 0, "First page should show at least one item");

        // The Next button should be visible since there are 14 items total but only 10 per page
        WebElement nextButton = driver.findElement(By.id("Next"));
        Assertions.assertTrue(nextButton.isDisplayed(), "Next button should be visible on the first page");

        // Navigate to the second page; wait for the first-page items to go stale before counting
        Click.element(driver, waiter, nextButton);
        waiter.waitUntil(ExpectedConditions.stalenessOf(firstPageFirstItem),
                "Page did not transition after clicking Next");

        int secondPageCount = driver.findElements(By.className("esh-catalog-item")).size();
        log.debug("Second page item count: {}", secondPageCount);
        Assertions.assertTrue(secondPageCount > 0, "Second page should show at least one item");
        Assertions.assertEquals(14, firstPageCount + secondPageCount,
                "Total catalog items across both pages should be 14");

        // The Previous button should be visible on the second page
        WebElement previousButton = driver.findElement(By.id("Previous"));
        Assertions.assertTrue(previousButton.isDisplayed(), "Previous button should be visible on the second page");

        // Navigate back to the first page; wait for the second-page items to go stale before counting
        WebElement secondPageFirstItem = driver.findElement(By.className("esh-catalog-item"));
        Click.element(driver, waiter, previousButton);
        waiter.waitUntil(ExpectedConditions.stalenessOf(secondPageFirstItem),
                "Page did not transition after clicking Previous");

        int backToFirstCount = driver.findElements(By.className("esh-catalog-item")).size();
        Assertions.assertEquals(firstPageCount, backToFirstCount,
                "Item count after navigating back should match the initial first page count");
    }
}