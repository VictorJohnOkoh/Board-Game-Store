package Payment;

import Users.Address;

public class CreditCardReceipt extends Receipt{

    int cardNumber;

    public CreditCardReceipt(double amount, Address address, int cardNumber){
        super(amount, address);
        this.cardNumber = cardNumber;
    }

    public String toString(){
        return String.format("£%.2f paid by Credit Card %d on %s. Billing Address: %s\n", super.getAmount(), cardNumber, super.getDate(), super.getAddress().toString());
    }

}
