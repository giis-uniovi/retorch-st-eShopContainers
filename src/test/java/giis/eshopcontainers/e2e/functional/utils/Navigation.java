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
 * The variants required for the WebSPA are addressed overriding in the
 * {@code NavigationWebSPA} class.
 */
public class Navigation {
    public static final Logger log = LoggerFactory.getLogger(Navigation.class);

    protected By getMainMenuBy(){return By.xpath("//img");}
    protected By getCatalogItemBy(){return By.className("esh-catalog-item");}


    /**
     * Returns to the catalog home page by clicking the top logo image, whereas also counts
     * that more than one item in the catalog is displayed.
     */
    public void toMainMenu(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to main menu, clicking logo...");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(getMainMenuBy()), "The menu image is not visible");
        Click.element(driver, waiter, driver.findElement(getMainMenuBy()));
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(getCatalogItemBy(), 0),
                "Catalog items did not appear after navigating to main menu");
    }

    /**
     * Navigates to the Orders page via the WebMVC identity drop-down menu.
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    public void toOrdersPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to orders page (WebMVC)...");
        toMainMenu(driver, waiter);
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

    /**
     * Navigates to the checkout page and checks that the total amount of money for being paid is the expected one
     * @param driver         {@code WebDriver} on which the operations are performed.
     * @param waiter         {@code Waiter} to perform the necessary async waits.
     */
    public void navigateToCheckout(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        WebElement menuOrder = driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver, waiter, menuOrder);
        //Click into the Checkout button
        WebElement buttonCheckout = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonCheckout);
    }

    /**
     * Navigates to the basket page by clicking the basket icon in the header.
     */
    public void navigateToBasket(WebDriver driver,Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to basket page by clicking the basket icon...");
        WebElement basketIcon = driver.findElement(
                By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver, waiter, basketIcon);
    }
}