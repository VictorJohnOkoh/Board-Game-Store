package Payment;

import java.util.regex.Pattern;

import Users.Address;


public class PayPal implements PaymentMethod{

    /* The email pattern. Local part allows letters, digits, underscore, plus and hyphen,
     * with dot-separated segments. The domain may not begin with a hyphen and must end in
     * a TLD of at least two letters. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_+-]+(\\.[A-Za-z0-9_+-]+)*@[^-][A-Za-z0-9-]*(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    private final String email;

    /** Takes an already-collected email. Collecting it - from a console or a text field -
     * is the caller's job, so this class stays usable from both the CLI and the GUI. */
    public PayPal (String email){
        this.email = email;
    }

    /** Checks if the email address provided has a valid domain and format*/
    public static boolean isEmailValid(String email){
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public Receipt processPayment(double total, Address address) {
        return new PayPalReceipt(total, address, email);
    }

}
