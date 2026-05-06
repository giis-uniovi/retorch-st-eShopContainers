package giis.eshopcontainers.e2e.functional.model;

/**
 * Tuple of HTTP status code and response body, employed in the PaymentAPITests.
 */
public class PaymentResponse {

    private  int statusCode;
    private  String body;

    public PaymentResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {return statusCode;}
    public String getBody() {return body;}

    public void setStatusCode(int statusCode) {this.statusCode = statusCode;}
    public void setBody(String body) {this.body = body;}
}