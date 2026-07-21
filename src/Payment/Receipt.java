package Payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import Users.Address;

//TODO: write out the subclasses

public abstract class Receipt {
    LocalDate localdate = LocalDate.now();
    private final String date  = localdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private final double amount;
    private final Address address;


    public Receipt(double amount, Address address){
        this.amount = amount;
        this.address = address;
    }


    protected double getAmount(){
        return amount;
    }

    protected Address getAddress(){
        return address;
    }

    protected String getDate(){
        return date;
    }

    public abstract String toString();
}
