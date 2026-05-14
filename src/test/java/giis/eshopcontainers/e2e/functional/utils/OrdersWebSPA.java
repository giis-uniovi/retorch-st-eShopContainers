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

    @Override
    public By getCardHolderNameBy() {return By.cssSelector("[placeholder='Card holder']");}
    @Override
    public By getCardExpirationDateBy() {return By.cssSelector("[placeholder='MM/YY']");}
    @Override
    public By getCardSecNumberBy() {return By.cssSelector("[placeholder='000']");}
    @Override
    public By getCardNumberBy() {return By.cssSelector("[placeholder='000000000000000']");}

    @Override
    public By getCityBy() {return By.cssSelector("[placeholder='City']");}
    @Override
    public By getStreetBy() {return By.cssSelector("[placeholder='Street']");}
    @Override
    public By getStateBy() {return By.cssSelector("[placeholder='state']");}
    @Override
    public By getCountryBy() {return By.cssSelector("[placeholder='country']");}

    /**
     * Creates an order in the WebSPA frontend, adds three products to the baskets.
     * The SPA checkout renders 3 line-item entries for the 3 products (no extra total row).
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    @Override
    public void createOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toMainMenu(driver, waiter);
        utils.addThreeProductsToBasket(driver, waiter);
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
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
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
     * Checks the last order state in the WebSPA frontend (SPA version), iterates several times to get
     * the state, as the order has a state change according its lifetime.
     *
     * @param driver         {@code WebDriver} on which the operations are performed.
     * @param waiter         {@code Waiter} to perform the necessary async waits.
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
            } catch (TimeoutException ex) {
                log.debug("Status unchanged after wait, retrying...");
            } catch (org.openqa.selenium.WebDriverException ex) {
                log.debug("Navigation/state issue during order status check, retrying: {}", ex.getMessage());
                navUtils.toOrdersPage(driver, waiter);
            }
        }
        Assertions.assertFalse(actualState.isEmpty(), "No orders appeared in the list after " + maxIterations + " iterations");
        Assertions.assertTrue(expectedStates.contains(actualState), "Last order status is not as expected. Expected: " + expectedStates + ", Actual: " + actualState);
    }

    /**
     * Cancels the last order in the WebSPA frontend (SPA version).
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
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
     * Checks the expected number of order items and total expected amount in the checkout summary.
     *
     * @param driver           {@code WebDriver} on which the operations are performed.
     * @param expectedAmount   Total amount of $ that the order is expected to cost.
     * @param expectedNumItems Expected number of items of the order.
     */
    private void checkOrderAmountAndNumItems(WebDriver driver, String expectedAmount, int expectedNumItems) {
        List<WebElement> items = driver.findElements(By.cssSelector("article.divider--bottom"));
        Assertions.assertEquals(expectedNumItems, items.size(), "Expected " + expectedNumItems + " order items in checkout summary");
        String numericValue = expectedAmount.replaceAll("[^0-9.,]", "");
        Assertions.assertTrue(driver.findElement(By.tagName("body")).getText().contains(numericValue),
                "Checkout page should display the total expectedAmount: " + expectedAmount);
    }
}
