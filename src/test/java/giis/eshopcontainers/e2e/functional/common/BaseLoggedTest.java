package giis.eshopcontainers.e2e.functional.common;

import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.model.EShopUser;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import static org.openqa.selenium.logging.LogType.BROWSER;

public class BaseLoggedTest {
    protected static final Class<? extends WebDriver> chrome = ChromeDriver.class;
    protected static final Class<? extends WebDriver> firefox = FirefoxDriver.class;
    public static final Logger log= LoggerFactory.getLogger(BaseLoggedTest.class);
    public static final String CHROME = "chrome";
    protected static String SUT_URL;
    protected static Properties properties;
    public static String CLIENT_BROWSER;
    public static String LOCALHOST = "http://156.35.119.57:5100";
    protected BrowserUser user;
    private static EShopUser logged_user;
    protected static String TEST_NAME = "DEFAULT";
    protected static String TJOB_NAME = "DEFAULTTJOB";

    @Rule
    public TestName testName;

    @BeforeAll()
    static void setupAll() { //28 lines
        properties = new Properties();
        logged_user= new EShopUser("alice","Pass123$");
        try {
            // load a properties file for reading
            properties.load(Files.newInputStream(Paths.get("src/test/resources/inputs/test.properties")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (System.getenv("SUT_URL") == null) {
            // Outside CI
            ChromeDriverManager.getInstance(chrome).setup();
            FirefoxDriverManager.getInstance(firefox).setup();
        }
        if (System.getenv("SUT_URL") != null) {
            SUT_URL = "https://" + System.getenv("SUT_URL") + ":" + System.getenv("SUT_PORT") + "/";
        } else {
                SUT_URL = LOCALHOST;

        }
        CLIENT_BROWSER = System.getenv("CLIENT_BROWSER");
        if ((CLIENT_BROWSER == null)) {
            CLIENT_BROWSER = CHROME;
        }

    }

    @BeforeEach
    void setup() { //65 lines
        this.testName= new TestName();
        log.info("##### Start test: " + this.testName.getMethodName());
        TJOB_NAME = System.getProperty("dirtarget");

        user = setupBrowser(TJOB_NAME,1);
    }

    protected BrowserUser setupBrowser(String tJobName,
                                        int secondsOfWait) {

        log.info("Starting browser ({})", "Chrome");

        BrowserUser u = new ChromeUser(secondsOfWait, tJobName+"-"+testName.getMethodName());
        log.info("Navigating to {}", SUT_URL);

        u.getDriver().get(SUT_URL);
        final String GLOBAL_JS_FUNCTION = "var s = window.document.createElement('script');"
                + "s.innerText = 'window.MY_FUNC = function(containerQuerySelector) {"
                + "var elem = document.createElement(\"div\");"
                + "elem.id = \"video-playing-div\";"
                + "elem.innerText = \"VIDEO PLAYING\";"
                + "document.body.appendChild(elem);"
                + "console.log(\"Video check function successfully added to DOM by Selenium\")}';"
                + "window.document.head.appendChild(s);";
        u.runJavascript(GLOBAL_JS_FUNCTION);

        return u;
    }

    protected void login(BrowserUser user, boolean slow) throws ElementNotFoundException, InterruptedException {
        user.setOnSession(true);
        log.info("Logging in user {} with mail '{}'", user.getClientData(), logged_user.getEmail());

        user.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Login')]")), "The button searched by xPath //a[contains(text(),'Login')] is not clickable");
        Click.element(user,user.getDriver().findElement(By.xpath("//a[contains(text(),'Login')]")));
        user.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")), "The Username login field is not present");
        WebElement userNameField = user.getDriver().findElement(By.id("Username"));
        user.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Password")), "The Password login is not present");
        WebElement userPassField = user.getDriver().findElement(By.id("Password"));
        if (slow) Thread.sleep(3000);
        userNameField.sendKeys(logged_user.getEmail());
        userPassField.sendKeys(logged_user.getPassword());
        user.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Login')]")), "The button searched by xpath //button[contains(.,'Login')] is not clickable");
        Click.element(user,user.getDriver().findElement(By.xpath("//button[contains(.,'Login')]")));

        log.info("Logging in successful for user {}", user.getClientData());


    }
    protected void logout(BrowserUser user) throws ElementNotFoundException { //43 lines

        Navigation.toMainMenu(user);

        WebElement elementOfInterest;
        try {
            //trying to get the "select option"
            elementOfInterest = user.getDriver().findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));

        } catch (NoSuchElementException e) {
            //so options are not visible, meaning we need to click first
            WebElement mainmenu=user.getDriver().findElement(By.className("esh-identity-drop"));
            Click.element(user,mainmenu);

            //good idea would be to put "wait for element" here
            elementOfInterest = mainmenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
                }
//this would select the option
        Click.element(user,elementOfInterest);
        user.setOnSession(false);
        log.info("Logging out");
    }
    @AfterEach
    void tearDown() throws ElementNotFoundException { //13 lines

        if (user != null) {
            log.info("##### Finish test: {} - Driver {}", TEST_NAME, this.user.getDriver());
            log.info("Browser console at the end of the test");
            LogEntries logEntries = user.getDriver().manage().logs().get(BROWSER);
            logEntries.forEach((entry) -> log.info("[{}] {} {}",
                    new Date(entry.getTimestamp()), entry.getLevel(),
                    entry.getMessage()));
            if (user.isOnSession()) {
                this.logout(user);
            }
            user.dispose();
        }





    }




}
