package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation helpers for the WebSPA frontends.
 * Some methods override the current implemented in Navigation, the inheritance is
 * planned for method sharing (e.g. those that use the same selectors)
 */
public class NavigationWebSPA extends Navigation {
    public static final Logger log = LoggerFactory.getLogger(NavigationWebSPA.class);

    @Override
    protected By getMainMenuBy(){return By.className("esh-app-header-brand");}

    /**
     * Navigates to the Orders page.
     * The {@code .esh-identity-drop} section is always rendered when authenticated
     * so no explicit dropdown expansion is needed.
     */
    @Override
    public void toOrdersPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenu(driver, waiter);
        log.debug("Navigating to orders page (WebSPA), hovering identity and clicking My orders...");
        // The .esh-identity-drop is CSS hover-triggered; Actions hover then click
        // simulates real user behavior better than a raw JS click.
        By identityDropLocator = By.className("esh-identity-drop");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(identityDropLocator),
                "Identity drop not found");
        WebElement identityDrop = driver.findElement(identityDropLocator);
        new Actions(driver)
                .moveToElement(identityDrop)
                .perform();
        By myOrdersLocator = By.xpath(
                "//*[contains(@class,'esh-identity-item')]//*[normalize-space(text())='My orders']");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(myOrdersLocator),
                "'My orders' link not visible after hover");
        Click.element(driver, waiter, driver.findElement(myOrdersLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("orders"), "Orders page did not load");
    }

    /**
     * Navigates to the Orders checkout through the basket button.
     */
    @Override
    public void navigateToCheckout(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        By basketLocator = By.className("esh-basketstatus");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(basketLocator), "Basket icon is not clickable");
        Click.element(driver, waiter, driver.findElement(basketLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("basket"), "Basket page did not load");
        By checkoutLocator = By.xpath("//button[normalize-space(text())='Checkout']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(checkoutLocator), "Checkout button is not clickable");
        Click.element(driver, waiter, driver.findElement(checkoutLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("order"), "Checkout form did not load");
    }
}