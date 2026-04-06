/*TO-DO
* Update pay method to update stock
* Add search method
*   Search for product ID
*   Search for compatibility
* */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer extends User{
    private final ArrayList<Product> basket = new ArrayList<>();
    private final ArrayList<Integer> amount = new ArrayList<>();

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
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

    // Shows the name, price and quantity of the products in the customer's basket
    public String showBasket(){
        double total = getTotalPrice();
        StringBuilder basketContents = new StringBuilder();
        for (int i = 0; i< basket.size(); i++) {
            Product product = basket.get(i);
            String new_content = (String.format("%-27s %5c £%.2f x%d", product.getProductName(), '|',product.getPrice(), amount.get(i)));
            basketContents.append(new_content);
            basketContents.append("\n");
        }
        basketContents.append(String.format("--------------------------------\nTotal : £%.2f", total));
        return basketContents.toString();
    }

    // gets total price of products in the basket
    public double getTotalPrice(){
        double total = 0;
        for (int i = 0; i< basket.size();i++){
            Product product = basket.get(i);
            total += (product.getPrice()) * amount.get(i);
        }
        return total;
    }

    // Adds an item to the customer's basket
    public void addShopping(Product item){
        boolean found = false;
        int index = 0;
        for (int i = 0; i< basket.size(); i++){
            if (basket.get(i).getProductID() == item.getProductID()){
                found = true;
                index = i;
            }
        }
        if (found){
            int temp = amount.get(index);
            temp++;
            amount.set(index, temp);

        } else {
            basket.add(item);
            amount.add(1);
        }

    }

    // Empties the customer's basket
    public void emptyBasket(){
        basket.clear();
    }

    private void updateStock() throws IOException {
        File stockFile = new File("Stock.txt");
        List<String> contents = Files.readAllLines(stockFile.toPath());
        ArrayList<List<String>> splitContents = new ArrayList<>();
        for (String line : contents){
            splitContents.add(List.of(line.split(";")));
        }
        ArrayList<Product> productList = loadProducts(splitContents);
        for (Product product : productList){
            System.out.println(product.toString());
        }
        for (int i = 0; i<basket.size(); i++){
            Product basketContent = basket.get(i);
            for (Product product : productList) {
                if (basketContent.getProductID() == product.getProductID()) {
                    int oldStock = product.getQuantityInStock();
                    int boughtStock = amount.get(i);
                    product.setQuantityInStock(oldStock - boughtStock);
                }
            }
        }
        try(PrintWriter out = new PrintWriter(stockFile)){
            StringBuilder output = new StringBuilder();
            for (int i = 0; i <basket.size(); i++){
                output.append(productList.get(i).toString());
                if (i < basket.size()-1){
                    output.append("\n");
                }
            }
            out.print(output);
        }
    }

    private static ArrayList<Product> loadProducts(ArrayList<List<String>> splitContents) {
        ArrayList<Product> productList = new ArrayList<>();
        for (List<String> list : splitContents) {
            if (list.get(1).trim().equals("board game")) {
                BoardGame newGame = new BoardGame(list.toArray(new String[0]));
                productList.add(newGame);
            } else {
                Accessory newAccessory = new Accessory(list.toArray(new String[0]));
                productList.add(newAccessory);
            }
        }
        return productList;
    }

    public void pay() throws IOException {
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
            updateStock();

        } else if (choice == 3) {
            System.out.print("Payment cancelled");
        } else {
            System.out.println("Invalid choice");
        }
        emptyBasket();
    }

    public void search(String term, File stockFile){
        try (Scanner scanner = new Scanner(stockFile)){

        } catch (FileNotFoundException e){
            System.out.println("Error: Could not find/access the stock file");
        }
    }

}
