import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Receipt {
    LocalDate localdate = LocalDate.now();
    private double amount;
    private String date = date = localdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));;
    private String email;
    private Address address;
    private int cardNumber;

    public Receipt(double paying, Address addr, String mail){
        amount = paying;
        address = addr;
        email = mail;

    }

    public Receipt(double paying, Address addr, int card){
        amount = paying;
        address = addr;
        cardNumber = card;
    }

    public String cardReceipt(){
        return String.format("£%.2f paid by Credit Card %d on %s. Billing Address: %s", amount, cardNumber, date, address.toString());
    }

    public String paypalReceipt(){
        return String.format("£%.2f paid via PayPal using %s on %s. Billing address: %s", amount, email, date, address.toString());
       }
}
