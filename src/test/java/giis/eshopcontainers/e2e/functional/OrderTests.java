package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasket;

class OrderTests extends BaseLoggedClass {
    @Test
    @DisplayName("CreateNewOrder")
    void createNewOrder() throws ElementNotFoundException {
        // Perform login
        this.login();
        // Add products to the basket
        addProductToBasket(1, "NetCore Cup",driver,waiter);
        addProductToBasket(3, "Hoodie",driver,waiter);
        addProductToBasket(6, "Pin",driver,waiter);

        // Perform logout
        this.logout();
        // Verify that the product cup button is disabled after logout

    }

}