package giis.eshopcontainers.e2e.functional;


import giis.eshopcontainers.e2e.Class1;
import giis.eshopcontainers.e2e.functional.common.BaseLoggedTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.rules.TestName;

public class TestClass1 extends BaseLoggedTest {
	@Test
	@MethodSource("data")
	void fogTestCase() throws InterruptedException {
	log.info("Starting test");
		Assert.assertEquals("11", new Class1().function11());
		Thread.sleep(6000);
		log.info("Ending test");
	}
	
}
