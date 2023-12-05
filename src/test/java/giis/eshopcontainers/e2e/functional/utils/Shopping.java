package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shopping {
    public static final Logger log = LoggerFactory.getLogger(Shopping.class);

    /**
     * Retrieves the number of items in the shopping basket.
     * @param driver Web driver of the application
     * @param waiter Waiter of the provided web driver
     */
    private static Integer getNumShoppingItems(WebDriver driver, Waiter waiter) {
        log.debug("Getting number of Shopping Cart items, looking for the basket icon.");
        By basketIconXPath = By.xpath("/html/body/header/div/article/section[3]/a");
        // Wait for the basket icon to be visible
        waiter.waitUntil(ExpectedConditions.visibilityOf(driver.findElement(basketIconXPath)), "The basket icon is not visible");
        // Get the basket elements and extract the number of items
        WebElement basketElements = driver.findElement(By.className("esh-basketstatus-badge"));
        String itemsText = basketElements.getText();
        // Log the number of items
        log.debug("The number of items is: {}", itemsText);
        // Return the number of items as an Integer
        return Integer.valueOf(itemsText);
    }

    /**
     * Adds a product to the shopping basket.
     * @param numProduct  The index of the product on the page.
     * @param productName The name of the product to add
     * @param driver      Web driver of the application
     * @param waiter      Waiter of the provided web driver
     */
    public static void addProductToBasket(Integer numProduct, String productName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        WebElement productButton;
        int numItemsPriorAdd = getNumShoppingItems(driver, waiter);
        log.debug("Adding the product: {}", productName);

        // Verify that the product button is enabled after login
        productButton = driver.findElement(By.xpath("/html/body/div/div[3]/div[" + numProduct + "]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", productButton.getAttribute("class"),
                "The eShop product button was expected to be enabled but was disabled");

        // Add the product to the basket
        Click.element(driver, waiter, productButton);
        Assertions.assertEquals(numItemsPriorAdd + 1, getNumShoppingItems(driver, waiter), "The number of items in the basket doesn't match");
        log.debug("Product: {} correctly added!", productName);
    }
}
