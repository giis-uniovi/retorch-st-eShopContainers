package giis.eshopcontainers.e2e.functional.common;

import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import static org.openqa.selenium.logging.LogType.BROWSER;

/*
 * This class contains common set-up, tear-down, browser setup, login, and logout methods utilized across various
 * test cases within the test suite. All classes implementing test cases inherit from this class to execute
 * consistent set-up and tear-down procedures before each case. Additionally, it provides common clearance and
 * preparation methods shared among the test cases.
 */
public class BaseLoggedClass {

    public static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);
    protected static String sutUrl;
    protected static String tjobName = "DEFAULT_TJOB";
    protected static Properties properties;
    protected BrowserUser user;

    @BeforeAll()
    static void setupAll() throws IOException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        // load a properties file for reading
        properties.load(Files.newInputStream(Paths.get("src/test/resources/inputs/test.properties")));
        if (System.getProperty("SUT_URL") == null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
            log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: " + sutUrl);
        } else {
            sutUrl = "http://" + System.getProperty("SUT_URL") + ":" + System.getProperty("SUT_PORT") + "/";
            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: " + sutUrl);
        }
        log.info("Ending global setup for all test cases.");
    }

    @BeforeEach
    void setup(TestInfo testInfo) throws MalformedURLException { //65 lines
        log.info("Starting Individual Set-up for the test:" + testInfo.getDisplayName() + ".");
        tjobName = System.getProperty("tjob_name");
        user = setupBrowser(testInfo.getDisplayName(), 1);
        log.info("Individual Set-up finished, starting test" + testInfo.getDisplayName() + "...");
    }

    protected BrowserUser setupBrowser(String testName,
                                       int secondsOfWait) throws MalformedURLException {
        log.debug("Starting browser ({})", properties.getProperty("BROWSER_USER"));
        BrowserUser browserUser = new ChromeUser(secondsOfWait, testName, properties.getProperty("USER_ESHOP"),properties.getProperty("USER_ESHOP_PASSWORD") );
        log.debug("Navigating to {}", sutUrl);
        browserUser.getDriver().get(sutUrl);
        return browserUser;
    }

    /**
     * Navigates to the main menu to enter a user session.
     * @param user The user with its remote Web Driver.
     */
    protected void login(BrowserUser user) throws ElementNotFoundException {
        Navigation.toMainMenu(user);

        user.setIsLogged(true);
        log.debug("Logging in user {} ", user.getUserId());
        user.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Login')]")), "The button searched by xPath //a[contains(text(),'Login')] is not clickable");
        Click.element(user, user.getDriver().findElement(By.xpath("//a[contains(text(),'Login')]")));

        user.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")), "The Username login field is not present");
        WebElement userNameField = user.getDriver().findElement(By.id("Username"));
        user.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Password")), "The Password login is not present");
        WebElement userPassField = user.getDriver().findElement(By.id("Password"));
        userNameField.sendKeys(user.getUserId());
        userPassField.sendKeys(user.getPassword());

        user.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Login')]")), "The button searched by xpath //button[contains(.,'Login')] is not clickable");
        Click.element(user, user.getDriver().findElement(By.xpath("//button[contains(.,'Login')]")));
        log.debug("Logging in successful for user {}", user.getUserId());
    }

    /**
     * Navigates to the main menu and log-outs the user in session
     */
    protected void logout(BrowserUser user) throws ElementNotFoundException { //43 lines
        Navigation.toMainMenu(user);
        WebElement elementOfInterest;
        try {
            //trying to get the "select option"
            elementOfInterest = user.getDriver().findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        } catch (NoSuchElementException e) {
            //so options are not visible, meaning we need to click first
            WebElement mainMenu = user.getDriver().findElement(By.className("esh-identity-drop"));
            Click.element(user, mainMenu);
            //good idea would be to put "wait for element" here
            elementOfInterest = mainMenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        }
        //this would select the option
        Click.element(user, elementOfInterest);
        user.setIsLogged(false);
        log.debug("Logging out");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws ElementNotFoundException { //13 lines
        if (user != null) {
            log.info("Finish test: {} - Driver {}", testInfo.getDisplayName(), this.user.getDriver());
            log.debug("Browser console at the end of the test");
            LogEntries logEntries = user.getDriver().manage().logs().get(BROWSER);
            logEntries.forEach((entry) -> log.info("[{}] {} {}",
                    new Date(entry.getTimestamp()), entry.getLevel(),
                    entry.getMessage()));
            if (user.isLogged()) {
                this.logout(user);
            }
            log.info("Disposing user and releasing/closing browser");
            user.dispose();
        }

    }

}
