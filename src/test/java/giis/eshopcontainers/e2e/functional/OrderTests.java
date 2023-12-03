package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.eshopcontainers.e2e.functional.utils.Click;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static giis.eshopcontainers.e2e.functional.utils.Navigation.toOrdersPage;
import static giis.eshopcontainers.e2e.functional.utils.Shopping.addProductToBasket;

class OrderTests extends BaseLoggedClass {

    @Test
    @DisplayName("testCreateNewOrder")
    void testCreateNewOrder() throws ElementNotFoundException {
        // Step 1: Perform login
        this.login();
        // Step 2: Add products to the basket
        addProductsToBasket();
        // Step 3: Navigate to the checkout page
        navigateToCheckout();
        // Step 4: Fill in address details
        fillAddressDetails();
        // Step 5: Fill in payment details
        fillPaymentDetails();
        // Step 6: Perform logout
        checkOrderAmountAndNumItems();
        // Step 7: Place Order
        WebElement buttonPlaceOrder = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonPlaceOrder);
        // Step 8: Go to orders page
        toOrdersPage(driver, waiter);

        this.logout();
    }

    private void addProductsToBasket() throws ElementNotFoundException {
        addProductToBasket(2, ".NET Blue Hoodie", driver, waiter);
        addProductToBasket(4, ".NET Foundation Pin", driver, waiter);
        addProductToBasket(5, ".NET Foundation T-shirt", driver, waiter);
    }

    private void navigateToCheckout() throws ElementNotFoundException {
        WebElement menuOrder = driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a/div[2]"));
        Click.element(driver, waiter, menuOrder);

        WebElement totalAmountBasket = driver.findElement(By.xpath("//*[@id=\"cartForm\"]/div/div[2]/div[4]/article[2]/section[2]"));
        Assertions.assertEquals("$ 36.00", totalAmountBasket.getText());

        WebElement buttonCheckout = driver.findElement(By.name("action"));
        Click.element(driver, waiter, buttonCheckout);
    }


    private void fillAddressDetails() {
        fillFieldAndWait("Street", "Campus de Viesques, Edif. Polivalente – D.2.6.06");
        fillFieldAndWait("State", "Asturias");
        fillFieldAndWait("City", "Gijon");
        fillFieldAndWait("Country", "Spain");
    }

    private void fillFieldAndWait(String fieldId, String value) {
        By fieldLocator = By.id(fieldId);
        waiter.waitUntil(ExpectedConditions.presenceOfElementLocated(fieldLocator), "FieldID:" + fieldId + " field is not present");
        WebElement field = driver.findElement(By.id(fieldId));
        field.sendKeys(value);
    }

    private void fillPaymentDetails() {
        fillFieldAndWait("CardNumber", "6271 7012 2597 9642");
        fillFieldAndWait("CardHolderName", "Jose Ramon");
        fillFieldAndWait("CardExpirationShort", "03/26");
        fillFieldAndWait("CardSecurityNumber", "456");
    }

    private void checkOrderAmountAndNumItems() {
        int numItems = driver.findElements(By.className("esh-orders_new-items")).size();
        Assertions.assertEquals(4, numItems);
        WebElement totalAmountBasket = driver.findElement(By.xpath("/html/body/div[2]/form/section[4]/article[2]/section[2]"));
        Assertions.assertEquals("$ 36.00", totalAmountBasket.getText());
    }

}