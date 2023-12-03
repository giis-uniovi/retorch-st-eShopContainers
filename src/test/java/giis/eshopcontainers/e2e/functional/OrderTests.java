package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasket;

class OrderTests extends BaseLoggedClass {
    @Test
    @DisplayName("CreateNewOrder")
    void createNewOrder() throws ElementNotFoundException {
        // Perform login
        this.login();
        // Add products to the basket
        addProductToBasket(2, "NetCore Cup",driver,waiter);
        addProductToBasket(4, "Hoodie",driver,waiter);
        addProductToBasket(5, "Pin",driver,waiter);


        WebElement menuOrder=driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver,waiter,menuOrder);

        // Perform logout
        this.logout();
        // Verify that the product cup button is disabled after logout

    }

}