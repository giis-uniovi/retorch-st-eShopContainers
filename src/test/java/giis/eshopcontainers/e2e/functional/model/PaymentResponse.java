package giis.eshopcontainers.e2e.functional.model;

/**
 * {@code PaymentResponse} contains a DTO used as tuple of HTTP status code and response body,
 * employed in the {@code PaymentAPITests}.
 */
public class PaymentResponse {

    private final int statusCode;
    private final String body;

    public PaymentResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {return statusCode;}
    public String getBody() {return body;}

}