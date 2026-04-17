package Payment;

import java.util.Scanner;
import Users.Address;

public class CreditCard implements PaymentMethod{
    private int cardNumber;

    public CreditCard(Scanner consoleInput) {
        boolean pass = false;


        while (!pass) {
            System.out.print("Enter your 6 digit card number: ");
            cardNumber = consoleInput.nextInt();
            String test = String.format("%d", cardNumber).replaceAll("\\s+", "");
            if (test.length() != 6) {
                System.out.println("Incorrect length.");
            } else {
                pass = true;
            }
        }
        pass = false;
        while (!pass) {
            int securityNumber;
            System.out.print("Enter your 3 digit security number: ");
            securityNumber = consoleInput.nextInt();
            String test = String.format("%d", securityNumber).replaceAll("\\s+", "");
            if (test.length() != 3) {
                System.out.println("Incorrect length");
            } else {
                pass = true;
            }
        }
    }

    public Receipt processPayment(double total, Address address){
        return new Receipt(total, address, cardNumber);
    }

}
