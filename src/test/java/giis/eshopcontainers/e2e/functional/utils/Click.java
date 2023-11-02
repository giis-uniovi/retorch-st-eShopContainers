package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedTest;
import giis.eshopcontainers.e2e.functional.common.BrowserUser;
import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

public class Click {
    static final Logger log = BaseLoggedTest.log;
    public static BrowserUser element(BrowserUser user, WebElement ele) throws ElementNotFoundException {

        String tagName = ele.getTagName();
        String text = ele.getText();

        try {
            user.waitUntil(ExpectedConditions.elementToBeClickable(ele),"Element 1 not clickable");
            ele.click();
            log.info("Click.element (click): ele:" + tagName + ":" + text + " ==>OK");
            return user;
        } catch (Exception e) {
            log.error("Click.element (click): ele:" + tagName + ":" + text + " ==>KO " + e.getClass().getName() + ":" + e.getLocalizedMessage());
        }


        try {
            byJS(user, ele);
            log.info("Click.element (ByJs): ele:" + tagName + ":" + text + " ==>OK");
            return user;
        } catch (Exception e) {
            log.error("Click.element (ByJs): ele:" + tagName + ":" + text + " ==>KO " + e.getClass().getName() + ":" + e.getLocalizedMessage());
        }

        throw new ElementNotFoundException("Click.element ERROR");
    }
    public static void byJS(BrowserUser user, WebElement we) {
        JavascriptExecutor js = (JavascriptExecutor) user.getDriver();
        js.executeScript("var evt = document.createEvent('MouseEvents');"
                + "evt.initMouseEvent('click',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);"
                + "arguments[0].dispatchEvent(evt);", we);
    }

}
