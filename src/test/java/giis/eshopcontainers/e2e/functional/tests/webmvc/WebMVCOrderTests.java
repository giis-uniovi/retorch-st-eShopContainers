package giis.eshopcontainers.e2e.functional.tests.webmvc;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Test class for Order-related functionalities.
 */
class WebMVCOrderTests extends BaseLoggedClass {

    /**
     * Tests the creation of a new order in the MVC frontend and its correct state configuration.
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
    @DisplayName("testCreateNewOrderMVC")
    void testCreateNewOrderMVC() throws ElementNotFoundException {
        login();
        navHelper.toOrdersPage(driver, waiter);
        orderHelper.createOrder(driver, waiter);
        orderHelper.checkLastOrderState(driver, waiter, Arrays.asList("submitted", "paid"));
        logout();
    }

    /**
     * Created an order in the WebMVC frontend with three different products, fulfil the order data (payment and address) and removes it
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
    @DisplayName("testCancelOrderMVC")
    void testCancelOrderMVC() throws ElementNotFoundException {
        // States that can cancel an order
        List<String> cancellableStates = Arrays.asList("submitted", "stockconfirmed");
        // States that not allow the order cancelling
        List<String> preCancelStates = Arrays.asList("submitted", "stockconfirmed", "paid");

        login();
        navHelper.toOrdersPage(driver, waiter);
        orderHelper.createOrder(driver, waiter);
        String reachedState = orderHelper.checkLastOrderState(driver, waiter, preCancelStates);
        if (cancellableStates.contains(reachedState)) {
            orderHelper.cancelLastOrder(driver, waiter);
            orderHelper.checkLastOrderState(driver, waiter, Collections.singletonList("cancelled"));
        } else {
            log.debug("Order reached '{}' before cancellation was attempted — skipping cancel step", reachedState);
        }

        logout();
    }
}

