public interface PaymentMethod {

    Receipt processPayment(double total, Address address);
}
