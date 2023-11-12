package giis.eshopcontainers.e2e.functional.common;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/*
This class encompasses special parameterization and capabilities that are common to all types of drivers (e.g., Chrome,
Firefox, Opera...) for creating both local (Selenium Jupyter) and remote (Selenoid) drivers. It verifies the existence
of the environment variable SELENOID_ISPRESENT. If present, it adds the necessary capabilities to instantiate the browser
 with the video recorder
 */
public class BrowserUser {
    private static final Logger log = LoggerFactory.getLogger(BrowserUser.class);
    protected final int timeOfWaitInSeconds;
    protected WebDriver driver;
    protected boolean isLogged;
    protected WebDriverWait waiter;
    protected Map<String, Object> selenoidOptions;
    protected String selenoidIsPresent;
    protected final String userId;

    protected final String password;

    public BrowserUser(int timeOfWaitInSeconds, String testName, String userName, String userPassword) {
        this.timeOfWaitInSeconds = timeOfWaitInSeconds;
        this.isLogged = false;
        this.userId = userName;
        this.password = userPassword;
        log.info("Starting the configuration of the web browser for the test " + testName);
        selenoidIsPresent = System.getenv("SELENOID_PRESENT");
        if (selenoidIsPresent != null) {
            selenoidOptions = new HashMap<>();
            log.info("Using the remote WebDriver (Selenoid)");
            String tjobName = System.getProperty("tjob_name");
            String dateHour = DateTimeFormatter.ofPattern("yy-MM-dd-HH:mm").format(LocalDateTime.now());
            //CAPABILITIES FOR SELENOID RETORCH
            log.debug("Adding all the extra capabilities needed: {testName,enableVideo,enableVNC,name,enableLog,videoName,screenResolution}");
            selenoidOptions.put("name", testName + "-" + dateHour);
            selenoidOptions.put("enableVNC", true);
            selenoidOptions.put("enableVideo", true);
            selenoidOptions.put("videoName", dateHour + "_" + tjobName + "-" + testName + ".mp4");
            selenoidOptions.put("enableLog", true);
            selenoidOptions.put("logName ", dateHour + "-" + tjobName + "-" + testName + ".log");
            selenoidOptions.put("screenResolution", "1920x1080x24");
        }

    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public WebDriverWait getWaiter() {
        return this.waiter;
    }

    public void waitUntil(ExpectedCondition<?> condition, String errorMessage) {
        try {
            this.waiter.until(condition);
        } catch (org.openqa.selenium.TimeoutException timeout) {
            log.error(errorMessage);
            throw new org.openqa.selenium.TimeoutException("\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
        }
    }

    protected void configureDriver() {
        this.waiter = new WebDriverWait(this.driver, Duration.ofSeconds(this.timeOfWaitInSeconds));
    }

    public void dispose() {
        this.driver.quit();
    }

    public Object runJavascript(String script, Object... args) {
        return ((JavascriptExecutor) this.driver).executeScript(script, args);
    }

    public boolean isLogged() {
        return this.isLogged;
    }

    public void setIsLogged(boolean logged) {
        isLogged = logged;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

}
