/*TO-DO
* Pay method should update stock
* Add search method
*   Search for product ID
*   Search for compatibility
* */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer extends User{
    private ArrayList<Product> basket;

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
        basket = new ArrayList<>();
    }

    // Allows the customer to view the list of available products in descending order of the unit price
    public StringBuilder viewProducts(File stockFile) throws IOException {
        List<String> lines = Files.readAllLines(stockFile.toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        String[][] orderedLines = super.descOrder(splitlines);
        StringBuilder output = new StringBuilder();
        for (String[] orderedLine : orderedLines) {
            for (int j = 0; j < orderedLine.length; j++) {
                // Prevents the customer from being able to see the purchase cost
                if (j == 6) {
                    continue;
                }
                output.append(orderedLine[j]);
                if (j < orderedLine.length-2) {
                    output.append(",");
                }
            }
            output.append("\n");
        }
        return output;
    }

    // Shows the contents of the customer's basket
    public String showBasket(){
        double total = 0;
        StringBuilder basketContents = new StringBuilder();
        for (Product product : basket) {
            total += product.getPrice();
            String new_content = (String.format("%20s %5c £%.2f", product.getProductName(), '|',product.getPrice()));
            basketContents.append(new_content);
            basketContents.append("\n");
        }
        basketContents.append(String.format("---------------------\nTotal : £%.2f", total));
        return basketContents.toString();
    }

    public double getTotalPrice(){
        double total = 0;
        for (Product product : basket){
            total += product.getPrice();
        }
        return total;
    }

    // Adds an item to the customer's basket
    public void addShopping(Product item){
        basket.add(item);
    }

    // Empties the customer's basket
    public void emptyBasket(){
        basket.clear();
    }
    
    public void pay(){
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.print("How would you like to pay?\n1. PayPal\n2. Credit Card\n3. Cancel\nChoice: ");
        choice = scanner.nextInt();
        if (choice == 1){
            PayPal paypalInst = new PayPal();
            Receipt receipt = paypalInst.processPayment(getTotalPrice(), getAddress());
            System.out.print(receipt.paypalReceipt());
        } else if (choice == 2) {
            CreditCard creditInst = new CreditCard();
            Receipt receipt = creditInst.processPayment(getTotalPrice(), getAddress());
            System.out.print(receipt.cardReceipt());
        } else if (choice == 3) {
            System.out.print("Payment cancelled");
        } else {
            System.out.println("Invalid choice");
        }
    }

//    public void search(String term, File stockFile){
//        try (Scanner scanner = new Scanner(stockFile)){
//
//        }
//    }

}
