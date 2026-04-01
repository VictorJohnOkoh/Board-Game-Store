import java.util.Scanner;

public class CreditCard implements PaymentMethod{

    private int cardNumber;
    private int securityNumber;

    public CreditCard() {
        boolean pass = false;
        Scanner scanner = new Scanner(System.in);

        while (!pass) {
            System.out.print("Enter your 6 digit card number: ");
            cardNumber = scanner.nextInt();
            String test = String.format("%d", cardNumber).replaceAll("\\s+", "");
            if (test.length() != 6) {
                System.out.println("Incorrect length.");
            } else {
                pass = true;
            }
        }
        pass = false;
        while (!pass) {
            System.out.print("Enter your 3 digit security number: ");
            securityNumber = scanner.nextInt();
            String test = String.format("%d", securityNumber).replaceAll("\\s+", "");
            if (test.length() != 3) {
                System.out.println("Incorrect length");
            } else {
                pass = true;
            }
        }
        scanner.close();
    }

    public Receipt processPayment(double total, Address address){
        return new Receipt(total, address, cardNumber);
    }

    public int getCardNumber() {
        return cardNumber;
    }
}
