package giis.eshopcontainers.e2e.functional;


import giis.eshopcontainers.e2e.Class1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClass1 {
	private final Logger log=LoggerFactory.getLogger(this.getClass());

	@Rule 
	public TestName testName = new TestName();
	
	@Before
	public void setUp() {
		log.info("****** Running test: {} ******", testName.getMethodName());
	}

	@Test
	public void testFunction11() {
		Assert.assertEquals("11", new Class1().function11());
	}
	
}
