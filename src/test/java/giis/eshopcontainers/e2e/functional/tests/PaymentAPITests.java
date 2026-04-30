package giis.eshopcontainers.e2e.functional.tests;

import giis.eshopcontainers.e2e.functional.common.BaseAPIClass;
import giis.retorch.annotations.AccessMode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * The {@code PaymentAPITests} validates the Payment API HTTP surface. The Payment service is an
 * event-driven RabbitMQ subscriber and exposes only health/liveness probes — there is no business
 * REST surface to test.
 *
 * <p>Each test first attempts the request through the Desktop BFF gateway base URL with the
 * {@code /payment-api} prefix (matching the legacy Ocelot route from
 * {@code Mobile.Bff.Shopping/aggregator/apigw/configuration.json}). If the gateway returns 404
 * (no proxy route configured for Payment) the test falls back to the Payment service URL
 * directly so the contract is still exercised. This dual-path strategy mirrors the project
 * design where Payment is intentionally not on the active YARP routes.
 *
 * <p>Endpoints under test (relative to either {@code <gateway>/payment-api} or {@code <payment>}):
 * <ul>
 *   <li>GET /hc        — full health check (includes EventBus dependency)</li>
 *   <li>GET /liveness  — minimal liveness probe</li>
 * </ul>
 */
class PaymentAPITests extends BaseAPIClass {

    @AccessMode(resID = "payment-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testPaymentHealthCheckGateway")
    void testPaymentHealthCheck() throws IOException {
        PaymentResponse response = getPaymentResponse("/hc");
        Assertions.assertEquals(200, response.statusCode,
                "Expected HTTP 200 from Payment /hc, got " + response.statusCode);
        Assertions.assertFalse(response.body.isEmpty(), "Health check response body must not be empty");
        // The full /hc endpoint returns a JSON document or "Healthy"/"Unhealthy" text — both
        // contain the literal status. We accept either shape.
        Assertions.assertTrue(
                response.body.contains("Healthy") || response.body.contains("\"status\":\"Healthy\""),
                "Health check must report Healthy status; body was: " + response.body);
    }

    @AccessMode(resID = "payment-api", concurrency = 50, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testPaymentLivenessGateway")
    void testPaymentLiveness() throws IOException {
        PaymentResponse response = getPaymentResponse("/liveness");
        Assertions.assertEquals(200, response.statusCode,
                "Expected HTTP 200 from Payment /liveness, got " + response.statusCode);
        Assertions.assertFalse(response.body.isEmpty(), "Liveness response body must not be empty");
    }

    // -----------------------------------------------------------------------
    // Helpers — gateway first, fall back to direct payment URL
    // -----------------------------------------------------------------------

    /**
     * Issues a GET against the given Payment-relative path. Tries the BFF gateway with the
     * {@code /payment-api} prefix first; if the gateway returns 404 (no proxy route) the request
     * is retried against the Payment service URL directly.
     *
     * @param subPath payment-relative path including leading slash (e.g. {@code "/hc"})
     */
    private PaymentResponse getPaymentResponse(String subPath) throws IOException {
        // 1) Gateway path
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.getDesktopBFFPaymentURL() + subPath);
            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status != 404) {
                return new PaymentResponse(status, readBody(response));
            }
            log.debug("Payment path {} not exposed by the gateway (404), falling back to direct URL", subPath);
        }
        // 2) Direct Payment URL
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(getPaymentURL() + subPath);
            HttpResponse response = httpClient.execute(request);
            return new PaymentResponse(response.getStatusLine().getStatusCode(), readBody(response));
        }
    }

    private static String readBody(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        return entity != null ? EntityUtils.toString(entity) : "";
    }

    /** Tuple of HTTP status code and response body, returned by {@link #getPaymentResponse}. */
    private static final class PaymentResponse {
        final int statusCode;
        final String body;

        PaymentResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
