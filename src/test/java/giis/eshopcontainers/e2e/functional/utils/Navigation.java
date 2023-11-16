package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Class with various methods facilitating navigation within the web UI of the application: return to the main page
 */
public class Navigation {
    /**
     * Click on the top logo to return to the main menu.
     * @param driver remote web driver to perform the action
     */
    public static void toMainMenu(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        Click.element(driver,waiter, driver.findElement(By.xpath("//img")));

    }
}
