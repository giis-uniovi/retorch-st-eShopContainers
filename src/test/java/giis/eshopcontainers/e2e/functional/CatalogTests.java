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

class CatalogTests extends BaseLoggedClass {
    @Test
    @DisplayName("AddProductsToBasket")
    void addProductsToBasket() throws ElementNotFoundException {
        log.debug("Checking that the product buttons are disabled before the user logs-in");
        WebElement productCupButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[1]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button is-disabled", productCupButton.getAttribute("class"), "Before Login the eShop product button was expected for being disabled and was enabled");
        this.login();
        WebElement loggedUser = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        Assertions.assertEquals("alice", loggedUser.getText(), "The user logged is not the expected, the expected was alice and the obtained is " + loggedUser.getText());
        log.debug("Adding the first element: NetCore Cup ");
        productCupButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[1]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productCupButton.getAttribute("class"), "The eShop product button was expected for being enabled and was disabled");
        Click.element(driver, waiter, productCupButton);
        Assertions.assertEquals(1, getNumShoppingItems(driver, waiter), "The number of elements in the basket doesn't match");
        log.debug("Adding Two elements more: Hoodie and CupPin");
        WebElement productHoodieBlackButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[3]/form/input[1]"));
        Click.element(driver, waiter, productHoodieBlackButton);
        Assertions.assertEquals(2, getNumShoppingItems(driver, waiter));
        WebElement productCupPinButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[6]/form/input[1]"));
        Click.element(driver, waiter, productCupPinButton);
        Assertions.assertEquals(3, getNumShoppingItems(driver, waiter));
        this.logout();
        log.debug("Checking that the product buttons are disabled after the logout...");
        productCupButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[1]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button is-disabled", productCupButton.getAttribute("class"), "After Log-Out the eShop product buttons was expected for being disabled and was enabled");
    }

    @Test
    @DisplayName("FilterProductsByBrand")
    void FilterProductsByBrandType() throws ElementNotFoundException {
        int[] brands = {1, 2, 3};
        int[] types = {1, 2, 3, 4};
        int[][] numItems = {{14, 4, 7, 3}, {7, 2, 3, 2}, {7, 2, 4, 1}};
        for (int numBrand : brands) {
            selectBrandFilter(driver, waiter, numBrand);
            for (int numType : types) {
                selectTypeFilter(driver, waiter, numType);
                clickApplyFilterButton(driver, waiter);
                Assertions.assertEquals(numItems[numBrand - 1][numType - 1], numberCatalogDisplayedItems(driver, waiter),
                        "Brand:" + new String[]{"All Brands", "Net Core", "Others"}[numBrand - 1] + " Type:" +
                                new String[]{"All Types", "Mug", "TShirt", "Pin"}[numType - 1] + " Num items exp:" + numItems[numBrand - 1][numType - 1]);
            }
        }
    }
}