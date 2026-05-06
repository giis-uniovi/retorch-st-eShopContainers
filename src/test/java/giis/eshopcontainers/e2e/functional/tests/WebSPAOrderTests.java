package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedList;
import java.util.List;

import static giis.eshopcontainers.e2e.functional.utils.Navigation.toMainMenuSPA;
import static giis.eshopcontainers.e2e.functional.utils.Navigation.toOrdersPageSPA;
import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasketSPA;

/**
 * Validates order creation and cancellation flows in the WebSPA (Angular) frontend.
 * The checkout form uses placeholder-based locators instead of id-based ones.
 * Basket isolation is handled by the parent {@code @BeforeEach clearUserBasket()}.
 */
class WebSPAOrderTests extends BaseWebSPALoggedClass {

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "payment-api", concurrency = 20, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testCreateNewOrderSPA")
    void testCreateNewOrderSPA() throws ElementNotFoundException {
        LinkedList<String> expectedStates = new LinkedList<>();
        expectedStates.add("submitted");
        expectedStates.add("paid");

        login();
        toOrdersPageSPA(driver, waiter);
        createOrder();
        checkLastOrderState(expectedStates);
        logout();
    }

    @AccessMode(resID = "webspa", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "ordering-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "payment-api", concurrency = 20, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testCancelOrderSPA")
    void testCancelOrderSPA() throws ElementNotFoundException {
        LinkedList<String> expectedStatesPriorCancelling = new LinkedList<>();
        expectedStatesPriorCancelling.add("submitted");
        expectedStatesPriorCancelling.add("stockconfirmed");
        LinkedList<String> expectedStatesPostCancelling = new LinkedList<>();
        expectedStatesPostCancelling.add("cancelled");
        LinkedList<String> expectedStatesBeforeLongDelay = new LinkedList<>();
        expectedStatesBeforeLongDelay.add("paid");

        login();
        toOrdersPageSPA(driver, waiter);
        createOrder();
        long startTime = System.currentTimeMillis();
        checkLastOrderState(expectedStatesPriorCancelling);
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Time invested in placing the order: {}ms", duration);
        if (duration <= 3000) {
            cancelLastOrder();
            checkLastOrderState(expectedStatesPostCancelling);
        } else {
            checkLastOrderState(expectedStatesBeforeLongDelay);
        }
        logout();
    }

    // -----------------------------------------------------------------------
    // Order flow helpers
    // -----------------------------------------------------------------------

    private void createOrder() throws ElementNotFoundException {
        toMainMenuSPA(driver, waiter);
        addProductsToBasket();
        navigateToCheckout();
        fillAddressDetails("Campus de Viesques, Edif. Polivalente – D.2.6.06", "Gijon", "Asturias", "Spain");
        fillPaymentDetails("6271 7012 2597 9642", "Jose Ramon", "03/38", "456");
        checkOrderItems(3);
        By placeOrderLocator = By.xpath("//button[normalize-space(text())='Place Order']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(placeOrderLocator),
                "Place Order button is not clickable");
        Click.element(driver, waiter, driver.findElement(placeOrderLocator));
        Assertions.assertTrue(checkOrderPlaced(), "The order was not placed within the allowed attempts");
    }

    private void addProductsToBasket() throws ElementNotFoundException {
        addProductToBasketSPA(2, ".NET Blue Hoodie", driver, waiter);
        addProductToBasketSPA(4, ".NET Foundation Pin", driver, waiter);
        addProductToBasketSPA(5, ".NET Foundation T-shirt", driver, waiter);
    }

    private void navigateToCheckout() throws ElementNotFoundException {
        By basketLocator = By.className("esh-basketstatus");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(basketLocator),
                "Basket icon is not clickable");
        Click.element(driver, waiter, driver.findElement(basketLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("basket"), "Basket page did not load");
        By checkoutLocator = By.xpath("//button[normalize-space(text())='Checkout']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(checkoutLocator),
                "Checkout button is not clickable");
        Click.element(driver, waiter, driver.findElement(checkoutLocator));
        waiter.waitUntil(ExpectedConditions.urlContains("order"), "Checkout form did not load");
    }

    private void fillAddressDetails(String street, String city, String state, String country) {
        fillFormField("[placeholder='Street']", street);
        fillFormField("[placeholder='City']", city);
        fillFormField("[placeholder='state']", state);
        fillFormField("[placeholder='country']", country);
    }

    private void fillPaymentDetails(String cardNumber, String cardHolderName,
                                    String expirationDate, String secCode) {
        log.debug("Filling payment: card={}, holder={}, exp={}", cardNumber, cardHolderName, expirationDate);
        fillFormField("[placeholder='000000000000000']", cardNumber);
        fillFormField("[placeholder='Card holder']", cardHolderName);
        fillFormField("[placeholder='MM/YY']", expirationDate);
        fillFormField("[placeholder='000']", secCode);
    }

    private void fillFormField(String cssSelector, String value) {
        By locator = By.cssSelector(cssSelector);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(locator),
                "Form field not present: " + cssSelector);
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }

    private void checkOrderItems(int expectedNumItems) {
        List<WebElement> items = driver.findElements(By.cssSelector("article.divider--bottom"));
        Assertions.assertEquals(expectedNumItems, items.size(),
                "Expected " + expectedNumItems + " order items in checkout summary");
    }

    public boolean checkOrderPlaced() throws ElementNotFoundException {
        int totalAttempts = 5;
        while (totalAttempts > 0) {
            try {
                waiter.waitUntil(ExpectedConditions.textToBe(
                        By.className("esh-basketstatus-badge"), "0"),
                        "Basket value is not 0");
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

    // -----------------------------------------------------------------------
    // Order list helpers
    // -----------------------------------------------------------------------

    private void checkLastOrderState(List<String> expectedStates) throws ElementNotFoundException {
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
                waiter.waitUntil(ExpectedConditions.not(
                        ExpectedConditions.textToBePresentInElement(statusElement, actualState)),
                        "Order status has not changed yet");
            } catch (Exception ex) {
                log.debug("Status unchanged after wait, retrying...");
            }
        }
        Assertions.assertFalse(actualState.isEmpty(),
                "No orders appeared in the list after " + maxIterations + " iterations");
        Assertions.assertTrue(expectedStates.contains(actualState),
                "Last order status is not as expected. Expected: " + expectedStates
                        + ", Actual: " + actualState);
    }

    private void cancelLastOrder() throws ElementNotFoundException {
        toOrdersPageSPA(driver, waiter);
        List<WebElement> listOrders = driver.findElements(By.className("esh-orders-item"));
        WebElement lastOrder = listOrders.get(listOrders.size() - 1);
        WebElement cancelLink = lastOrder.findElement(By.linkText("Cancel"));
        Click.element(driver, waiter, cancelLink);
    }
}
