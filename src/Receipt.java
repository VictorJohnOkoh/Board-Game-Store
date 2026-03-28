import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Receipt {
    private double amount;
    private String email;
    private int cardNumber;
    private String date;
    private Address address;

    public Receipt(double paying, String mail, int card, Address addr){
        amount = paying;
        email = mail;
        cardNumber = card;
        address = addr;
        LocalDate localdate = LocalDate.now();
        date = localdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    }

    public String cardReceipt(){
        return String.format("%.2f paid by Credit Card %d on %s. Billing Address: %s", amount, cardNumber, date, address.toString());
    }

    public String paypalReceipt(){
        return String.format("%.2f paid via PayPal using %s on %s. Billing address: %s", amount, email, date, address.toString());
    }
}
