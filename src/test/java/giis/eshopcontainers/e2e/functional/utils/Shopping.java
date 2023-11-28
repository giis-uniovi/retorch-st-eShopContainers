package giis.eshopcontainers.e2e.functional.utils;

import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with various methods facilitating the catalog-shopping actions
 */
public class Shopping {
    public static final Logger log = LoggerFactory.getLogger(Shopping.class);

    public static Integer getNumShoppingItems(WebDriver driver, Waiter waiter) {
        waiter.waitUntil(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("/html/body/header/div/article/section[3]/a"))), "The basket icon its not visible");
        WebElement basketElements = driver.findElement(By.className("esh-basketstatus-badge"));
        log.debug("The number of items is: " + Integer.valueOf(basketElements.getText()));
        return Integer.valueOf(basketElements.getText());
    }

    /**
     * This method applies several brand filters to the eShopOnContainers catalog
     * @param option Type of filter: 1) All brands, 2)NETCore and 3) Others
     */
    public static void selectBrandFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
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
        log.debug("Selecting the Brand: " + new String[]{"All Brands", "Net Core", "Others"}[option - 1]);
        Click.element(driver, waiter, elementOfInterest);
    }

    /**
     * This method applies several type filters to the eShopOnContainers catalog
     * @param option Type of filter: 1) All Types, 2) Mug, 3) TShirt and 4)Pin
     */
    public static void selectTypeFilter(WebDriver driver, Waiter waiter, Integer option) throws ElementNotFoundException {
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
        log.debug("Selecting the Type: " + new String[]{"All Types", "Mug", "TShirt", "Pin"}[option - 1]);
        Click.element(driver, waiter, elementOfInterest);
    }

    /**
     * Get the number of displayed items in the catalog, counting also the items in the second page (if exist)
     */
    public static Integer numberCatalogDisplayedItems(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        int numelements = 0;
        log.debug("Getting the next button to check its visibility");
        WebElement nextButton = driver.findElement(By.id("Next"));
        if (nextButton.isDisplayed()) {
            numelements += driver.findElements(By.className("esh-catalog-item")).size();
            log.debug("Clicking to go to the next page, the first page number of items is:" + numelements);
            Click.element(driver, waiter, nextButton);
        } else {
            log.debug("Button not visible, counting only the elements of the webpage");
        }
        java.util.List<WebElement> childDivsWithClass = driver.findElements(By.className("esh-catalog-item"));
        log.debug("The total number of items is:" + numelements + childDivsWithClass.size());
        return numelements + childDivsWithClass.size();
    }

    public static void clickApplyFilterButton(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Click the Filter Apply button");
        WebElement filterApplyButton = driver.findElement(By.xpath("/html/body/section[2]/div/form/input[1]"));
        Click.element(driver, waiter, filterApplyButton);
    }
}