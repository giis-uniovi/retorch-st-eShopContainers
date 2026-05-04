package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasketSPA;

/**
 * Validates catalog browsing features of the WebSPA (Angular) frontend:
 * adding products to the basket and filtering by brand/type.
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
    void addProductsToBasket() throws ElementNotFoundException {
        // Before login catalog items must be disabled
        checkProductButtonDisabled();
        this.login();
        addProductToBasketSPA(1, "NetCore Cup", driver, waiter);
        addProductToBasketSPA(3, "Hoodie", driver, waiter);
        addProductToBasketSPA(6, "Pin", driver, waiter);
        this.logout();
        // After logout items must be disabled again
        checkProductButtonDisabled();
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("FilterProductsByBrandSPA")
    void filterProductsByBrandType() throws ElementNotFoundException {
        // Option indices are 1-based: 1=All, 2=second brand, 3=third brand
        int[] brands = {1, 2, 3};
        int[] types  = {1, 2, 3, 4};
        int[][] expectedNumItems = {{14, 4, 7, 3}, {7, 2, 3, 2}, {7, 2, 4, 1}};

        for (int brand : brands) {
            selectBrandFilter(brand);
            for (int type : types) {
                selectTypeFilter(type);
                String brandName = new String[]{"All Brands", "Net Core", "Others"}[brand - 1];
                String typeName  = new String[]{"All Types", "Mug", "TShirt", "Pin"}[type - 1];
                int expected = expectedNumItems[brand - 1][type - 1];
                Assertions.assertEquals(expected, numberCatalogDisplayedItems(),
                        "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expected);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Navigates to the catalog and asserts all visible product tiles are disabled. */
    private void checkProductButtonDisabled() throws ElementNotFoundException {
        Navigation.toMainMenuSPA(driver, waiter);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.className("esh-catalog-item"), 4), "Expected more than 4 catalog items");
        WebElement firstItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertTrue(firstItem.getAttribute("class").contains("is-disabled"),
                "Catalog item should be disabled when not logged in, class was: " + firstItem.getAttribute("class"));
    }

    /**
     * Selects an option from the brand or type {@code <select>} dropdown by 1-based index
     * then clicks the "Apply" button and waits for the catalog to reload.
     *
     * @param filterId      the HTML {@code id} attribute of the {@code <select>} element
     * @param filterOptions display names for log messages (1-based)
     * @param option        1-based option index
     */
    public void selectFilter(String filterId, String[] filterOptions, Integer option) throws ElementNotFoundException {
        By pagerInfoLocator = By.cssSelector(".esh-pager-item:not([id])");
        // Capture current pager text so we can detect when the catalog reloads
        String pagerTextBefore = "";
        try {
            pagerTextBefore = driver.findElement(pagerInfoLocator).getText();
        } catch (Exception e) {
            log.debug("Pager not present before first filter apply — will wait for presence instead");
        }

        Select select = new Select(driver.findElement(By.id(filterId)));
        select.selectByIndex(option - 1);
        log.debug("Selected {} : {}", filterId, filterOptions[option - 1]);

        By applyLocator = By.xpath("//button[normalize-space(text())='Apply']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(applyLocator), "Apply button is not clickable");
        Click.element(driver, waiter, driver.findElement(applyLocator));

        // Wait for the pager to show a different text (new count arrived from the API).
        // A 3-second timeout is used because a subset of filter combinations produce the
        // same total count as before (e.g. "All Brands + All Types" = 14 → still 14),
        // so the pager text does not change and we simply proceed after the short timeout.
        final String capturedText = pagerTextBefore;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.not(
                            ExpectedConditions.textToBe(pagerInfoLocator, capturedText)));
            log.debug("Pager updated after filter, was: '{}'", capturedText);
        } catch (org.openqa.selenium.TimeoutException e) {
            log.debug("Pager unchanged after 3s (same count expected for this filter combination)");
        }
    }

    /** Selects a brand filter. Option 1 = All Brands, 2 = Net Core, 3 = Others. */
    public void selectBrandFilter(Integer option) throws ElementNotFoundException {
        selectFilter("brand", new String[]{"All Brands", "Net Core", "Others"}, option);
    }

    /** Selects a type filter. Option 1 = All Types, 2 = Mug, 3 = TShirt, 4 = Pin. */
    public void selectTypeFilter(Integer option) throws ElementNotFoundException {
        selectFilter("type", new String[]{"All Types", "Mug", "TShirt", "Pin"}, option);
    }

    /**
     * Returns the total number of catalog items for the current filter by reading the
     * pager info text ("Showing N of <b>M</b> products …"). This avoids navigating
     * between pages for multi-page results.
     */
    public Integer numberCatalogDisplayedItems() {
        // The middle pager span (no id) shows "Showing N of M products - Page X - Y"
        By pagerInfoLocator = By.cssSelector(".esh-pager-item:not([id])");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(pagerInfoLocator), "Pager info not found");
        WebElement pagerInfo = driver.findElement(pagerInfoLocator);
        String text = pagerInfo.getText(); // e.g. "Showing 10 of 14 products - Page 1 - 2"
        log.debug("Pager text: '{}'", text);
        // Extract total count from "… of <total> products …"
        String[] splitOf = text.split(" of ");
        int total = Integer.parseInt(splitOf[1].trim().split(" ")[0]);
        log.debug("Total catalog items: {}", total);
        return total;
    }
}
