package giis.eshopcontainers.e2e.functional.common;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

import static java.util.logging.Level.ALL;
import static org.openqa.selenium.logging.LogType.BROWSER;
/*
This class includes special parameterization and capabilities for Chrome WebDrivers.
 */
public class ChromeUser extends BrowserUser {
    private static final Logger log = LoggerFactory.getLogger(ChromeUser.class);

    /**
     * Configures a Chrome web driver for two different scenarios:
     * 1. A remote web driver deployed with Selenoid, if the SELENOID_URL environment variable is present,
     * with the necessary capabilities to record and store the session.
     * 2. A local web driver for performing the E2E test case.
     * @param timeOfWaitInSeconds The timeout to configure the default driver waiter.
     * @param testName            A string containing the test name for video storage.
     */
    public ChromeUser(int timeOfWaitInSeconds, String testName, String userName, String userPassword) throws MalformedURLException {
        super(timeOfWaitInSeconds, testName, userName,userPassword);
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(BROWSER, ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);
        //Problems with the max attempt of retry, solved with : https://github.com/aerokube/selenoid/issues/1124 solved with --disable-gpu
        //Problems with flakiness due to screen resolution solved with --start-maximized
        String[] arguments = {"--no-sandbox", "--disable-dev-shm-usage", "--allow-elevated-browser", "--disable-gpu", "--start-maximized"};
        log.debug("Adding the arguments ({})", Arrays.toString(arguments));
        for (String argument : arguments) {
            options.addArguments(argument);
        }
        options.setAcceptInsecureCerts(true);
        log.debug("Added Capabilities of acceptInsecureCerts and ignore alarms");
        if (selenoidIsPresent == null) {
            log.info("Configuring Chrome WebDriver (Local)");
            this.driver = new ChromeDriver(options);
        } else {
            log.info("Configuring Chrome Remote WebDriver (Selenoid)");
            options.setCapability("selenoid:options", selenoidOptions);
            log.debug("Configuring the remote WebDriver ");
            RemoteWebDriver remote = new RemoteWebDriver(new URL("http://selenoid:4444/wd/hub"), options);
            log.debug("Configuring the Local File Detector");
            remote.setFileDetector(new LocalFileDetector());
            this.driver = remote;
        }
        log.debug("Configure the driver connection timeouts at ({})", this.timeOfWaitInSeconds);
        new WebDriverWait(driver, Duration.ofSeconds(this.timeOfWaitInSeconds));
        log.info("Driver Successfully configured");
        this.configureDriver();
    }

}
