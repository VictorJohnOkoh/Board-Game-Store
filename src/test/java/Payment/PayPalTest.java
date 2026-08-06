package Payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayPalTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "victor@gmail.com",
            "v.john+okoh@sub.example.co.uk",
            "a_b-c@x.io",
            "first.last@domain-with-hyphen.com"
    })
    @DisplayName("accepts well-formed email addresses")
    void acceptsValidEmails(String email) {
        assertTrue(PayPal.isEmailValid(email), email + " should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-an-email",
            "@gmail.com",
            "a@-bad.com",
            "a@b",
            "a@b.c",
            "no domain@.com",
            ""
    })
    @DisplayName("rejects malformed email addresses")
    void rejectsInvalidEmails(String email) {
        assertFalse(PayPal.isEmailValid(email), email + " should be invalid");
    }

    @Test
    @DisplayName("rejects null rather than throwing")
    void rejectsNull() {
        assertFalse(PayPal.isEmailValid(null));
    }

    @Test
    @DisplayName("validation does not throw - the original pattern had an illegal character range")
    void validationNeverThrows() {
        // The previous pattern contained [A-Za-z0-9_-+], whose '_' to '+' range is reversed.
        // Pattern.matches threw PatternSyntaxException for every input, so no payment could
        // ever be made by PayPal. This guards against that regressing.
        PayPal.isEmailValid("anything at all");
        PayPal.isEmailValid("victor@gmail.com");
    }
}
