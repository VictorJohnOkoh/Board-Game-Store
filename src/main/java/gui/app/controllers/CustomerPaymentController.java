package gui.app.controllers;

import Payment.CreditCard;
import Payment.PayPal;
import Payment.PaymentMethod;
import Payment.Receipt;
import Users.CheckoutException;
import Users.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Collects payment details for the chosen method and runs the checkout.
 * <p>
 * One screen serves both methods: the field group for the method in use is shown and
 * the other is hidden, so the Cancel/Pay buttons and the outcome handling live in one place.
 */
public class CustomerPaymentController {

    public enum Method {
        PAYPAL("PayPal"),
        CREDIT_CARD("Credit Card");

        private final String label;

        Method(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private Customer currentUser;
    private Method method;

    @FXML private Text titleText;
    @FXML private Label amountLabel;
    @FXML private VBox payPalFields;
    @FXML private VBox creditCardFields;
    @FXML private TextField emailField;
    @FXML private Label emailError;
    @FXML private TextField cardNumberField;
    @FXML private Label cardNumberError;
    @FXML private TextField securityCodeField;
    @FXML private Label securityCodeError;
    @FXML private Button payButton;

    public void initialize(Customer customer, Method method) {
        this.currentUser = customer;
        this.method = method;

        titleText.setText("Pay with " + method.label());
        amountLabel.setText(String.format("Amount due: £%.2f", customer.basket.getTotalPrice()));
        payButton.setText("Pay");

        boolean isPayPal = method == Method.PAYPAL;
        showFields(payPalFields, isPayPal);
        showFields(creditCardFields, !isPayPal);
        clearErrors();
    }

    /** Hiding alone would leave the space reserved, so managed is cleared too. */
    private void showFields(VBox fields, boolean visible) {
        fields.setVisible(visible);
        fields.setManaged(visible);
    }

    private void clearErrors() {
        emailError.setText("");
        cardNumberError.setText("");
        securityCodeError.setText("");
    }

    @FXML
    private void handlePay() {
        clearErrors();

        PaymentMethod paymentMethod = method == Method.PAYPAL ? buildPayPal() : buildCreditCard();
        if (paymentMethod == null) {
            return; // validation errors are already on screen
        }

        try {
            Receipt receipt = currentUser.checkout(paymentMethod);
            CustomerNavigation.showInfo("Payment Successful", describeReceipt(receipt));
            returnToMenu();
        } catch (CheckoutException e) {
            // The basket is deliberately still intact here.
            CustomerNavigation.showError("Payment Not Completed", e.getMessage()
                    + "\n\nYour basket has been kept so you can try again.");
        }
    }

    /** Adds where the receipt was filed, or warns when it could not be written. */
    private String describeReceipt(Receipt receipt) {
        if (receipt.getSavedPath() == null) {
            return receipt.toString()
                    + "\nYour payment went through, but the receipt could not be saved to disk.";
        }
        return receipt.toString() + "\nSaved to " + receipt.getSavedPath();
    }

    /** @return the payment method, or null when the input was invalid */
    private PayPal buildPayPal() {
        String email = emailField.getText();
        if (!PayPal.isEmailValid(email)) {
            emailError.setText("Enter a valid email address");
            return null;
        }
        return new PayPal(email);
    }

    /** @return the payment method, or null when the input was invalid */
    private CreditCard buildCreditCard() {
        String cardNumber = cardNumberField.getText();
        String securityCode = securityCodeField.getText();

        boolean valid = true;
        if (!CreditCard.isCardNumberValid(cardNumber)) {
            cardNumberError.setText("Enter a 6 digit card number");
            valid = false;
        }
        if (!CreditCard.isSecurityCodeValid(securityCode)) {
            securityCodeError.setText("Enter a 3 digit security code");
            valid = false;
        }

        // Both fields are checked before returning so the customer sees every problem at once.
        if (!valid) {
            return null;
        }
        return new CreditCard(Integer.parseInt(cardNumber.trim()), Integer.parseInt(securityCode.trim()));
    }

    /** Cancelling goes back to the basket, not the menu - the customer was mid-purchase. */
    @FXML
    private void handleCancel() {
        try {
            CustomerPayController controller =
                    CustomerNavigation.swap(payButton, "customer-pay.fxml", 800, 600);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not return to the basket: " + e.getMessage());
        }
    }

    private void returnToMenu() {
        try {
            CustomerMenuController controller =
                    CustomerNavigation.swap(payButton, "customer-menu.fxml", 700, 500);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not return to menu: " + e.getMessage());
        }
    }
}
