package giis.eshopcontainers.e2e.functional.common;

import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import giis.eshopcontainers.e2e.functional.utils.Waiter;
import giis.selema.framework.junit5.LifecycleJunit5;
import giis.selema.manager.SeleManager;
import giis.selema.services.impl.SelenoidService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/*
 * This class contains common set-up, tear-down, browser setup, login, and logout methods utilized across various
 * test cases within the test suite. All classes implementing test cases inherit from this class to execute
 * consistent set-up and tear-down procedures before each case. Additionally, it provides common clearance and
 * preparation methods shared among the test cases.
 */
@ExtendWith(LifecycleJunit5.class)
public class BaseLoggedClass {
    public static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);
    protected static String sutUrl;
    protected static String tjobName = "DEFAULT_TJOB";
    protected static Properties properties;
    protected WebDriver driver;
    protected Waiter waiter;
    private static SeleManager seleManager = new SeleManager();
    private String userName;
    private String password;
    private boolean isLogged = false;

    @BeforeAll()
    static void setupAll() throws IOException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        // load a properties file for reading
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        String envUrl=System.getProperty("SUT_URL");
        String envParameterUrl=System.getenv("SUT_URL");
        if ( envUrl== null & envParameterUrl==null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
            log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: " + sutUrl);
        } else {
            sutUrl = envUrl!=null ? "http://" +envUrl + "/" : "http://" +envParameterUrl + "/";
            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: " + sutUrl);
        }
        setupBrowser();
        log.info("Ending global setup for all test cases.");

    }

    @BeforeEach
    void setup(TestInfo testInfo) { //65 lines
        log.info("Starting Individual Set-up for the test:" + testInfo.getDisplayName() + ".");

        driver = seleManager.getDriver();
        this.waiter = new Waiter(driver);
        tjobName = System.getProperty("tjob_name");
        userName = properties.getProperty("USER_ESHOP");
        password = properties.getProperty("USER_ESHOP_PASSWORD");
        log.debug("Navigating to {}", sutUrl);
        driver.get(sutUrl);

        log.info("Individual Set-up finished, starting test" + testInfo.getDisplayName() + "...");
    }

    protected static void setupBrowser() {
        log.debug("Starting browser ({})", properties.getProperty("BROWSER_USER"));
        seleManager.setBrowser("chrome").setArguments(new String[]{"--start-maximized"});
        if (System.getenv("SELENOID_PRESENT") != null) {
            seleManager.setDriverUrl("http://selenoid:4444/wd/hub").add(new SelenoidService().setVideo().setVnc());
        }
        log.debug("Finishing set-up browser ({})", properties.getProperty("BROWSER_USER"));
    }

    /**
     * Navigates to the main menu to enter a user session.
     */
    protected void login() throws ElementNotFoundException {
        Navigation.toMainMenu(driver, waiter);
        isLogged = true;
        log.debug("Logging in user {} ", userName);
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Login')]")), "The button searched by xPath //a[contains(text(),'Login')] is not clickable");
        Click.element(driver, waiter, driver.findElement(By.xpath("//a[contains(text(),'Login')]")));

        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")), "The Username login field is not present");
        WebElement userNameField = driver.findElement(By.id("Username"));
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Password")), "The Password login is not present");
        WebElement userPassField = driver.findElement(By.id("Password"));
        userNameField.sendKeys(userName);
        userPassField.sendKeys(password);

        waiter.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Login')]")), "The button searched by xpath //button[contains(.,'Login')] is not clickable");
        Click.element(driver, waiter, driver.findElement(By.xpath("//button[contains(.,'Login')]")));
        log.debug("Logging in successful for user {}", userName);
    }

    /**
     * Navigates to the main menu and log-outs the user in session
     */
    protected void logout() throws ElementNotFoundException { //43 lines
        Navigation.toMainMenu(driver, waiter);
        WebElement elementOfInterest;
        try {
            //trying to get the "select option"
            elementOfInterest = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        } catch (NoSuchElementException e) {
            //so options are not visible, meaning we need to click first
            WebElement mainMenu = driver.findElement(By.className("esh-identity-drop"));
            Click.element(driver, waiter, mainMenu);
            //good idea would be to put "wait for element" here
            elementOfInterest = mainMenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        }
        //this would select the option
        Click.element(driver, waiter, elementOfInterest);
        isLogged = false;
        log.debug("Logging out");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws ElementNotFoundException { //13 lines
        log.info("Disposing user and releasing/closing browser for the test" + testInfo.getDisplayName());
        if (isLogged) {
            log.debug("User {0} logged, proceeding to log-out" + this.userName);
            this.logout();
        }
    }

}

