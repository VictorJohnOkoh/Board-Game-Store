package Payment;

import Users.Address;

public class CreditCard implements PaymentMethod{

    private static final int CARD_NUMBER_LENGTH = 6;
    private static final int SECURITY_CODE_LENGTH = 3;

    private final int cardNumber;
    private final int securityCode;

    /** Takes already-collected card details. Collecting them - from a console or from
     * text fields - is the caller's job, so this class stays usable from both front-ends. */
    public CreditCard(int cardNumber, int securityCode) {
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
    }

    /** True when the input is exactly {@value #CARD_NUMBER_LENGTH} digits. */
    public static boolean isCardNumberValid(String input) {
        return isDigitsOfLength(input, CARD_NUMBER_LENGTH);
    }

    /** True when the input is exactly {@value #SECURITY_CODE_LENGTH} digits. */
    public static boolean isSecurityCodeValid(String input) {
        return isDigitsOfLength(input, SECURITY_CODE_LENGTH);
    }

    private static boolean isDigitsOfLength(String input, int length) {
        if (input == null) {
            return false;
        }
        String trimmed = input.trim();
        return trimmed.length() == length && trimmed.chars().allMatch(Character::isDigit);
    }

    public Receipt processPayment(double total, Address address){
        return new CreditCardReceipt(total, address, cardNumber);
    }

}
