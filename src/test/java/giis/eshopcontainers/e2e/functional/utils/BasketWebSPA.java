package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * This class extends {@code Basket} with the necessary overrides SPA-specific selectors and interactions
 * to interact with the frontend.
 */
public class BasketWebSPA extends Basket {
    public static final Logger log = LoggerFactory.getLogger(BasketWebSPA.class);

    public BasketWebSPA() {
        navUtils = new NavigationWebSPA();
    }

    /**
     * Adds the Nth catalog product to the basket using the click handler.
     * In the SPA each {@code .esh-catalog-item} div triggers {@code addToCart(item)} on click.
     * Verifies the basket badge increments by one after the click.
     *
     * @param driver      {@code WebDriver} on which the operations are performed.
     * @param waiter      {@code Waiter} to perform the necessary async waits.
     * @param numProduct  1-based position of the product in the catalog grid
     * @param productName product name (used only for log / assertion messages)
     */
    @Override
    public void addProductToBasket(WebDriver driver, Waiter waiter, Integer numProduct, String productName) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItems(driver, waiter);
        log.debug("Adding product (SPA): {}", productName);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), numProduct - 1), "Not enough catalog items loaded for position " + numProduct);
        List<WebElement> catalogItems = driver.findElements(By.className("esh-catalog-item"));
        WebElement item = catalogItems.get(numProduct - 1);
        Assertions.assertFalse(Objects.requireNonNull(item.getAttribute("class")).contains("is-disabled"), "Product '" + productName + "' is disabled — is the user logged in?");
        Click.element(driver, waiter, item);
        int expected = numItemsPriorAdd + 1;
        waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), String.valueOf(expected)), "Basket count did not increase to " + expected + " after adding '" + productName + "'");
        log.debug("Product '{}' correctly added (SPA)!", productName);
    }

    /**
     * Support method that enables the catalog navigation the checking that all visible products items are disabled
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    @Override
    public void checkProductButtonDisabled(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toMainMenu(driver, waiter);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 4), "Expected more than 4 catalog items");
        WebElement firstItem = driver.findElement(By.className("esh-catalog-item"));
        Assertions.assertTrue(Objects.requireNonNull(firstItem.getAttribute("class")).contains("is-disabled"), "Catalog item should be disabled when not logged in, class was: " + firstItem.getAttribute("class"));
    }

    /**
     * Selects an option from the brand or type dropdown by passing 1-based, for then apply the filter and
     * wait for the catalog reloading.
     *
     * @param driver        {@code WebDriver} on which the operations are performed.
     * @param waiter        {@code Waiter} to perform the necessary async waits.
     * @param filterId      the HTML {@code id} attribute of the selector.
     * @param filterOptions display names for log messages (1-based)
     * @param option        1-based option index
     */
    @Override
    public void selectFilter(WebDriver driver, Waiter waiter, String filterId, String[] filterOptions, Integer option) throws ElementNotFoundException {
        By pagerInfoLocator = By.cssSelector(".esh-pager-item:not([id])");
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

        final String capturedText = pagerTextBefore;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.not(ExpectedConditions.textToBe(pagerInfoLocator, capturedText)));
            log.debug("Pager updated after filter, was: '{}'", capturedText);
        } catch (org.openqa.selenium.TimeoutException e) {
            log.debug("Pager unchanged after 3s (same count expected for this filter combination)");
        }
    }

    @Override
    protected String getBrandFilterId() { return "brand"; }

    @Override
    protected String getTypeFilterId() { return "type"; }

    /**
     * Returns the total number of catalog items for the current filter by reading the
     * pager info text ("Showing N of <b>M</b> products …"), avoiding the multipage navigation.
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    @Override
    public Integer numberCatalogDisplayedItems(WebDriver driver, Waiter waiter) {
        By pagerInfoLocator = By.cssSelector(".esh-pager-item:not([id])");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(pagerInfoLocator), "Pager info not found");
        WebElement pagerInfo = driver.findElement(pagerInfoLocator);
        String text = pagerInfo.getText();
        log.debug("Pager text: '{}'", text);
        String[] splitOf = text.split(" of ");
        int total = Integer.parseInt(splitOf[1].trim().split(" ")[0]);
        log.debug("Total catalog items: {}", total);
        return total;
    }
}
