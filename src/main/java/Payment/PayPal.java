package Payment;

import java.util.Scanner;
import java.util.regex.Pattern;

import Users.Address;


public class PayPal implements PaymentMethod{

    private String email;

    public PayPal (Scanner consoleInput){
        boolean pass = false;

        while (!pass) {
            System.out.print("Enter your PayPal email: ");
            email = consoleInput.nextLine();
            if (!isEmailValid(email)) {
                System.out.println("Invalid email address provided\n");
            }
        }

    }

    /** Checks if the email address provided has a valid domain and format*/
    private boolean isEmailValid(String email){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-+]+(\\\\.[A-Za-z0-9_-+]+)*@[^-][A-Za-z0-9-+]+(\\\\.[A-Za-z0-9-+]+)*(\\\\.[A-Za-z]{2,})$";
        return Pattern.matches(regexPattern, email);
    }

    @Override
    public Receipt processPayment(double total, Address address) {
        return new PayPalReceipt(total, address, email);
    }

}
