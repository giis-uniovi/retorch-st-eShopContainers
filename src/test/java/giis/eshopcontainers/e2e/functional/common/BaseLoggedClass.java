package giis.eshopcontainers.e2e.functional.common;

import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.eshopcontainers.e2e.functional.utils.Navigation;
import giis.eshopcontainers.e2e.functional.utils.Waiter;
import giis.selema.framework.junit5.LifecycleJunit5;
import giis.selema.manager.SeleManager;
import giis.selema.manager.SelemaConfig;
import giis.selema.services.impl.SelenoidService;
import org.junit.jupiter.api.*;
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
import java.sql.*;
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
    protected static String tJobName = "DEFAULT_TJOB";
    protected static Properties properties;
    protected WebDriver driver;
    protected Waiter waiter;
    private static final SeleManager seleManager = new SeleManager(new SelemaConfig().setReportSubdir("target/containerlogs/" + (System.getProperty("tjob_name") == null ? "" : System.getProperty("tjob_name"))).setName(System.getProperty("tjob_name") == null ? "locallogs" : System.getProperty("tjob_name")));
    private String userName;
    private String password;
    private boolean isLogged = false;

    @BeforeAll()
    static void setupAll() throws IOException, SQLDataException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        // load a properties file for reading
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        // Retrieve test job name
        tJobName = System.getProperty("tjob_name");
        String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        if (envUrl == null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
            log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: " + sutUrl);
        } else {
            sutUrl = envUrl + ":" + (System.getProperty("SUT_PORT") != null ? System.getProperty("SUT_PORT") : System.getenv("SUT_PORT")) + "/";
            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: " + sutUrl);
        }
        checkDBMigration();
        checkCatalogDBStatus();
        setupBrowser();
        log.info("Ending global setup for all test cases.");

    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        log.info("Starting Individual Set-up for the test: {}.", testInfo.getDisplayName());

        // Initialize WebDriver and Waiter instances
        driver = seleManager.getDriver();
        waiter = new Waiter(driver);

        // Retrieve user credentials
        userName = properties.getProperty("USER_ESHOP");
        password = properties.getProperty("USER_ESHOP_PASSWORD");
        // Navigate to SUT URL
        log.debug("Navigating to {}.", sutUrl);
        driver.get(sutUrl);

        log.info("Individual Set-up for the TJob {} finished, starting test: {}.", tJobName, testInfo.getDisplayName());
    }

    /**
     * Configures and initializes the browser for testing.
     * <p>
     * The method sets up the browser using SeleManager with necessary arguments if, SELENOID_PRESENT is not set.
     * If Selenoid is present, it  configures the Selenoid service for video recording and VNC support.
     * </p>
     */
    protected static void setupBrowser() {
        String browserUser = properties.getProperty("BROWSER_USER");
        log.debug("Starting browser ({})", browserUser);
        // Setting up browser using selema with the necessary Arguments.
        seleManager.setBrowser("chrome").setArguments(new String[]{"--start-maximized"});
        // Set up Selenoid configuration if Selenoid is present
        if (System.getenv("SELENOID_PRESENT") != null) {
            seleManager.setDriverUrl("http://selenoid:4444/wd/hub").add(new SelenoidService().setVideo().setVnc());
        }

        log.debug("Finished setting up browser ({})", browserUser);
    }

    /**
     * Logs in the user with the specified credentials navigating to the main menu.
     */
    protected void login() throws ElementNotFoundException {
        Navigation.toMainMenu(driver, waiter);
        log.debug("Logging in user: {}", userName);

        // Click the "Login" button
        By loginButtonXPath = By.xpath("//a[contains(text(),'Login')]");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginButtonXPath), "Login button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginButtonXPath));
        // Wait for and locate the Username and Password fields
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Username")), "Username login field is not present");
        WebElement userNameField = driver.findElement(By.id("Username"));
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("Password")), "Password login field is not present");
        WebElement userPassField = driver.findElement(By.id("Password"));
        // Enter credentials
        userNameField.sendKeys(userName);
        userPassField.sendKeys(password);
        // Click the "Login" button
        By loginButtonXPathAfterInput = By.xpath("//button[contains(.,'Login')]");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(loginButtonXPathAfterInput), "Login button is not clickable");
        Click.element(driver, waiter, driver.findElement(loginButtonXPathAfterInput));
        // Verify that the user is logged in as expected
        WebElement loggedUser = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        String actualUserName = loggedUser.getText();
        Assertions.assertEquals(userName, actualUserName,
                String.format("The logged-in user is not the expected user. Expected: %s, Actual: %s", userName, actualUserName));
        // Update the login status
        isLogged = true;

        log.debug("Login successful for user: {}", userName);
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws ElementNotFoundException {
        log.info("Disposing user and releasing/closing browser for the test {}", testInfo.getDisplayName());
        if (isLogged) {
            log.debug("Logging out user: {}", this.userName);
            this.logout();
        }
    }

    /**
     * Logs out the currently logged-in user.
     */
    protected void logout() throws ElementNotFoundException {
        // Navigate to the main menu
        Navigation.toMainMenu(driver, waiter);
        WebElement logoutElement;
        try {
            // Attempt to locate the logout link directly
            logoutElement = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        } catch (NoSuchElementException e) {
            // If the logout link is not visible, click the main menu to reveal it
            WebElement mainMenu = driver.findElement(By.className("esh-identity-drop"));
            Click.element(driver, waiter, mainMenu);
            // Wait for the logout link to become visible
            waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div")),
                    "Logout link is not visible after expanding the menu");
            logoutElement = mainMenu.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[2]/a[2]/div"));
        }
        // Click the logout link
        Click.element(driver, waiter, logoutElement);
        // Update the login status
        isLogged = false;
        log.debug("Logout successful");
    }

    /**
     * Checks if the database migration is complete by attempting to connect and query the number
     * of databases in the master.sys.databases table. Retries the connection and query up to a
     * specified number of times. The number of expected databases are 2 (default) + another 5 databases
     * created by the different services
     */
    protected static void checkDBMigration() throws SQLDataException {

        // Get properties
        String user = properties.getProperty("SQLDB_USER");
        String password = properties.getProperty("SQLDB_PASSWORD");
        String host = "sqldata_" + tJobName;
        // host = "localhost"; // default host

        // Build JDBC URL
        final int MAX_TABLES = 6;
        final int MAX_ITERATIONS = 10;
        final int WAIT_TIME_MS = 5000;

        String query = "SELECT name FROM master.sys.databases";
        String url = "jdbc:sqlserver://" + host + ":1433;Encrypt=True;TrustServerCertificate=True;user=" + user + ";password=" + password;

        int iter = 0;
        boolean found = false;
        while (!found && iter < MAX_ITERATIONS) {
            iter++;
            int numTables = 0;
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    numTables++;
                    if (numTables > MAX_TABLES) {
                        found = true;
                        break;
                    }
                }
            } catch (SQLException e) {
                log.warn("The database is not ready yet or the table cannot be found: {}", e.getMessage());
            }
            if (!found) {
                try {
                    Thread.sleep(WAIT_TIME_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!found) {
            log.error("The databases are not migrated after " + MAX_ITERATIONS + " attempts.");
            throw new SQLDataException("The DB Migration was not successfully done , E2E test execution aborted");
        }
    }
    /**
     * Checks the status of the CatalogDB by attempting to connect and query the number of products
     * in the Catalog table. Retries the connection and query up to a specified number of times.
     */
    protected static void checkCatalogDBStatus() throws SQLDataException {
        String dbName = "Microsoft.eShopOnContainers.Services.CatalogDb";
        String tableName = "Catalog";
        String query = "SELECT COUNT(*) AS numproducts FROM " + tableName;

        // Get properties
        String user = properties.getProperty("SQLDB_USER");
        String password = properties.getProperty("SQLDB_PASSWORD");
        String host = "sqldata_" + tJobName;
        // host = "localhost"; // default host

        // Build JDBC URL
        String url = "jdbc:sqlserver://" + host + ":1433;databaseName=" + dbName + ";Encrypt=True;TrustServerCertificate=True;user=" + user + ";password=" + password;

        // Retry logic
        boolean found = false;
        int iter = 0;
        final int maxIterations = 10;
        while (!found && iter < maxIterations) {
            iter++;
            try (Connection connection = DriverManager.getConnection(url);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int result = rs.getInt("numproducts");
                    log.debug("The number of products in CatalogDB is {}", result);
                    if (result > 0) {
                        found = true;
                        break;
                    }
                } else {
                    log.info("No data available in the Catalog table yet.");
                }
            } catch (SQLException e) {
                log.warn("The Table or the SQL database is not ready, proceeding to wait, its message: {}", e.getMessage());
            }
            try {
                Thread.sleep(5000); // Wait 5 seconds for the next connection and query
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!found) {
            log.error("The database is not ready after " + maxIterations + " attempts.");
            throw new SQLDataException("The Catalog database state its not the expected, E2E test execution aborted");
        }
    }
}