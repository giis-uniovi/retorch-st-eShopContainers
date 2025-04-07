package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasket;

class CatalogTests extends BaseLoggedClass {

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30,sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("AddProductsToBasket")
    void addProductsToBasket() throws ElementNotFoundException {
        log.debug("Before login, checking that the product buttons are disabled");
        // Verify that the product cup button is disabled before login
        checkProductButtonDisabled();
        // Perform login
        this.login();
        // Add products to the basket
        addProductToBasket(1, "NetCore Cup", driver, waiter);
        addProductToBasket(3, "Hoodie", driver, waiter);
        addProductToBasket(6, "Pin", driver, waiter);
        // Perform logout
        this.logout();
        // Verify that the product cup button is disabled after logout
        checkProductButtonDisabled();
    }

    /**
     * Checks that the product buttons are disabled.
     */
    private void checkProductButtonDisabled() throws ElementNotFoundException {
        //Navigate to main menu.
        Navigation.toMainMenu(driver, waiter);
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
    @DisplayName("FilterProductsByBrand")
    void FilterProductsByBrandType() throws ElementNotFoundException {
        // Define test data
        int[] brands = {1, 2, 3};
        int[] types = {1, 2, 3, 4};
        int[][] expectedNumItems = {{14, 4, 7, 3}, {7, 2, 3, 2}, {7, 2, 4, 1}};
        // Iterate over brands
        for (int brand : brands) {
            selectBrandFilter(brand);
            // Iterate over types
            for (int type : types) {
                selectTypeFilter(type);
                // Verify the number of displayed items
                String brandName = new String[]{"All Brands", "Net Core", "Others"}[brand - 1];
                String typeName = new String[]{"All Types", "Mug", "TShirt", "Pin"}[type - 1];
                int expectedItems = expectedNumItems[brand - 1][type - 1];
                Assertions.assertEquals(expectedItems, numberCatalogDisplayedItems(),
                        "Brand: " + brandName + ", Type: " + typeName + ", Expected Items: " + expectedItems);
            }
        }
    }

    /**
     * Selects a filter option for a given filter on the eShopOnContainers catalog.
     * @param filterId      The ID of the filter element.
     * @param filterOptions Array of display names for filter options.
     * @param option        The selected option for the filter.
     */
    public void selectFilter(String filterId, String[] filterOptions, Integer option) throws ElementNotFoundException {
        WebElement elementOfInterest;
        try {
            elementOfInterest = driver.findElement(By.xpath("//*[@id=\"" + filterId + "\"]/option[" + option + "]"));
        } catch (NoSuchElementException e) {
            WebElement mainMenu = driver.findElement(By.id(filterId));
            Click.element(driver, waiter, mainMenu);
            elementOfInterest = mainMenu.findElement(By.xpath("//*[@id=\"" + filterId + "\"]/option[" + option + "]"));
        }
        log.debug("Selecting the {} : {}", filterId, filterOptions[option - 1]);
        Click.element(driver, waiter, elementOfInterest);
        log.debug("Click the Filter Apply button");
        WebElement filterApplyButton = driver.findElement(By.xpath("/html/body/section[2]/div/form/input[1]"));
        Click.element(driver, waiter, filterApplyButton);
    }

    /**
     * Selects a brand filter option for the eShopOnContainers catalog.
     * @param option The selected brand filter option: 1) All brands, 2)NETCore and 3) Others
     */
    public void selectBrandFilter(Integer option) throws ElementNotFoundException {
        String[] brandOptions = {"All Brands", "Net Core", "Others"};
        selectFilter("BrandFilterApplied", brandOptions, option);
    }

    /**
     * Selects a type filter option for the eShopOnContainers catalog.
     * @param option The selected type filter option: 1) All Types, 2) Mug, 3) TShirt and 4)Pin
     */
    public void selectTypeFilter(Integer option) throws ElementNotFoundException {
        String[] typeOptions = {"All Types", "Mug", "TShirt", "Pin"};
        selectFilter("TypesFilterApplied", typeOptions, option);
    }

    /**
     * Get the number of displayed items in the catalog, counting also the items in the second page (if exist).
     */
    public Integer numberCatalogDisplayedItems() throws ElementNotFoundException {
        int totalItems = 0;

        log.debug("Checking the visibility of the Next button");
        WebElement nextButton = driver.findElement(By.id("Next"));

        if (nextButton.isDisplayed()) {
            // Count items on the current page
            totalItems += driver.findElements(By.className("esh-catalog-item")).size();
            log.debug("Clicking to go to the next page. The total number of items on the first page is: {}", totalItems);
            Click.element(driver, waiter, nextButton);
            // Count items on the next page
        } else {
            log.debug("Next button not visible. Counting only the elements on the current page.");
        }
        totalItems += driver.findElements(By.className("esh-catalog-item")).size();

        log.debug("The total number of items is: {}", totalItems);
        return totalItems;
    }

}