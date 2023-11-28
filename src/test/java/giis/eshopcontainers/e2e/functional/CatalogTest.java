package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static giis.eshopcontainers.e2e.functional.utils.Shopping.*;

class ShoppingBasketTests extends BaseLoggedClass {
    @Test
    @DisplayName("AddProductsToBasket")
    void addProductsToBasket() throws ElementNotFoundException {
        this.login();
        WebElement loggedUser = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        Assertions.assertEquals("alice", loggedUser.getText(), "The user logged is not the expected, the expected was alice and the obtained is " + loggedUser.getText());
        WebElement productCupButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[1]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productCupButton.getAttribute("class"), "The eShop product button was expected for being enabled and was disabled");
        Click.element(driver, waiter, productCupButton);
        Assertions.assertEquals(1, getNumShoppingItems(driver, waiter));
        WebElement productHoodieBlackButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[3]/form/input[1]"));
        Click.element(driver, waiter, productHoodieBlackButton);
        Assertions.assertEquals(2, getNumShoppingItems(driver, waiter));
        WebElement productCupPinButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[6]/form/input[1]"));
        Click.element(driver, waiter, productCupPinButton);
        Assertions.assertEquals(3, getNumShoppingItems(driver, waiter));
        eraseShoppingBasket(driver, waiter);
        this.logout();
    }

    @Test
    @DisplayName("FilterProductsByBrand")
    void FilterProductsByBrand() throws ElementNotFoundException {

        selectBrandFilter(driver, waiter, 2);
        selectTypeFilter(driver, waiter, 3);
        //Assertions.assertEquals(2, numberCatalogDisplayedItems(driver));
        selectBrandFilter(driver, waiter, 2);
        selectTypeFilter(driver, waiter, 2);

        selectBrandFilter(driver, waiter, 3);
        selectTypeFilter(driver, waiter, 1);
    }


}