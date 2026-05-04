package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static giis.eshopcontainers.e2e.functional.utils.Navigation.*;
import static giis.eshopcontainers.e2e.functional.utils.Navigation.toOrdersPageSPA;

/**
 * Shopping helpers for both the WebMVC and WebSPA frontends.
 * Methods with the {@code SPA} suffix use the WebSPA selectors; the
 * plain variants target the server-rendered WebMVC markup.
 */
public class Shopping {
    public static final Logger log = LoggerFactory.getLogger(Shopping.class);

    /**
     * Returns the number of items currently shown in the basket badge.
     * Both WebMVC and WebSPA use the same {@code .esh-basketstatus-badge} class.
     */
    private static Integer getNumShoppingItemsMVC(WebDriver driver, Waiter waiter) {
        log.debug("Getting number of Shopping Cart items via badge.");
        By badgeLocator = By.className("esh-basketstatus-badge");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(badgeLocator), "The basket badge is not visible");
        String itemsText = driver.findElement(badgeLocator).getText().trim();
        log.debug("The number of items is: {}", itemsText);
        return Integer.valueOf(itemsText);
    }

    // -----------------------------------------------------------------------
    // WebMVC
    // -----------------------------------------------------------------------

    /**
     * Adds the Nth catalog product to the basket using the WebMVC form-submit button.
     * Verifies the basket badge increments by one after the click.
     *
     * @param numProduct  1-based position of the product on the catalog page
     * @param productName product name (used only for log / assertion messages)
     */
    public static void addProductToBasketMVC(Integer numProduct, String productName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItemsMVC(driver, waiter);
        log.debug("Adding the product: {}", productName);
        WebElement productButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[" + numProduct + "]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productButton.getAttribute("class"), "The eShop product button was expected to be enabled but was disabled");
        Click.element(driver, waiter, productButton);
        Assertions.assertEquals(numItemsPriorAdd + 1, getNumShoppingItemsMVC(driver, waiter), "The number of items in the basket doesn't match");
        log.debug("Product: {} correctly added!", productName);
    }

    // -----------------------------------------------------------------------
    // WebSPA
    // -----------------------------------------------------------------------

    /**
     * Adds the Nth catalog product to the basket using the WebSPA Angular click handler.
     * In the SPA each {@code .esh-catalog-item} div triggers {@code addToCart(item)} on click.
     * Verifies the basket badge increments by one after the click.
     *
     * @param numProduct  1-based position of the product in the catalog grid
     * @param productName product name (used only for log / assertion messages)
     */
    public static void addProductToBasketSPA(Integer numProduct, String productName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int numItemsPriorAdd = getNumShoppingItemsMVC(driver, waiter);
        log.debug("Adding product (SPA): {}", productName);
        // Wait until at least numProduct items are rendered so get(numProduct-1) is always safe
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("esh-catalog-item"), numProduct - 1), "Not enough catalog items loaded for position " + numProduct);
        List<WebElement> catalogItems = driver.findElements(By.className("esh-catalog-item"));
        WebElement item = catalogItems.get(numProduct - 1);
        Assertions.assertFalse(Objects.requireNonNull(item.getAttribute("class")).contains("is-disabled"), "Product '" + productName + "' is disabled — is the user logged in?");
        Click.element(driver, waiter, item);
        // The badge updates asynchronously after an API call; wait for the expected value.
        int expected = numItemsPriorAdd + 1;
        waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), String.valueOf(expected)), "Basket count did not increase to " + expected + " after adding '" + productName + "'");
        log.debug("Product '{}' correctly added (SPA)!", productName);
    }

    /**
     * This method creates an order in the WebSPA frontend, adds three products to the baskets
     */
    public static void createOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toMainMenuSPA(driver, waiter);
        addThreeProductsToBasket(driver, waiter);
        navigateToCheckoutSPA(driver, waiter);
        fillAddressDetails("Campus de Viesques, Edif. Polivalente – D.2.6.06", "Gijon", "Asturias", "Spain", driver, waiter);
        fillPaymentDetails("6271 7012 2597 9642", "Jose Ramon", "03/38", "456", driver, waiter);
        checkOrderItems(3, driver);
        By placeOrderLocator = By.xpath("//button[normalize-space(text())='Place Order']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(placeOrderLocator), "Place Order button is not clickable");
        Click.element(driver, waiter, driver.findElement(placeOrderLocator));
        Assertions.assertTrue(checkOrderPlaced(driver, waiter), "The order was not placed within the allowed attempts");
    }

    private static void addThreeProductsToBasket(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        addProductToBasketSPA(2, ".NET Blue Hoodie", driver, waiter);
        addProductToBasketSPA(4, ".NET Foundation Pin", driver, waiter);
        addProductToBasketSPA(5, ".NET Foundation T-shirt", driver, waiter);
    }

    private static void fillAddressDetails(String street, String city, String state, String country, WebDriver driver, Waiter waiter) {
        fillFormField("[placeholder='Street']", street, driver, waiter);
        fillFormField("[placeholder='City']", city, driver, waiter);
        fillFormField("[placeholder='state']", state, driver, waiter);
        fillFormField("[placeholder='country']", country, driver, waiter);
    }

    private static void fillPaymentDetails(String cardNumber, String cardHolderName, String expirationDate, String secCode, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Filling payment: card={}, holder={}, exp={}", cardNumber, cardHolderName, expirationDate);
        fillFormField("[placeholder='000000000000000']", cardNumber, driver, waiter);
        fillFormField("[placeholder='Card holder']", cardHolderName, driver, waiter);
        fillFormField("[placeholder='MM/YY']", expirationDate, driver, waiter);
        fillFormField("[placeholder='000']", secCode, driver, waiter);
    }

    private static void fillFormField(String cssSelector, String value, WebDriver driver, Waiter waiter) {
        By locator = By.cssSelector(cssSelector);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(locator), "Form field not present: " + cssSelector);
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }

    private static void checkOrderItems(int expectedNumItems, WebDriver driver) {
        List<WebElement> items = driver.findElements(By.cssSelector("article.divider--bottom"));
        Assertions.assertEquals(expectedNumItems, items.size(), "Expected " + expectedNumItems + " order items in checkout summary");
    }

    public static boolean checkOrderPlaced(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int totalAttempts = 5;
        while (totalAttempts > 0) {
            try {
                waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "0"), "Basket value is not 0");
                log.debug("Order placed successfully!");
                return true;
            } catch (TimeoutException e) {
                log.debug("Basket count was not 0 yet, retrying... ({} left)", totalAttempts - 1);
                toMainMenuSPA(driver, waiter);
                totalAttempts--;
            }
        }
        return false;
    }

    // Order list helpers

    public static void checkLastOrderState(List<String> expectedStates, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int maxIterations = 10;
        String actualState = "";
        for (int iter = 0; iter < maxIterations; iter++) {
            log.debug("Iteration {} checking order state", iter);
            toOrdersPageSPA(driver, waiter);
            List<WebElement> listOrders = driver.findElements(By.className("esh-orders-item"));
            if (listOrders.isEmpty()) {
                // Order may not yet be persisted by the messaging pipeline — retry
                log.debug("Iteration {} — orders list is empty, retrying...", iter);
                continue;
            }
            WebElement lastOrder = listOrders.get(listOrders.size() - 1);
            // Columns: 0=number, 1=date, 2=total, 3=status, 4=cancel, 5=details
            WebElement statusElement = lastOrder.findElements(By.tagName("section")).get(3);
            actualState = statusElement.getText();
            log.debug("Iteration {} — order state: {}", iter, actualState);
            if (expectedStates.contains(actualState)) {
                break;
            }
            try {
                waiter.waitUntil(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(statusElement, actualState)), "Order status has not changed yet");
            } catch (Exception ex) {
                log.debug("Status unchanged after wait, retrying...");
            }
        }
        Assertions.assertFalse(actualState.isEmpty(), "No orders appeared in the list after " + maxIterations + " iterations");
        Assertions.assertTrue(expectedStates.contains(actualState), "Last order status is not as expected. Expected: " + expectedStates + ", Actual: " + actualState);
    }

    public static void cancelLastOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        toOrdersPageSPA(driver, waiter);
        List<WebElement> listOrders = driver.findElements(By.className("esh-orders-item"));
        WebElement lastOrder = listOrders.get(listOrders.size() - 1);
        WebElement cancelLink = lastOrder.findElement(By.linkText("Cancel"));
        Click.element(driver, waiter, cancelLink);
    }
}
