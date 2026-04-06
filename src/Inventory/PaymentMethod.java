package Inventory;
import Users.Address;

public interface PaymentMethod {

    Receipt processPayment(double total, Address address);
}
