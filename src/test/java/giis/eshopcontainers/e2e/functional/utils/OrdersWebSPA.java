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

/**
 * WebSPA order management helpers for order creation, cancellation, and state checking.
 * Overrides WebMVC order methods with SPA-specific selectors and navigation.
 */
public class OrdersWebSPA extends Orders {
    public static final Logger log = LoggerFactory.getLogger(OrdersWebSPA.class);

    public OrdersWebSPA(){
        navUtils = new NavigationWebSPA();
        utils = new BasketWebSPA();
    }

    /**
     * Creates an order in the WebSPA frontend, adds three products to the baskets
     */
    @Override
    public void createOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toMainMenu(driver, waiter);
        utils.addProductsToBasket(driver, waiter);
        navUtils.navigateToCheckout(driver, waiter);
        fillAddressDetails(driver, waiter, "Campus de Viesques, Edif. Polivalente – D.2.6.06", "Gijon", "Asturias", "Spain");
        fillPaymentDetails(driver, waiter, "6271 7012 2597 9642", "Jose Ramon", "03/38", "456");
        checkOrderAmountAndNumItems(driver, "$ 36.00", 3);
        By placeOrderLocator = By.xpath("//button[normalize-space(text())='Place Order']");
        waiter.waitUntil(ExpectedConditions.elementToBeClickable(placeOrderLocator), "Place Order button is not clickable");
        Click.element(driver, waiter, driver.findElement(placeOrderLocator));
        Assertions.assertTrue(checkOrderPlaced(driver, waiter), "The order was not placed within the allowed attempts");
    }

    /**
     * Method to verify if an order is correctly placed by ensuring that the basket is empty (SPA version).
     * @return true if the basket is empty (no elements), otherwise returns false.
     */
    @Override
    public boolean checkOrderPlaced(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int totalAttempts = 5;
        while (totalAttempts > 0) {
            try {
                waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "0"), "Basket value is not 0");
                log.debug("Order placed successfully!");
                return true;
            } catch (TimeoutException e) {
                log.debug("Basket count was not 0 yet, retrying... ({} left)", totalAttempts - 1);
                navUtils.toMainMenu(driver, waiter);
                totalAttempts--;
            }
        }
        return false;
    }

    /**
     * Checks the last order state in the WebSPA frontend (SPA version).
     * Uses different selectors than WebMVC version.
     * @param expectedStates List with the expected state of the last order
     */
    @Override
    public void checkLastOrderState(WebDriver driver, Waiter waiter, List<String> expectedStates) throws ElementNotFoundException {
        int maxIterations = 10;
        String actualState = "";
        for (int iter = 0; iter < maxIterations; iter++) {
            log.debug("Iteration {} checking order state", iter);
            navUtils.toOrdersPage(driver, waiter);
            List<WebElement> listOrders = driver.findElements(By.className("esh-orders-item"));
            if (listOrders.isEmpty()) {
                log.debug("Iteration {} — orders list is empty, retrying...", iter);
                continue;
            }
            WebElement lastOrder = listOrders.get(listOrders.size() - 1);
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

    /**
     * Cancels the last order in the WebSPA frontend (SPA version).
     */
    @Override
    public void cancelLastOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toOrdersPage(driver, waiter);
        List<WebElement> listOrders = driver.findElements(By.className("esh-orders-item"));
        WebElement lastOrder = listOrders.get(listOrders.size() - 1);
        WebElement cancelLink = lastOrder.findElement(By.linkText("Cancel"));
        Click.element(driver, waiter, cancelLink);
    }



    /**
     * Fills in address details during checkout using CSS selectors (SPA version).
     */
    @Override
    protected void fillAddressDetails(WebDriver driver, Waiter waiter, String street, String city, String state, String country) {
        fillField(driver, waiter, By.cssSelector("[placeholder='Street']"), street);
        fillField(driver, waiter, By.cssSelector("[placeholder='City']"), city);
        fillField(driver, waiter, By.cssSelector("[placeholder='state']"), state);
        fillField(driver, waiter, By.cssSelector("[placeholder='country']"), country);
    }

    /**
     * Fills in payment details during checkout using CSS selectors (SPA version).
     */
    @Override
    protected void fillPaymentDetails(WebDriver driver, Waiter waiter, String cardNumber, String cardHolderName, String expirationDate, String secCode) {
        log.debug("Filling payment: card={}, holder={}, exp={}", cardNumber, cardHolderName, expirationDate);
        fillField(driver, waiter, By.cssSelector("[placeholder='000000000000000']"), cardNumber);
        fillField(driver, waiter, By.cssSelector("[placeholder='Card holder']"), cardHolderName);
        fillField(driver, waiter, By.cssSelector("[placeholder='MM/YY']"), expirationDate);
        fillField(driver, waiter, By.cssSelector("[placeholder='000']"), secCode);
    }

    /**
     * Checks the expected number of order items and total amount in the checkout summary.
     */
    private void checkOrderAmountAndNumItems(WebDriver driver, String amount, int expectedNumItems) {
        List<WebElement> items = driver.findElements(By.cssSelector("article.divider--bottom"));
        Assertions.assertEquals(expectedNumItems, items.size(), "Expected " + expectedNumItems + " order items in checkout summary");
        String numericValue = amount.replaceAll("[^0-9.,]", "");
        Assertions.assertTrue(driver.getPageSource().contains(numericValue),
                "Checkout page should display the total amount: " + amount);
    }
}
