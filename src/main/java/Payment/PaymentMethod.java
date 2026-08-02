package main.java.Payment;
import main.java.Users.Address;

public interface PaymentMethod {

    Receipt processPayment(double total, Address address);
}
