package Inventory;

import java.util.Scanner;
import Users.Address;

public class PayPal implements PaymentMethod{

    private final String email;

    public PayPal (Scanner consoleInput){
        System.out.print("Enter your PayPal email: ");
        email = consoleInput.nextLine();

    }


    @Override
    public Receipt processPayment(double total, Address address) {

        return new Receipt(total, address, email);
    }

}
