package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation helpers for both the WebMVC and WebSPA frontends.
 * Methods with the {@code SPA} suffix use Angular-specific selectors; the
 * plain variants target the server-rendered WebMVC markup.
 */
public class Navigation {
    public static final Logger log = LoggerFactory.getLogger(Navigation.class);

    // -----------------------------------------------------------------------
    // WebMVC
    // -----------------------------------------------------------------------

    /**
     * Returns to the catalog home page by clicking the top logo image.
     */
    public static void toMainMenu(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to main menu (WebMVC), clicking logo...");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img")), "The menu image is not visible");
        Click.element(driver, waiter, driver.findElement(By.xpath("//img")));
    }

    /**
     * Navigates to the Orders page via the WebMVC identity drop-down menu.
     */
    public static void toOrdersPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenu(driver, waiter);
        log.debug("Navigating to orders page (WebMVC)...");
        Navigation.toMainMenu(driver, waiter);
        WebElement ordersButton;
        try {
            ordersButton = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div"));
            log.debug("The menu was opened, getting the Orders button");
        } catch (NoSuchElementException e) {
            log.debug("Menu not opened, clicking to expand then pressing the orders button...");
            WebElement mainMenu = driver.findElement(By.className("esh-identity-drop"));
            Click.element(driver, waiter, mainMenu);
            waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div")),
                    "Orders link is not visible after expanding the menu");
            ordersButton = mainMenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div"));
        }
        Click.element(driver, waiter, ordersButton);
    }

    // -----------------------------------------------------------------------
    // WebSPA
    // -----------------------------------------------------------------------

    /**
     * Returns to the catalog home page by clicking the Angular app header logo.
     */
    public static void toMainMenuSPA(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to main menu (WebSPA), clicking header logo...");
        By logoLocator = By.className("esh-app-header-brand");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(logoLocator), "Header logo is not visible");
        Click.element(driver, waiter, driver.findElement(logoLocator));
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), 0),
                "Catalog items did not appear after navigating to main menu");
    }

    /**
     * Navigates to the Orders page via the WebSPA Angular identity component.
     * The {@code .esh-identity-drop} section is always rendered when authenticated
     * so no explicit dropdown expansion is needed.
     */
    public static void toOrdersPageSPA(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenuSPA(driver, waiter);
        log.debug("Navigating to orders page (WebSPA), hovering identity and clicking My orders...");
        // The .esh-identity-drop is CSS hover-triggered; JS click bypasses the visibility guard.
        By myOrdersLocator = By.xpath(
                "//*[contains(@class,'esh-identity-item')]//*[normalize-space(text())='My orders']");
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(myOrdersLocator),
                "'My orders' link not found in DOM");
        Click.byJS(driver, driver.findElement(myOrdersLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("orders"), "Orders page did not load");
    }
}
