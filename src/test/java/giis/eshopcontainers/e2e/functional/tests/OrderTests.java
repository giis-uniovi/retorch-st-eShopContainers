package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
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

import static giis.eshopcontainers.e2e.functional.utils.Navigation.toMainMenu;
import static giis.eshopcontainers.e2e.functional.utils.Navigation.toOrdersPage;
import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasket;

/**
 * Test class for Order-related functionalities.
 */
class OrderTests extends BaseLoggedClass {

    /**
     * Tests the creation of a new order and its correct state configuration.
     */

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "ordering-api", concurrency = 50, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "payment-api", concurrency = 20, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testCreateNewOrder")
    void testCreateNewOrder() throws ElementNotFoundException {
        LinkedList<String> expectedStatesPriorCancelling = new LinkedList<>();
        expectedStatesPriorCancelling.add("submitted");
        expectedStatesPriorCancelling.add("paid");

        login();
        toOrdersPage(driver, waiter);
        createOrder();
        checkLastOrderState(expectedStatesPriorCancelling);
        logout();
    }

    /**
     * Created an order with three different products, fullfil the order data (payment and address) and removes it
     * checking that the order state changes as expected.
     */

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "basket-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "ordering-api", concurrency = 30, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "payment-api", concurrency = 20, sharing = true, accessMode = "READWRITE")
    @AccessMode(resID = "chrome-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, accessMode = "READWRITE")
    @Test
    @DisplayName("testCancelOrder")
    void testCancelOrder() throws ElementNotFoundException {
        LinkedList<String> expectedStatesPriorCancelling = new LinkedList<>();
        expectedStatesPriorCancelling.add("submitted");
        expectedStatesPriorCancelling.add("stockconfirmed");
        LinkedList<String> expectedStatesPostCancelling = new LinkedList<>();
        expectedStatesPostCancelling.add("cancelled");
        LinkedList <String> expectedStatesBeforeLongDelay = new LinkedList<>();
        expectedStatesBeforeLongDelay.add("paid");

        login();
        toOrdersPage(driver, waiter);
        createOrder();
        long startTime = System.currentTimeMillis();
        checkLastOrderState( expectedStatesPriorCancelling);
        long endTime = System.currentTimeMillis();
        long duration =endTime-startTime;
        log.debug("The time invested in place the order was: {}s", duration);
        if(duration<=3000) {
            cancelLastOrder();
            checkLastOrderState(expectedStatesPostCancelling);
        }
        else {
            checkLastOrderState(expectedStatesBeforeLongDelay);
        }

        logout();
    }

    /**
     * This method is used to check if the state of the last order is the expected. In eShopContainers order list, the
     * different orders that the user create are ordered by inverse date. In this method we make an iterative order
     * because
     * in some cases the order state it's not updated as soon as expected, and remains for miliseconds-seconds as
     * "awaitingvalidation"
     * state
     * @param expectedStates List with the expected state of the last order
     */
    private void checkLastOrderState( List<String> expectedStates) throws ElementNotFoundException {
        int maxIterations = 10;
        String actualState = "";

        for (int iter = 0; iter < maxIterations; iter++) {
            log.debug("Performing iteration {} over the orders", iter);
            toOrdersPage(driver, waiter);
            List<WebElement> listOrders = driver.findElements(By.className("esh-orders-items"));
            Assertions.assertFalse(listOrders.isEmpty(), "There's at least one order in the list");
            WebElement lastOrder = listOrders.get(listOrders.size() - 1);
            WebElement statusElement = lastOrder.findElements(By.className("esh-orders-item")).get(3);
            actualState = statusElement.getText();
            log.debug("End of iteration {}, the order state is {}", iter, actualState);
            if (expectedStates.contains(actualState)) {
                break; // Transition state passed, break and exit the loop.
            } else {
                try {
                    waiter.waitUntil(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(statusElement
                            , actualState)),
                            "The actual state remains untouched");
                    log.debug("Refreshing the webpage to update order status...");
                } catch (Exception ex) {
                    log.debug("Timeout the element remains with the previous state, previous was{}current is:{}", actualState, statusElement.getText());
                }
            }
        }
        Assertions.assertTrue(expectedStates.contains(actualState), "Last order status is not as expected. Expected: "
                + expectedStates + ", Actual: " + actualState);
    }

    /**
     * Navigates to the orders menu and press the Cancellation button of the last order. Checks that the order state
     * evolves as expected.
     */
    private void cancelLastOrder() throws ElementNotFoundException {
        toOrdersPage(driver, waiter);
        List<WebElement> listOrders = driver.findElements(By.className("esh-orders-items"));
        WebElement lastOrder = listOrders.get(listOrders.size() - 1);
        WebElement cancelButton = lastOrder.findElements(By.className("esh-orders-item")).get(5);
        Click.element(driver, waiter, cancelButton);
    }

    /**
     * Creates an order with three products, fullfilling the payment data, address data and checking that the order
     * price is the amount of money expected
     */
    private void createOrder() throws ElementNotFoundException {
        toMainMenu(driver, waiter);
        addProductsToBasket();
        navigateToCheckout("$ 36.00");
        fillAddressDetails("Campus de Viesques, Edif. Polivalente – D.2.6.06", "Asturias", "Gijon", "Spain");
        fillPaymentDetails("6271 7012 2597 9642", "Jose Ramon", "03/26", "456");
        checkOrderAmountAndNumItems("$ 36.00", 4);
        WebElement buttonPlaceOrder = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonPlaceOrder);
        Assertions.assertTrue(checkOrderPlaced(), "The order was not placed until 5 seconds");
    }

    /**
     * Method to verify if an order is correctly placed by ensuring that the basket is empty.
     * This verification should also be extended to the consistency of the database, not solely relying on the final
     * UI state.
     * While detailed functional tests are not conducted, this UI check its enough for now.
     * @return true if the basket is empty (no elements), otherwise returns false.
     **/
    public boolean checkOrderPlaced() throws ElementNotFoundException {
        int totalAttempts = 5; // Total attempts allowed to check if the order is placed
        while (totalAttempts > 0) {
            try {
                log.debug("Trial {} to check that number of items in the basket is updated", totalAttempts);
                // Wait until the basket status badge indicates 0 items
                waiter.waitUntil(ExpectedConditions.textToBe(By.className("esh-basketstatus-badge"), "0"), "The " +
                        "Basket value is not 0");
                log.debug("Order placed sucessfully!");
                return true; // Order is placed successfully
            } catch (TimeoutException e) {
                log.debug("The number of items was not 0");
                // If timeout occurs, navigate back to the main menu and decrement the attempts
                toMainMenu(driver, waiter);
                totalAttempts--; // Decrement total attempts
            }
        }
        return false; // Order could not be placed within the specified attempts
    }

    /**
     * Adds  some products to the basketproducts to the shopping basket.
     */
    private void addProductsToBasket() throws ElementNotFoundException {
        addProductToBasket(2, ".NET Blue Hoodie", driver, waiter);
        addProductToBasket(4, ".NET Foundation Pin", driver, waiter);
        addProductToBasket(5, ".NET Foundation T-shirt", driver, waiter);
    }

    /**
     * Navigates to the checkout page and checks that the total amount of money for being paid is the expected one
     * @param priceOrder Price of the selected order products
     */
    private void navigateToCheckout(String priceOrder) throws ElementNotFoundException {
        WebElement menuOrder = driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver, waiter, menuOrder);
        // Get the order price and check if it's correct
        WebElement totalAmountBasket = driver.findElement(By.xpath("//*[@id=\"cartForm\"]/div/div[2]/div[4]/article[2" +
                "]/section[2]"));
        Assertions.assertEquals(priceOrder, totalAmountBasket.getText());
        //Click into the Checkout button
        WebElement buttonCheckout = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonCheckout);
    }

    /**
     * Fills in address details during the checkout process, including the Street, State, City, and Country.
     * @param street  The street address to be filled in the address details.
     * @param state   The state information to be filled in the address details.
     * @param city    The city information to be filled in the address details.
     * @param country The country information to be filled in the address details.
     */
    private void fillAddressDetails(String street, String state, String city, String country) {
        fillFieldAndWait("Street", street);
        fillFieldAndWait("State", state);
        fillFieldAndWait("City", city);
        fillFieldAndWait("Country", country);
    }

    /**
     * Fills in a field and waits until its present.
     * @param fieldId the ID of the field to be filled
     * @param value   the value to be filled in the field
     */
    private void fillFieldAndWait(String fieldId, String value) {
        By fieldLocator = By.id(fieldId);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(fieldLocator), "FieldID:" + fieldId + " field is" +
                " not present");
        WebElement field = driver.findElement(By.id(fieldId));
        field.clear();
        field.sendKeys(value);
    }

    /**
     * Fills in payment details during the checkout process, including the Card Number, Cardholder Name,
     * Card Expiration Date, and Security Number.
     * @param cardNumber     The Card Number to be filled in the payment details.
     * @param cardHolderName The Cardholder Name to be filled in the payment details.
     * @param expirationDate The Card Expiration Date to be filled in the payment details.
     * @param secNumber      The Security Number to be filled in the payment details.
     */
    private void fillPaymentDetails(String cardNumber, String cardHolderName, String expirationDate, String secNumber) {
        log.debug("Filling payment details with Card Number: {}, Card Holder: {}, Expiration Date: {}, Security " +
                        "Number: {}",
                cardNumber, cardHolderName, expirationDate, secNumber);
        fillFieldAndWait("CardNumber", cardNumber);
        fillFieldAndWait("CardHolderName", cardHolderName);
        fillFieldAndWait("CardExpirationShort", expirationDate);
        fillFieldAndWait("CardSecurityNumber", secNumber);
    }

    /**
     * Checks the order amount and the number of items in the shopping basket.
     * @param amount           The expected total amount of the order.
     * @param numItemsExpected The expected number of items in the shopping basket.
     */
    private void checkOrderAmountAndNumItems(String amount, int numItemsExpected) {
        int numItems = driver.findElements(By.className("esh-orders_new-items")).size();
        // Assert the number of items matches the expected value
        Assertions.assertEquals(numItemsExpected, numItems);
        // Find the total amount displayed on the page
        WebElement totalAmountBasket = driver.findElement(By.xpath("/html/body/div[2]/form/section[4]/article[2" +
                "]/section[2]"));
        // Assert the total amount matches the expected value
        Assertions.assertEquals(amount, totalAmountBasket.getText());
    }
}

