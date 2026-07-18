package Payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import Users.Address;

public class Receipt {
    LocalDate localdate = LocalDate.now();
    private final String date  = localdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private final double amount;
    private String email;
    private final Address address;
    private int cardNumber;

    // constructor for paying via PayPal
    public Receipt(double paying, Address addr, String mail){
        amount = paying;
        address = addr;
        email = mail;

    }

    // constructor for paying via Credit/Debit card
    public Receipt(double paying, Address addr, int card){
        amount = paying;
        address = addr;
        cardNumber = card;
    }

    public String cardReceipt(){
        return String.format("£%.2f paid by Credit Card %d on %s. Billing Users.Address: %s\n", amount, cardNumber, date, address.toString());
    }

    public String paypalReceipt(){
        return String.format("£%.2f paid via PayPal using %s on %s. Billing address: %s\n", amount, email, date, address.toString());
       }
}
