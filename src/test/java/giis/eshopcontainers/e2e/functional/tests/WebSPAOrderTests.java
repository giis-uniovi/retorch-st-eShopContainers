package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseWebSPALoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;


/**
 * Validates order creation and cancellation flows in the WebSPA frontend.
 * The clearing of the basket is handled by {@code BaseWebSPALoggedClass}
 */
class WebSPAOrderTests extends BaseWebSPALoggedClass {

    /**
     * Tests the creation of a new order in the SPA frontend and its correct state configuration.
     */
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
        navHelper.toOrdersPage(driver, waiter);
        orderHelper.createOrder(driver,waiter);
        orderHelper.checkLastOrderState(driver,waiter,expectedStates);
        logout();
    }

    /**
     * Created an order with three different products in the SPA frontend, fulfil the order data (payment and address) and removes it
     * checking that the order state changes as expected.
     */
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

        this.login();
        navHelper.toOrdersPage(driver, waiter);
        orderHelper.createOrder(driver,waiter);
        long startTime = System.currentTimeMillis();
        orderHelper.checkLastOrderState(driver,waiter,expectedStatesPriorCancelling);
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Time invested in placing the order: {}ms", duration);
        if (duration <= 3000) {
            orderHelper.cancelLastOrder(driver,waiter);
            orderHelper.checkLastOrderState(driver,waiter,expectedStatesPostCancelling);
        }
        else {
            orderHelper.checkLastOrderState(driver,waiter,expectedStatesBeforeLongDelay);
        }

        logout();
    }
}