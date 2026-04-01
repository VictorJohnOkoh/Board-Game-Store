import java.util.Scanner;

public class PayPal implements PaymentMethod{

    private final String email;

    public PayPal (){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your PayPal email: ");
        email = scanner.next();
    }


    @Override
    public Receipt processPayment(double total, Address address) {

        return new Receipt(total, address, email);
    }

    public String getEmail(){
        return email;
    }
}
