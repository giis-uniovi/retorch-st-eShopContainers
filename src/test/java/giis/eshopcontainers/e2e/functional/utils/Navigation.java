package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.BrowserUser;
import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import org.openqa.selenium.By;

public class Navigation {
    /**
     * Click on the top logo to return to the main menu.
     * @param user Browser user with the remote web driver to perform the action
     */
    public static BrowserUser toMainMenu(BrowserUser user) throws ElementNotFoundException {
        Click.element(user, user.getDriver().findElement(By.xpath("//img")));

        return user;
    }

}
