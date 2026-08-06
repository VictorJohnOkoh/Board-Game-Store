package Payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditCardTest {

    @Test
    @DisplayName("accepts a 6 digit card number")
    void acceptsSixDigitCardNumber() {
        assertTrue(CreditCard.isCardNumberValid("123456"));
    }

    @Test
    @DisplayName("trims surrounding whitespace before checking length")
    void trimsWhitespace() {
        assertTrue(CreditCard.isCardNumberValid("  123456  "));
        assertTrue(CreditCard.isSecurityCodeValid(" 123 "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234567", "12345a", "abcdef", "12 456", ""})
    @DisplayName("rejects card numbers that are not exactly 6 digits")
    void rejectsBadCardNumbers(String input) {
        assertFalse(CreditCard.isCardNumberValid(input), input + " should be invalid");
    }

    @Test
    @DisplayName("accepts a 3 digit security code")
    void acceptsThreeDigitSecurityCode() {
        assertTrue(CreditCard.isSecurityCodeValid("123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "1234", "12a", ""})
    @DisplayName("rejects security codes that are not exactly 3 digits")
    void rejectsBadSecurityCodes(String input) {
        assertFalse(CreditCard.isSecurityCodeValid(input), input + " should be invalid");
    }

    @Test
    @DisplayName("rejects null rather than throwing")
    void rejectsNull() {
        assertFalse(CreditCard.isCardNumberValid(null));
        assertFalse(CreditCard.isSecurityCodeValid(null));
    }

    @Test
    @DisplayName("non-numeric input is rejected without throwing")
    void nonNumericDoesNotThrow() {
        // The old Scanner-based constructor caught NumberFormatException, but nextInt()
        // throws InputMismatchException - so bad input escaped the catch entirely.
        assertFalse(CreditCard.isCardNumberValid("abcdef"));
    }
}
