package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class incorporates support methods for interacting with the web UI through clicking actions.
 */
public class Click {
    private static final Logger log = LoggerFactory.getLogger(Click.class);

    /**
     * Clicks on the specified WebElement using the default method provided by the browser. If that method fails, it attempts to perform the click operation using JavaScript.
     * @param ele  WebElement that is intended to be clicked.
     */
    public static WebDriver element(WebDriver driver,Waiter waiter, WebElement ele) throws ElementNotFoundException {
        String tagName = ele.getTagName();
        String text = ele.getText();

        try {
            waiter.waitUntil(ExpectedConditions.elementToBeClickable(ele), "Element 1 not clickable");
            ele.click();
            log.debug("Click.element (click): ele:{}:{} ==>OK", tagName, text);
            return driver;
        } catch (Exception e) {
            log.error("Click.element (click): ele:{}:{} ==>KO {}:{}", tagName, text, e.getClass().getName(), e.getLocalizedMessage());
        }
        try {
            byJS(driver, ele);
            log.debug("Click.element by JS (click): ele:{}:{} ==>OK", tagName, text);
            return driver;
        } catch (Exception e) {
            log.error("Click.element by JS (click): ele:{}:{} ==>KO {}:{}", tagName, text, e.getClass().getName(), e.getLocalizedMessage());
        }
        throw new ElementNotFoundException("Click.element ERROR");
    }

    public static void byJS(WebDriver driver, WebElement we) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("var evt = document.createEvent('MouseEvents');"
                + "evt.initMouseEvent('click',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);"
                + "arguments[0].dispatchEvent(evt);", we);
    }
}