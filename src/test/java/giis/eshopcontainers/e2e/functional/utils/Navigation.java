package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.BrowserUser;
import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import org.openqa.selenium.By;

public class Navigation {

    public static BrowserUser toMainMenu(BrowserUser user) throws ElementNotFoundException {
           Click.element(user,user.getDriver().findElement(By.xpath("//img")));

    return user;
    }

}
