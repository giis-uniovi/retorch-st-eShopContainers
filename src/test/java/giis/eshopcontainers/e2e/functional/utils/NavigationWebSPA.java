package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Navigation helpers for the WebSPA frontends.
 * Some methods override the current implemented in Navigation, the inheritance is
 * planned for method sharing (e.g. those that use the same selectors).
 */
public class NavigationWebSPA extends Navigation {
    public static final Logger log = LoggerFactory.getLogger(NavigationWebSPA.class);

    @Override
    protected By getMainMenuBy(){return By.className("esh-app-header-brand");}

    /**
     * Navigates to the Orders page by hovering over the identity container to expand the CSS dropdown,
     * then clicking "My orders". The dropdown uses CSS :hover on .esh-identity to expand
     * .esh-identity-drop from height:0 to height:5rem, so we hover the parent, not the drop itself.
     */
    @Override
    public void toOrdersPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenu(driver, waiter);
        log.debug("Navigating to orders page (WebSPA), hovering identity and clicking My orders...");
        // Wait for .esh-identity-drop to confirm authenticated (rendered via *ngIf="authenticated")
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.className("esh-identity-drop")),
                "Identity drop not found (user not authenticated)");
        clickIdentityMenuItem(driver, waiter, "My orders");
        waiter.waitUntil(ExpectedConditions.urlContains("orders"), "Orders page did not load");
    }

    /**
     * Clicks an item of the identity dropdown ("My orders", "Log Out") defined in identity.html.
     * The dropdown expands with CSS :hover on .esh-identity (from height:0), so we hover the visible user name.
     * The grid browser sometimes drops the :hover state (e.g. cursor already resting over the element), so the
     * cursor is moved away and back, retrying a few times before falling back to a JS click: the item is in the
     * DOM (rendered by *ngIf="authenticated") even while the dropdown is collapsed.
     */
    public static void clickIdentityMenuItem(WebDriver driver, Waiter waiter, String label) throws ElementNotFoundException {
        By identityNameLocator = By.cssSelector(".esh-identity > .esh-identity-section .esh-identity-name");
        By itemLocator = By.xpath("//*[contains(@class,'esh-identity-item')]//*[normalize-space(text())='" + label + "']");
        boolean visible = false;
        for (int attempt = 0; attempt < 3 && !visible; attempt++) {
            Actions actions = new Actions(driver);
            actions.moveToElement(driver.findElement(By.className("esh-app-header-brand"))).perform();
            actions.moveToElement(driver.findElement(identityNameLocator)).perform();
            try {
                new WebDriverWait(driver, Duration.ofSeconds(4))
                        .until(ExpectedConditions.visibilityOfElementLocated(itemLocator));
                visible = true;
            } catch (TimeoutException e) {
                log.debug("'{}' not visible after hover, retrying ({})...", label, attempt + 1);
            }
        }
        WebElement item = driver.findElement(itemLocator);
        if (visible) {
            Click.element(driver, waiter, item);
        } else {
            log.debug("Hover did not expand the dropdown, clicking '{}' through JavaScript", label);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", item);
        }
    }

    /**
     * Navigates to the basket page by clicking the basket icon in the header of the WebSPA frontend.
     */
    @Override
    public void navigateToBasket(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to basket page (WebSPA)...");
        By basketLocator = By.className("esh-basketstatus");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(basketLocator), "Basket icon is not clickable");
        Click.element(driver, waiter, driver.findElement(basketLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("basket"), "Basket page did not load");
    }

    /**
     * Navigates to the Orders checkout through the basket button in the WebSPA frontend.
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