package Users;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import Inventory.Product;
import Payment.CreditCard;
import Payment.PayPal;
import Payment.PaymentMethod;
import Payment.Receipt;
import Bridge.JavaPythonBridge;


public class Customer extends User{
    public final newBasket basket = new newBasket();

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
    }


    /** Prints all the products a customer can see to the CLI*/
    public void viewProducts(){
        System.out.println(JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCTS));
    }

        /** Prints the customer's basket to the CLI*/
    public void showBasket(){
        basket.printBasket();
    }

    /** Returns a string representation of the customer*/
    public String toString() {
        return String.format("%d | %s | %s", getUserID(), getName(), getRole());
    }


    /** Passes the product ID and amounts to update the amount of stock.
     * Returns true only when the database reported a clean update. */
    private boolean updateStock() {
        String result = JavaPythonBridge.run_result(JavaPythonBridge.UPDATE_STOCK, basket.getProductAmount());
        return "Success".equals(result);
    }


    /**
     * Runs the checkout for an already-built payment method: takes the payment, saves the
     * receipt, updates stock, then empties the basket.
     * <p>
     * A receipt that cannot be written to disk is not fatal - the sale still completes and
     * {@link Receipt#getSavedPath()} stays null so the caller can warn. A failed stock update
     * is fatal: the basket is left untouched and this throws, so a customer never loses their
     * basket to a sale the database did not record.
     *
     * @return the receipt for the completed sale
     * @throws CheckoutException when stock could not be updated
     */
    public Receipt checkout(PaymentMethod method) throws CheckoutException {
        Receipt receipt = method.processPayment(basket.getTotalPrice(), getAddress());

        // Copied because the basket is emptied below, and the receipt records what was sold.
        Map<Product, Integer> soldItems = new LinkedHashMap<>(basket.getItems());
        try {
            receipt.save(soldItems);
        } catch (IOException e) {
            System.out.println("Receipt could not be saved: " + e.getMessage());
        }

        if (!updateStock()) {
            throw new CheckoutException("Stock could not be updated - the sale was not completed.");
        }

        basket.emptyBasket();
        return receipt;
    }


    /** Prompts for a payment method on the CLI, collects the details, then runs the checkout. */
    public void pay(Scanner consoleInput) {
        System.out.println("How would you like to pay?\n1. PayPal\n2. Credit Card\n3. Cancel");
        int choice;
        try {
            choice = Integer.parseInt(consoleInput.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice");
            return;
        }

        PaymentMethod method;
        if (choice == 1){
            method = new PayPal(promptForEmail(consoleInput));
        }
        else if (choice == 2) {
            int cardNumber = promptForDigits(consoleInput, "Enter your 6 digit card number: ", true);
            int securityCode = promptForDigits(consoleInput, "Enter your 3 digit security number: ", false);
            method = new CreditCard(cardNumber, securityCode);
        }
        else if (choice == 3) {
            System.out.println("Payment cancelled");
            return;
        }
        else {
            System.out.println("Invalid choice");
            return;
        }

        try {
            Receipt receipt = checkout(method);
            System.out.println(receipt.toString());
            if (receipt.getSavedPath() != null) {
                System.out.println("Receipt saved to " + receipt.getSavedPath());
            }
        } catch (CheckoutException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Loops until the console supplies a valid PayPal email. */
    private String promptForEmail(Scanner consoleInput) {
        while (true) {
            System.out.print("Enter your PayPal email: ");
            String email = consoleInput.nextLine();
            if (PayPal.isEmailValid(email)) {
                return email;
            }
            System.out.println("Invalid email address provided\n");
        }
    }

    /** Loops until the console supplies a card number or security code of the right length. */
    private int promptForDigits(Scanner consoleInput, String prompt, boolean isCardNumber) {
        while (true) {
            System.out.print(prompt);
            String input = consoleInput.nextLine();
            boolean valid = isCardNumber ? CreditCard.isCardNumberValid(input) : CreditCard.isSecurityCodeValid(input);
            if (valid) {
                return Integer.parseInt(input.trim());
            }
            System.out.println("Incorrect length.\n");
        }
    }

    // Filters via compatibility
    public String search(String term) {
        return JavaPythonBridge.run_result(JavaPythonBridge.FILTER_COMPATIBILITY, term);
    }

    // Filters via product ID
    public String search(int term) throws NumberFormatException{
        return JavaPythonBridge.run_result(JavaPythonBridge.FILTER_ID, term);

    }

}
