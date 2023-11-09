package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedTest;
import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class LoggedUserTest extends BaseLoggedTest {
    @Test
    @DisplayName("BasicLoginTest")
    void loginTest() throws InterruptedException, ElementNotFoundException {
        log.info("Starting test");
        this.login(user, true);


        WebElement logged_user = user.getDriver().findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        Assertions.assertEquals("alice", logged_user.getText());
        WebElement product = user.getDriver().findElement(By.xpath("/html/body/div/div[3]/div[3]/form/input[1]"));
        Assertions.assertEquals("esh-catalog-button ", product.getAttribute("class"));

        this.logout(user);


        log.info("Ending test");

    }

}
