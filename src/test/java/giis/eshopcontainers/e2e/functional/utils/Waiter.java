package giis.eshopcontainers.e2e.functional.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Waiter {
    private static final Logger log = LoggerFactory.getLogger(Waiter.class);
    private WebDriverWait waiter;
    public Waiter(WebDriver driver){
       waiter= new WebDriverWait(driver, Duration.ofSeconds(1));
    }
    public void waitUntil(ExpectedCondition condition, String errorMessage){
        try {
            this.waiter.until(condition);
        } catch (org.openqa.selenium.TimeoutException timeout) {
            log.error(errorMessage);
            throw new org.openqa.selenium.TimeoutException("\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
        }
    }
}
