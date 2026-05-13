package giis.eshopcontainers.e2e.functional.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for shopping-related helpers shared by both WebMVC and WebSPA.
 * Specific catalog browsing and order operations are delegated to Basket and Orders subclasses.
 */
public class Shopping {
    public static final Logger log = LoggerFactory.getLogger(Shopping.class);
    protected Navigation navUtils;
    protected Basket utils;

    /**
     * Returns the number of items currently shown in the basket badge.
     * Both WebMVC and WebSPA use the same {@code .esh-basketstatus-badge} class.
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    protected Integer getNumShoppingItems(WebDriver driver, Waiter waiter) {
        log.debug("Getting number of Shopping Cart items via badge.");
        By badgeLocator = By.className("esh-basketstatus-badge");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(badgeLocator), "The basket badge is not visible");
        String itemsText = driver.findElement(badgeLocator).getText().trim();
        log.debug("The number of items is: {}", itemsText);
        return Integer.valueOf(itemsText);
    }

    /**
     * Generic form field fill utility that works with any By locator.
     * Waits for field presence, then clears and fills it.
     *
     * @param driver  {@code WebDriver} on which the operations are performed.
     * @param waiter  {@code Waiter} to perform the necessary async waits.
     * @param locator the By locator for the field (e.g., By.id("fieldId") or By.cssSelector("[placeholder='Field']"))
     * @param value   the value to fill in the field
     */
    protected void fillField(WebDriver driver, Waiter waiter, By locator, String value) {
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(locator), "Field is not present: " + locator);
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }
}
