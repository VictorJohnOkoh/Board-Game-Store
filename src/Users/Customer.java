package Users;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import Inventory.*;
import Payment.CreditCard;
import Payment.PayPal;
import Payment.Receipt;


public class Customer extends User{
    private final ArrayList<Product> basket = new ArrayList<>();
    private final ArrayList<Integer> amount = new ArrayList<>();

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
    }

    // Allows the customer to view the list of available products in descending order of the unit price
    public String viewProducts() throws IOException {
        ArrayList<Product> orderedLines = listProducts();
        StringBuilder output = new StringBuilder();
        for (Product line : orderedLines) {
            if (line.getCategory().equals(ProductCategory.BOARDGAME)) {
                BoardGame game = (BoardGame) line;
                output.append(game.partString());
            } else{
                Accessory accessory = (Accessory) line;
                output.append(accessory.partString());
            }
            output.append("\n");
        }
        return output.toString();
    }

    // Passed a list of products then returns products as strings, for viewing filtered items
    private String viewProducts(ArrayList<Product> products){
        StringBuilder output = new StringBuilder();
        for (Product product : products){
            if (product.getCategory().equals(ProductCategory.BOARDGAME)){
                BoardGame game = (BoardGame) product;
                output.append(game.partString());
                output.append("\n");
            } else {
                Accessory accessory = (Accessory) product;
                output.append(accessory.partString());
                output.append("\n");
            }
        }
            return output.toString();
        }

    // Shows the name, price and quantity of the products in the customer's basket
    public String showBasket(){
        double total = getTotalPrice();
        StringBuilder basketContents = new StringBuilder();
        for (int i = 0; i< basket.size(); i++) {
            Product product = basket.get(i);
            String new_content = (String.format("%-27s %5c £%.2f %c x%d", product.getProductName(), '|',product.getPrice(), '|',amount.get(i)));
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

    public String toString() {
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }

    // Adds an item to the customer's basket
    public void addShopping(int productID) throws IOException {
        boolean found = false;
        int index = 0;
        for (int i = 0; i< basket.size(); i++){
            if (basket.get(i).getProductID() == productID){
                found = true;
                index = i;
            }
        }
        if (found){
            int temp = amount.get(index);
            temp++;
            amount.set(index, temp);

        } else {
            ArrayList<Product> orderedLines = listProducts();
            for (Product product : orderedLines) {
                if (product.getProductID() == productID){
                    basket.add(product);
                    amount.add(1);
                }
            }

        }

    }

    // returns an ArrayList of Products in the Stock.txt file ordered by price
    private ArrayList<Product> listProducts() throws IOException {
        List<String> lines = Files.readAllLines(super.getStockFile().toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        ArrayList<Product> listedProducts = loadProducts(splitlines);
        return super.descOrder(listedProducts);
    }

    // Empties the customer's basket
    public void emptyBasket(){
        System.out.println("Basket cleared\n");
        basket.clear();
    }

    // Removes the amount of the product in the basket from the current stock
    private void updateStock() throws IOException {
        List<String> contents = Files.readAllLines(super.getStockFile().toPath());
        ArrayList<List<String>> splitContents = new ArrayList<>();
        for (String line : contents){
            splitContents.add(List.of(line.split(";")));
        }
        ArrayList<Product> productList = loadProducts(splitContents);
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
        // writes the updates stock to the Stock file
        try(PrintWriter out = new PrintWriter(super.getStockFile())){
            StringBuilder output = new StringBuilder();
            for (int i = 0; i <productList.size(); i++){
                output.append(productList.get(i).toString());
                if (i < productList.size()-1){
                    output.append("\n");
                }
            }
            out.print(output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // processes payment then clears the customer's basket
    public void pay(Scanner consoleInput) throws IOException {
        int choice;

        System.out.println("How would you like to pay?\n1. PayPal\n2. Credit Card\n3. Cancel");
        choice = consoleInput.nextInt();
        consoleInput.nextLine();

        if (choice == 1){
            PayPal paypalInst = new PayPal(consoleInput);
            Receipt receipt = paypalInst.processPayment(getTotalPrice(), getAddress());
            System.out.print(receipt.paypalReceipt());
            updateStock();
        }
        else if (choice == 2) {
            CreditCard creditInst = new CreditCard(consoleInput);
            Receipt receipt = creditInst.processPayment(getTotalPrice(), getAddress());
            System.out.print(receipt.cardReceipt());
            updateStock();
        }
        else if (choice == 3) {
            System.out.println("Payment cancelled");
        }
        else {
            System.out.println("Invalid choice");
        }
        emptyBasket();

    }

    // Filters via compatibility
    public String search(String term) throws IOException {
        ArrayList<Product> orderedProductList = listProducts();
        ArrayList<Product> filteredProducts = new ArrayList<>();
        if (term.equals("e")){
            return "";
        }
        for (Product product : orderedProductList){
            if (product.getCategory().equals(ProductCategory.ACCESSORY)){
                Accessory temp = (Accessory) product;
                if (temp.getCompatibility().toLowerCase().contains(term.toLowerCase())){
                    filteredProducts.add(product);
                }
            }
        }
        return viewProducts(filteredProducts);
    }

    // Filters via product ID
    public String search(int term) throws IOException, NumberFormatException{
        boolean found = false;
        String test = String.format("%d", term).replaceAll("\\s+", "");
        if (test.length() != 4){
            return "Invalid product ID";
        }
        ArrayList<Product> orderedProductList = listProducts();
        ArrayList<Product> filteredProducts = new ArrayList<>();
        for (Product product : orderedProductList){
            if (product.getProductID() == term){
                filteredProducts.add(product);
                found = true;
            }
        }
        if (!found){
            System.out.println("The provided product ID is not in store");
        }
        return viewProducts(filteredProducts);
    }

}
