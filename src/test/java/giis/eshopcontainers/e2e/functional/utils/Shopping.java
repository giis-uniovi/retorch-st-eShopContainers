package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Shopping {
    public static Integer getNumShoppingItems(WebDriver driver, Waiter waiter) {
        waiter.waitUntil(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a"))), "The basket icon its not visible");
        WebElement basketElements = driver.findElement(By.className("esh-basketstatus-badge"));
        return Integer.valueOf(basketElements.getText());
    }

    public static void eraseShoppingBasket(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        Navigation.toMainMenu(driver, waiter);
        if (getNumShoppingItems(driver, waiter) != 0) {
            waiter.waitUntil(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a"))), "The basket icon is not visible");
            WebElement buttonBasket = driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a"));
            Click.element(driver, waiter, buttonBasket);
        }
    }

    /**
     * This method applies several brand filters to the eShopOnContainers catalog
     *
     * @param driver WebDriver with the browser
     * @param waiter Waiter of the provided webdriver
     * @param option Type of filter: 1) All brands, 2)NETCore and 3) Others
     */
    public static void selectBrandFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
        Navigation.toMainMenu(driver, waiter);
        WebElement elementOfInterest;
        try {
            //trying to get the "select option"
            elementOfInterest = driver.findElement(By.xpath("//*[@id=\"BrandFilterApplied\"]/option[" + option + "]"));
        } catch (NoSuchElementException e) {
            //so options are not visible, meaning we need to click first
            WebElement mainMenu = driver.findElement(By.id("BrandFilterApplied"));
            Click.element(driver, waiter, mainMenu);
            //good idea would be to put "wait for element" here
            elementOfInterest = mainMenu.findElement(By.xpath("//*[@id=\"BrandFilterApplied\"]/option[" + option + "]"));
        }

        Click.element(driver, waiter, elementOfInterest);
        WebElement filterApplyButton = driver.findElement(By.xpath("/html/body/section[2]/div/form/input[1]"));
        Click.element(driver, waiter, filterApplyButton);
    }

    /*  This method applies several type filters to the eShopOnContainers catalog
     * @param driver WebDriver with the browser
     * @param waiter of the provided web-driver
     * @param option Type of filter: 1) All Types, 2) Mug, 3) TShirt and 4)Pin */
    public static void selectTypeFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
        Navigation.toMainMenu(driver, waiter);
        WebElement elementOfInterest;
        try {
            //trying to get the "select option"
            elementOfInterest = driver.findElement(By.xpath("//*[@id=\"TypesFilterApplied\"]/option[" + option + "]"));
        } catch (NoSuchElementException e) {
            //so options are not visible, meaning we need to click first
            WebElement mainMenu = driver.findElement(By.id("TypesFilterApplied"));
            Click.element(driver, waiter, mainMenu);
            //good idea would be to put "wait for element" here
            elementOfInterest = mainMenu.findElement(By.xpath("//*[@id=\"TypesFilterApplied\"]/option[" + option + "]"));
        }
        Click.element(driver, waiter, elementOfInterest);
        WebElement filterApplyButton = driver.findElement(By.xpath("/html/body/section[2]/div/form/input[1]"));
        Click.element(driver, waiter, filterApplyButton);
    }

    public static Integer numberCatalogDisplayedItems(WebDriver driver) {
        WebElement parentDiv = driver.findElement(By.xpath("/html/body/div/div[3]"));

        String targetClass = "esh-catalog-item col-md-4"; // Change to the actual class name
        By childDivsWithClassLocator = By.cssSelector("div." + targetClass);
        java.util.List<WebElement> childDivsWithClass = parentDiv.findElements(childDivsWithClassLocator);

        // Get the count of child div elements with the specific class

        return childDivsWithClass.size();
    }
}