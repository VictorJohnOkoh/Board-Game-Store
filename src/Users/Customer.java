package Users;

import java.util.Scanner;
import Inventory.*;
import Payment.CreditCard;
import Payment.PayPal;
import Payment.Receipt;


public class Customer extends User{
    public final newBasket basket = new newBasket();

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
    }


    /** Prints all the products a customer can see to the CLI*/
    public void viewProducts(){
        System.out.println(JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCTS));
    }

        /** Prints the customer's basket to the CLI*/
    public void showBasket(){
        basket.printBasket();
    }

    /** Returns a string representation of the customer*/
    public String toString() {
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }


    /** Passes the product ID and amounts to update the amount of stock*/
    private void updateStock() {
        System.out.println(JavaPythonBridge.run_result(JavaPythonBridge.UPDATE_STOCK, basket.getProductAmount()));
    }


    /** processes payment then clears the customer's basket*/
    public void pay(Scanner consoleInput) {
        int choice;


        System.out.println("How would you like to pay?\n1. PayPal\n2. Credit Card\n3. Cancel");
        choice = consoleInput.nextInt();
        consoleInput.nextLine();

        if (choice == 1){
            PayPal paypalInst = new PayPal(consoleInput);
            Receipt receipt = paypalInst.processPayment(basket.getTotalPrice(), getAddress());
            System.out.println(receipt.toString());
            updateStock();
            basket.emptyBasket();

		}
        else if (choice == 2) {
            CreditCard creditInst = new CreditCard(consoleInput);
            Receipt receipt = creditInst.processPayment(basket.getTotalPrice(), getAddress());
            System.out.println(receipt.toString());
            updateStock();
            basket.emptyBasket();

		}
        else if (choice == 3) {
            System.out.println("Payment cancelled");
        }
        else {
            System.out.println("Invalid choice");
        }


    }

    // Filters via compatibility
    public String search(String term) {
        return JavaPythonBridge.run_result(JavaPythonBridge.FILTER_COMPATIBILITY, term);
    }

    // Filters via product ID
    public String search(int term) throws NumberFormatException{
        return JavaPythonBridge.run_result(JavaPythonBridge.FILTER_ID, term);

    }

}
