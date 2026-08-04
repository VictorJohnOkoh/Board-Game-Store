package Payment;

import Users.Address;

public class PayPalReceipt extends Receipt{

    private String email;

    public PayPalReceipt(double amount, Address address, String email){
        super(amount, address);
        this.email = email;

    }

    public String toString(){
        return String.format("£%.2f paid via PayPal using %s on %s. Billing Address: %s\n", super.getAmount(), email, super.getDate(), super.getAddress().toString());
    }


}
