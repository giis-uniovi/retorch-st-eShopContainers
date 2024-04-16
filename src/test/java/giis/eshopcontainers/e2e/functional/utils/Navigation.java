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
 * Class with various methods facilitating navigation within the web UI of the application: return to the main page
 */
public class Navigation {
    public static final Logger log = LoggerFactory.getLogger(Navigation.class);

    /**
     * Click on the top logo to return to the main menu.
     * @param waiter waiter of the remote web driver
     * @param driver remote web driver to perform the action
     */
    public static void toMainMenu(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to main menu, clicking logo...");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img")), "The menu image its not visible");
        Click.element(driver, waiter, driver.findElement(By.xpath("//img")));

    }

    /**
     * Navigates to the Orders menu
     * @param waiter waiter of the remote web driver
     * @param driver remote web driver to perform the action
     */
    public static void toOrdersPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenu(driver, waiter);
        log.debug("Navigating to orders page, clicking in the menu...");
        Navigation.toMainMenu(driver, waiter);
        WebElement ordersButton;
        try {
            // Attempt to locate the logout link directly
            ordersButton = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div"));
            log.debug("The menu was opened, getting the Orders button");
        } catch (NoSuchElementException e) {
            log.debug("Menu don't opened, clicking on it for then press the orders button...");
            // If the logout link is not visible, click the main menu to reveal it
            WebElement mainMenu = driver.findElement(By.className("esh-identity-drop"));
            Click.element(driver, waiter, mainMenu);
            // Wait for the logout link to become visible
            waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div")), "Orders link is not visible after expanding the menu");
            ordersButton = mainMenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[1]/div"));
        }
        Click.element(driver, waiter, ordersButton);
    }
}
