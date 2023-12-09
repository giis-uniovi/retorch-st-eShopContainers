package giis.eshopcontainers.e2e.api;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class CatalogAPITests {
    public static final Logger log = LoggerFactory.getLogger(CatalogAPITests.class);
    private static String sutUrl;
    protected static Properties properties;

    @BeforeAll()
    static void setupAll() { //28 lin
        properties = new Properties();
        String envUrl = System.getProperty("SUT_URL");
        String envParameterUrl = System.getenv("SUT_URL");
        if (envUrl == null & envParameterUrl == null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
            log.debug("Configuring the local browser to connect to a local System Under Test (SUT) at: " + sutUrl);
        } else {
            sutUrl = envUrl != null ? "http://" + envUrl + "/" : "http://" + envParameterUrl + "/";
            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: " + sutUrl);
        }
    }

    @Test
    void getsCorrectlyTheProductCatalogTest()
            throws IOException {

        // Given
        String name = RandomStringUtils.randomAlphabetic(8);

        HttpUriRequest request = new HttpGet("http://webshoppingagg_" + System.getProperty("tjob_name") + "}:80/c/api/v1/catalog/items/[0]/pic/");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        Assertions.assertEquals(
                httpResponse.getCode(),
                200);
    }
}
