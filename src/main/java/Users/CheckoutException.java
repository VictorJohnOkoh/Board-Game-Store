package Users;

/**
 * Thrown when a checkout could not be completed after payment details were taken -
 * currently only when the stock update fails. The basket is deliberately left intact
 * so the customer does not lose it.
 */
public class CheckoutException extends Exception {

    public CheckoutException(String message) {
        super(message);
    }
}
