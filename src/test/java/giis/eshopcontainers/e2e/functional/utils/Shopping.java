package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shopping helpers for both the WebMVC and WebSPA frontends.
 * Methods with the {@code SPA} suffix use Angular-specific selectors; the
 * plain variants target the server-rendered WebMVC markup.
 */
public class Shopping {
    public static final Logger log = LoggerFactory.getLogger(Shopping.class);

    // -----------------------------------------------------------------------
    // Shared
    // -----------------------------------------------------------------------

    /**
     * Returns the number of items currently shown in the basket badge.
     * Both WebMVC and WebSPA use the same {@code .esh-basketstatus-badge} class.
     */
    private static Integer getNumShoppingItems(WebDriver driver, Waiter waiter) {
        log.debug("Getting number of Shopping Cart items via badge.");
        By badgeLocator = By.className("esh-basketstatus-badge");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(badgeLocator), "The basket badge is not visible");
        String itemsText = driver.findElement(badgeLocator).getText().trim();
        log.debug("The number of items is: {}", itemsText);
        return Integer.valueOf(itemsText);
    }

    // -----------------------------------------------------------------------
    // WebMVC
    // -----------------------------------------------------------------------

    /**
     * Adds the Nth catalog product to the basket using the WebMVC form-submit button.
     * Verifies the basket badge increments by one after the click.
     *
     * @param numProduct 1-based position of the product on the catalog page
     * @param productName product name (used only for log / assertion messages)
     */
    public static void addProductToBasket(Integer numProduct, String productName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItems(driver, waiter);
        log.debug("Adding the product: {}", productName);
        WebElement productButton = driver.findElement(
                By.xpath("/html/body/div/div[3]/div[" + numProduct + "]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productButton.getAttribute("class"),
                "The eShop product button was expected to be enabled but was disabled");
        Click.element(driver, waiter, productButton);
        Assertions.assertEquals(numItemsPriorAdd + 1, getNumShoppingItems(driver, waiter),
                "The number of items in the basket doesn't match");
        log.debug("Product: {} correctly added!", productName);
    }

    // -----------------------------------------------------------------------
    // WebSPA
    // -----------------------------------------------------------------------

    /**
     * Adds the Nth catalog product to the basket using the WebSPA Angular click handler.
     * In the SPA each {@code .esh-catalog-item} div triggers {@code addToCart(item)} on click.
     * Verifies the basket badge increments by one after the click.
     *
     * @param numProduct 1-based position of the product in the catalog grid
     * @param productName product name (used only for log / assertion messages)
     */
    public static void addProductToBasketSPA(Integer numProduct, String productName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItems(driver, waiter);
        log.debug("Adding product (SPA): {}", productName);
        // Wait until at least numProduct items are rendered so get(numProduct-1) is always safe
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.className("esh-catalog-item"), numProduct - 1),
                "Not enough catalog items loaded for position " + numProduct);
        List<WebElement> catalogItems = driver.findElements(By.className("esh-catalog-item"));
        WebElement item = catalogItems.get(numProduct - 1);
        Assertions.assertFalse(item.getAttribute("class").contains("is-disabled"),
                "Product '" + productName + "' is disabled — is the user logged in?");
        Click.element(driver, waiter, item);
        // The badge updates asynchronously after an API call; wait for the expected value.
        int expected = numItemsPriorAdd + 1;
        waiter.waitUntil(
                ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), String.valueOf(expected)),
                "Basket count did not increase to " + expected + " after adding '" + productName + "'");
        log.debug("Product '{}' correctly added (SPA)!", productName);
    }
}
