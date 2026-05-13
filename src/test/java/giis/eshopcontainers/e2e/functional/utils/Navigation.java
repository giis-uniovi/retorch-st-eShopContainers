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

    /**
     * Returns to the catalog home page by clicking the top logo image.
     */
    public void toMainMenu(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to main menu (WebMVC), clicking logo...");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img")), "The menu image is not visible");
        Click.element(driver, waiter, driver.findElement(By.xpath("//img")));
    }

    /**
     * Navigates to the Orders page via the WebMVC identity drop-down menu.
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
     * @param priceOrder Price of the selected order products
     */
    public void navigateToCheckout(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        WebElement menuOrder = driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver, waiter, menuOrder);
        // Get the order price and check if it's correct
        //WebElement totalAmountBasket = driver.findElement(By.xpath("//*[@id=\"cartForm\"]/div/div[2]/div[4]/article[2" +
        //        "]/section[2]"));
        //Assertions.assertEquals(priceOrder, totalAmountBasket.getText());
        //Click into the Checkout button
        WebElement buttonCheckout = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonCheckout);
    }
}