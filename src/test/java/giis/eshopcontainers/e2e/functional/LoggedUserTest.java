package giis.eshopcontainers.e2e.functional;

import giis.eshopcontainers.e2e.functional.common.BaseLoggedTest;
import giis.eshopcontainers.e2e.functional.common.exceptions.ElementNotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoggedUserTest extends BaseLoggedTest {
    @Test
    void loginTest() throws InterruptedException, ElementNotFoundException {
        log.info("Starting test");
        this.login(user, true);


        WebElement loggeduser = user.getDriver().findElement(By.xpath("//*[@id=\"logoutForm\"]/section[1]/div"));
        Assertions.assertEquals("alice", loggeduser.getText());
		WebElement product = user.getDriver().findElement(By.xpath("/html/body/div/div[3]/div[3]/form/input[1]"));
		Assertions.assertEquals("esh-catalog-button ",product.getAttribute("class"));

		this.logout(user);


        log.info("Ending test");

    }

}
