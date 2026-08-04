package Payment;

import java.util.Scanner;
import Users.Address;

public class CreditCard implements PaymentMethod{
    private int cardNumber;

    public CreditCard(Scanner consoleInput) {
        boolean pass = false;

        // loops as long as the card number doesn't pass verification
        try {
            while (!pass) {
                System.out.print("Enter your 6 digit card number: ");
                cardNumber = consoleInput.nextInt();
                consoleInput.nextLine();
                String test = String.format("%d", cardNumber).replaceAll("\\s+", "");
                if (test.length() != 6) {
                    System.out.println("Incorrect length.");
                } else {
                    pass = true;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Only numerical characters allowed");
        }
        // loops while the security number doesn't pass verification
        pass = false;
        try {
            while (!pass) {
                int securityNumber;
                System.out.print("Enter your 3 digit security number: ");
                securityNumber = consoleInput.nextInt();
                consoleInput.nextLine();
                String test = String.format("%d", securityNumber).replaceAll("\\s+", "");
                if (test.length() != 3) {
                    System.out.println("Incorrect length");
                } else {
                    pass = true;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Only numerical characters allowed");
        }
    }

    public Receipt processPayment(double total, Address address){
        return new CreditCardReceipt(total, address, cardNumber);
    }

}
