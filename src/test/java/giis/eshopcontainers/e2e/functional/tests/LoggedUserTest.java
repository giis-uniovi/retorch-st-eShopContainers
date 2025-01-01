package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedClass;
import giis.eshopcontainers.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class LoggedUserTest extends BaseLoggedClass {

    @AccessMode(resID = "webmvc", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "identity-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "catalog-api", concurrency = 60, sharing =true, accessMode = "READONLY")
    @AccessMode(resID = "chrome-browser", concurrency = 1, accessMode ="READWRITE")
    @AccessMode(resID = "eshopUser", concurrency = 1, sharing = false, accessMode ="READONLY")
    @Test
    @DisplayName("BasicLoginTest")
    void loginTest() throws ElementNotFoundException {
        this.login();
        WebElement loggedUser = driver.findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        Assertions.assertEquals("alice", loggedUser.getText(), "The user logged is not the expected, the expected was alice and the obtained is " + loggedUser.getText());
        WebElement product = driver.findElement(By.xpath("/html/body/div/div[3]/div[3]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", product.getAttribute("class"), "The eShop product button was expected for being enabled and was disabled");
        this.logout();
    }
}