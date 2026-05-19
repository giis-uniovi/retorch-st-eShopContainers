package giis.eshopcontainers.e2e.functional.common;

/** Custom exception used to manage the failures retrieving web elements in the system test cases*/
public class ElementNotFoundException extends Exception {

    private static final long serialVersionUID = -470143598556264052L;

    public ElementNotFoundException() {
        super();
    }

    public ElementNotFoundException(String message) {
        super(message);
    }
}