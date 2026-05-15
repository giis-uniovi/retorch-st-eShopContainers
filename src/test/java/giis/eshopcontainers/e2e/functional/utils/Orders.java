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
 * WebMVC order management helpers for order creation, cancellation, and state checking.
 */
public class Orders extends Shopping {
    public static final Logger log = LoggerFactory.getLogger(Orders.class);
    protected Basket utils;

    public Orders(){
        navUtils = new Navigation();
        utils = new Basket();
    }

    // Getters of the different placeholders By, overridden if necessary in OrdersWebSpa
    public By getCardHolderNameBy() {return By.id("CardHolderName");}
    public By getCardExpirationDateBy() {return By.id("CardExpirationShort");}
    public By getCardSecNumberBy() {return By.id("CardSecurityNumber");}
    public By getCardNumberBy() {return By.id("CardNumber");}

    public By getCityBy() {return By.id("City");}
    public By getStreetBy() {return By.id("Street");}
    public By getStateBy() {return By.id("State");}
    public By getCountryBy() {return By.id("Country");}


    /**
     * Creates an order with three products, fulfilling payment data, address data and checking that the order
     * price is the amount of money expected.
     * The MVC checkout renders 4 line-item entries for the 3 products (one extra entry for the total row).
     * @see OrdersWebSPA#createOrder for the SPA variant which renders 3 entries.
     */
    public void createOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toMainMenu(driver, waiter);
        utils.addThreeProductsToBasket(driver, waiter);
        navUtils.navigateToCheckout(driver, waiter);
        //TO-DO validate the order price
        fillAddressDetails(driver, waiter, "Campus de Viesques, Edif. Polivalente – D.2.6.06", "Asturias", "Gijon", "Spain");
        fillPaymentDetails(driver, waiter, "6271 7012 2597 9642", "Jose Ramon", "03/38", "456");
        checkOrderAmountAndNumItems(driver, "$ 36.00", 4);
        WebElement buttonPlaceOrder = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonPlaceOrder);
        Assertions.assertTrue(checkOrderPlaced(driver, waiter), "The order was not placed until 5 seconds");
    }

    /**
     * Method to verify if an order is correctly placed by ensuring that the basket is empty.
     * This verification should also be extended to the consistency of the database, not solely relying on the final
     * UI state.
     *
     * @return true if the basket is empty (no elements), otherwise returns false.
     **/
    public boolean checkOrderPlaced(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int totalAttempts = 5;
        while (totalAttempts > 0) {
            try {
                log.debug("Trial {} to check that number of items in the basket is updated", totalAttempts);
                waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "0"), "The Basket value is not 0");
                log.debug("Order placed successfully!");
                return true;
            } catch (TimeoutException e) {
                log.debug("The number of items was not 0");
                navUtils.toMainMenu(driver, waiter);
                totalAttempts--;
            }
        }
        return false;
    }

    /**
     * This method is used to check if the state of the last order is the expected. In eShopContainers order list, the
     * different orders that the user create are ordered by inverse date. In this method we make an iterative order
     * because in some cases the order state it's not updated as soon as expected, and remains for milliseconds as
     * "awaitingvalidation" state
     *
     * @param driver         {@code WebDriver} on which the operations are performed.
     * @param waiter         {@code Waiter} to perform the necessary async waits.
     * @param expectedStates List with the expected state of the last order
     */
    public String checkLastOrderState(WebDriver driver, Waiter waiter, List<String> expectedStates) throws ElementNotFoundException {
        int maxIterations = 10;
        String actualState = "";

        for (int iter = 0; iter < maxIterations; iter++) {
            log.debug("Performing iteration {} over the orders", iter);
            navUtils.toOrdersPage(driver, waiter);
            List<WebElement> listOrders = driver.findElements(By.className("esh-orders-items"));
            Assertions.assertFalse(listOrders.isEmpty(), "There's at least one order in the list");
            WebElement lastOrder = listOrders.get(listOrders.size() - 1);
            WebElement statusElement = lastOrder.findElements(By.className("esh-orders-item")).get(3);
            actualState = statusElement.getText();
            log.debug("End of iteration {}, the order state is {}", iter, actualState);
            if (expectedStates.contains(actualState)) {
                break;
            } else {
                try {
                    waiter.waitUntil(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(statusElement, actualState)),
                            "The actual state remains untouched");
                    log.debug("Refreshing the webpage to update order status...");
                } catch (TimeoutException ex) {
                    log.debug("Timeout the element remains with the previous state, previous was{}current is:{}", actualState, statusElement.getText());
                } catch (org.openqa.selenium.WebDriverException ex) {
                    log.debug("Navigation/state issue during order status check, retrying: {}", ex.getMessage());
                    navUtils.toOrdersPage(driver, waiter);
                }
            }
        }
        Assertions.assertTrue(expectedStates.contains(actualState), "Last order status is not as expected. Expected: "
                + expectedStates + ", Actual: " + actualState);
        return actualState;
    }

    /**
     * Navigates to the orders menu and press the Cancellation button of the last order. Checks that the order state
     * evolves as expected.
     *
     * @param driver {@code WebDriver} on which the operations are performed.
     * @param waiter {@code Waiter} to perform the necessary async waits.
     */
    public void cancelLastOrder(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        navUtils.toOrdersPage(driver, waiter);
        List<WebElement> listOrders = driver.findElements(By.className("esh-orders-items"));
        WebElement lastOrder = listOrders.get(listOrders.size() - 1);
        WebElement cancelButton = lastOrder.findElement(By.linkText("Cancel"));
        Click.element(driver, waiter, cancelButton);
    }


    /**
     * Fills in address details during the checkout process, including the Street, State, City, and Country.
     *
     * @param driver  {@code WebDriver} on which the operations are performed.
     * @param waiter  {@code Waiter} to perform the necessary async waits.
     * @param street  The street address to be filled in the address details.
     * @param state   The state information to be filled in the address details.
     * @param city    The city information to be filled in the address details.
     * @param country The country information to be filled in the address details.
     */
    protected void fillAddressDetails(WebDriver driver, Waiter waiter, String street, String state, String city, String country) {
        fillField(driver, waiter, getStreetBy(), street);
        fillField(driver, waiter, getStateBy(), state);
        fillField(driver, waiter, getCityBy(), city);
        fillField(driver, waiter, getCountryBy(), country);
    }

    /**
     * Fills in payment details during the checkout process, including the Card Number, Cardholder Name,
     * Card Expiration Date, and Security Number.
     *
     * @param driver         {@code WebDriver} on which the operations are performed.
     * @param waiter         {@code Waiter} to perform the necessary async waits.
     * @param cardNumber     The Card Number to be filled in the payment details.
     * @param cardHolderName The Cardholder Name to be filled in the payment details.
     * @param expirationDate The Card Expiration Date to be filled in the payment details.
     * @param secNumber      The Security Number to be filled in the payment details.
     */
    protected void fillPaymentDetails(WebDriver driver, Waiter waiter, String cardNumber, String cardHolderName, String expirationDate, String secNumber) {
        log.debug("Filling payment details with Card Number: {}, Card Holder: {}, Expiration Date: {}, Security Number: {}",
                cardNumber, cardHolderName, expirationDate, secNumber);
        fillField(driver, waiter, getCardNumberBy(), cardNumber);
        fillField(driver, waiter, getCardHolderNameBy(), cardHolderName);
        fillField(driver, waiter, getCardExpirationDateBy(), expirationDate);
        fillField(driver, waiter, getCardSecNumberBy(), secNumber);
    }

    /**
     * Checks the order amount and the number of items in the shopping basket.
     *
     * @param driver           {@code WebDriver} on which the operations are performed.
     * @param amount           The expected total amount of the order.
     * @param numItemsExpected The expected number of items in the shopping basket.
     */
    private void checkOrderAmountAndNumItems(WebDriver driver, String amount, int numItemsExpected) {
        int numItems = driver.findElements(By.className("esh-orders_new-items")).size();
        Assertions.assertEquals(numItemsExpected, numItems);
        WebElement totalAmountBasket = driver.findElement(By.xpath("/html/body/div[2]/form/section[4]/article[2]/section[2]"));
        Assertions.assertEquals(amount, totalAmountBasket.getText());
    }
}
