package giis.eshopcontainers.e2e.functional.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/** This class provides the necessary synchronous and asynchronous wait methods to make
 * the implicit and explicit waits for the web elements.*/
public class Waiter {
    private static final Logger log = LoggerFactory.getLogger(Waiter.class);
    private final WebDriverWait waiter;

    public Waiter(WebDriver driver){
       waiter= new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** This method provides a generic waiter which allows the necessary asynchronous waits for
     * the different webpage elements
     *
     * @param condition the condition that the waiter should asynchronously wait.
     * @param errorMessage the error message that is displayed if the condition is not met.
     * */
    public void waitUntil(ExpectedCondition <?> condition, String errorMessage){
        try {
            this.waiter.until(condition);
        } catch (org.openqa.selenium.TimeoutException timeout) {
            log.error(errorMessage);
            throw new org.openqa.selenium.TimeoutException("\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
        }
    }
}
