package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebMVC shopping helpers for basket and catalog operations.
 * Methods target the server-rendered WebMVC markup.
 */
public class Basket extends Shopping {
    public static final Logger log = LoggerFactory.getLogger(Basket.class);

    public Basket() {
        navUtils = new Navigation();
    }

    /**
     * Adds the Nth catalog product to the basket using the WebMVC form-submit button.
     * Verifies the basket badge increments by one after the click.
     *
     * @param numProduct  1-based position of the product on the catalog page
     * @param productName product name (used only for log / assertion messages)
     */
    public void addProductToBasket(WebDriver driver, Waiter waiter, Integer numProduct, String productName) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItems(driver, waiter);
        log.debug("Adding the product: {}", productName);
        WebElement productButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[" + numProduct + "]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productButton.getAttribute("class"), "The eShop product button was expected to be enabled but was disabled");
        Click.element(driver, waiter, productButton);
        Assertions.assertEquals(numItemsPriorAdd + 1, getNumShoppingItems(driver, waiter), "The number of items in the basket doesn't match");
        log.debug("Product: {} correctly added!", productName);
    }

    /**
     * Adds some products to the shopping basket.
     */
    public void addProductsToBasket(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        addProductToBasket(driver, waiter, 2, ".NET Blue Hoodie");
        addProductToBasket(driver, waiter, 4, ".NET Foundation Pin");
        addProductToBasket(driver, waiter, 5, ".NET Foundation T-shirt");
    }

    /**
     * Selects a filter option for a given filter on the MVC frontend eShopOnContainers catalog.
     * @param filterId      The ID of the filter element.
     * @param filterOptions Array of display names for filter options.
     * @param option        The selected option for the filter.
     */
    public void selectFilter(WebDriver driver, Waiter waiter, String filterId, String[] filterOptions, Integer option) throws ElementNotFoundException {
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
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Next")), "Catalog page did not reload after filter apply");
    }

    /**
     * Selects a brand filter option for the eShopOnContainers catalog.
     * @param option The selected brand filter option: 1) All brands, 2)NETCore and 3) Others
     */
    public void selectBrandFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
        String[] brandOptions = {"All Brands", "Net Core", "Others"};
        selectFilter(driver, waiter, "BrandFilterApplied", brandOptions, option);
    }

    /**
     * Selects a type filter option for the eShopOnContainers catalog.
     * @param option The selected type filter option: 1) All Types, 2) Mug, 3) TShirt and 4)Pin
     */
    public void selectTypeFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
        String[] typeOptions = {"All Types", "Mug", "TShirt", "Pin"};
        selectFilter(driver, waiter, "TypesFilterApplied", typeOptions, option);
    }

    /**
     * Get the number of displayed items in the catalog, counting also the items in the second page (if exist).
     */
    public Integer numberCatalogDisplayedItems(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int totalItems = 0;

        log.debug("Checking the visibility of the Next button");
        WebElement nextButton = driver.findElement(By.id("Next"));

        if (nextButton.isDisplayed()) {
            totalItems += driver.findElements(By.className("esh-catalog-item")).size();
            log.debug("Clicking to go to the next page. The total number of items on the first page is: {}", totalItems);
            Click.element(driver, waiter, nextButton);
        } else {
            log.debug("Next button not visible. Counting only the elements on the current page.");
        }
        totalItems += driver.findElements(By.className("esh-catalog-item")).size();

        log.debug("The total number of items is: {}", totalItems);
        return totalItems;
    }

    public void checkProductButtonDisabled(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
    }
}
