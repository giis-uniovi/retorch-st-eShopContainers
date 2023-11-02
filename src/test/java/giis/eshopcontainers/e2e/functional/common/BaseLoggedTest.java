package giis.eshopcontainers.e2e.functional.common;

import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.junit.jupiter.api.*;

import static java.lang.invoke.MethodHandles.lookup;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.slf4j.LoggerFactory.getLogger;

import org.openqa.selenium.logging.LogEntries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class BaseLoggedTest {
    protected static final Class<? extends WebDriver> chrome = ChromeDriver.class;
    protected static final Class<? extends WebDriver> firefox = FirefoxDriver.class;
    public static final Logger log= LoggerFactory.getLogger(BaseLoggedTest.class);
    public static final String CHROME = "chrome";
    protected static String SUT_URL;
    protected static Properties properties;
    public WebDriver driver;
    public static String CLIENT_BROWSER;
    public static String LOCALHOST = "http://156.35.119.57:5100";
    protected BrowserUser user;
    protected static String TEST_NAME = "DEFAULT";
    protected static String TJOB_NAME = "DEFAULTTJOB";

    @Rule
    public TestName testName;

    @BeforeAll()
    static void setupAll() { //28 lines
        properties = new Properties();
        try {
            // load a properties file for reading
            properties.load(new FileInputStream("src/test/resources/inputs/test.properties"));
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
        driver = user.getDriver();
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
    protected void logout(BrowserUser user) { //43 lines
        log.info("Logging out");
    }
    @AfterEach
    void tearDown() { //13 lines

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
