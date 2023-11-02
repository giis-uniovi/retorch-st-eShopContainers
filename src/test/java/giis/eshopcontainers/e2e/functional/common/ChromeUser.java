package giis.eshopcontainers.e2e.functional.common;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.ALL;
import static org.openqa.selenium.logging.LogType.BROWSER;

public class ChromeUser extends BrowserUser{
    ChromeOptions options = new ChromeOptions();

    public ChromeUser(int timeOfWaitInSeconds, String testName) {
        super(testName, timeOfWaitInSeconds);
        log.info("Starting the configuration of the web browser");
        log.debug(String.format("The Test names are: %s", testName));


        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(BROWSER, ALL);


        options.setCapability("goog:loggingPrefs", logPrefs);

        //Problems with the max attempt of retry, solved with : https://github.com/aerokube/selenoid/issues/1124 solved with --disable-gpu
        //Problems with flakiness due to screen resolution solved with --start-maximized
        String[] arguments = {"--no-sandbox", "--disable-dev-shm-usage", "--allow-elevated-browser", "--disable-gpu", "--start-maximized"};

        log.debug("Adding the arguments ({})", Arrays.toString(arguments));
        for (String argument : arguments
        ) {
            options.addArguments(argument);
        }

        String eusApiURL = System.getenv("SELENOID_URL");

        log.debug("The URL its ({})", eusApiURL);

        options.setAcceptInsecureCerts(true);
        //This capability is to store the logs of the test case
        log.debug("Added Capabilities of acceptInsecureCerts and ignore alarms");

        if (eusApiURL == null) {
            log.info("Using the Local WebDriver ()");
            this.driver = new ChromeDriver(options);
        } else {
            try {

                Map<String, Object> selenoidOptions = new HashMap<>();
                log.info("Using the remote WebDriver (Selenoid)");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
                log.debug("Adding all the extra capabilities needed: {testName,enableVideo,enableVNC,name,enableLog,videoName,screenResolution}");

                selenoidOptions.put("testName", testName + "_" + format.format(new Date()));
                //CAPABILITIES FOR SELENOID RETORCH
                selenoidOptions.put("enableVideo", true);
                selenoidOptions.put("enableVNC", true);
                selenoidOptions.put("name", testName );

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd-HH:mm");
                LocalDateTime now = LocalDateTime.now();
                String logName = dtf.format(now) + "-" + testName + "-"  + ".log";
                String videoName = dtf.format(now) + "_" + testName  + ".mp4";
                log.debug("The data of this test would be stored into: video name " + videoName + " and the log is " + logName);

                selenoidOptions.put("enableLog", true);
                selenoidOptions.put("logName ", logName);
                selenoidOptions.put("videoName", videoName);

                selenoidOptions.put("screenResolution", "1920x1080x24");

                options.setCapability("selenoid:options", selenoidOptions);

                //END CAPABILITIES FOR SELENOID RETORCH
                log.debug("Configuring the remote WebDriver ");
                RemoteWebDriver remote = new RemoteWebDriver(new URL(eusApiURL), options);
                log.debug("Configuring the Local File Detector");
                remote.setFileDetector(new LocalFileDetector());
                this.driver = remote;
            } catch (MalformedURLException e) {
                throw new RuntimeException("Exception creating eusApiURL", e);
            }
        }
        log.debug("Configure the driver connection timeouts at ({})", this.timeOfWaitInSeconds);
        new WebDriverWait(driver, Duration.ofSeconds(this.timeOfWaitInSeconds));

        log.info("Driver Successfully configured");
        this.configureDriver();
    }

}