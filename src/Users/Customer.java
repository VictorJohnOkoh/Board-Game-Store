package Users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import Inventory.*;
import Payment.CreditCard;
import Payment.PayPal;
import Payment.Receipt;

/*
 * stop customer from buying more products than in stock
 * stop customer from buying products out of stock
 */

public class Customer extends User{
    public final Basket basket = new Basket();

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
    }

    // Allows the customer to view the list of available products in descending order of the unit price
    public String viewProducts() {
        return User.stockClass.showStockCustomer();
    }

    // Passed a list of products then returns products as strings, for viewing filtered items
    private String viewProducts(ArrayList<Product> products){
        StringBuilder output = new StringBuilder();
        for (Product product : products){
            if (product.getProductCategory().equals(ProductCategory.BOARDGAME)){
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


    public String showBasket(){
        return basket.showBasket();
    }

    public String toString() {
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }


    // Removes the amount of the product in the basket from the current stock
    private void updateStock() throws IOException {
        User.stockClass.updateStock(basket);
    }


    // processes payment then clears the customer's basket
    public void pay(Scanner consoleInput) throws IOException {
        int choice;

        if (!Stock.checkStock(this.basket)){
            return;
        }

        System.out.println("How would you like to pay?\n1. PayPal\n2. Credit Card\n3. Cancel");
        choice = consoleInput.nextInt();
        consoleInput.nextLine();

        if (choice == 1){
            PayPal paypalInst = new PayPal(consoleInput);
            Receipt receipt = paypalInst.processPayment(basket.getTotalPrice(), getAddress());
            System.out.print(receipt.paypalReceipt());
            updateStock();
            basket.emptyBasket();

		}
        else if (choice == 2) {
            CreditCard creditInst = new CreditCard(consoleInput);
            Receipt receipt = creditInst.processPayment(basket.getTotalPrice(), getAddress());
            System.out.print(receipt.cardReceipt());
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
    public String search(String term) throws IOException {
        ArrayList<Product> orderedProductList = Stock.getLoadedProducts();
        ArrayList<Product> filteredProducts = new ArrayList<>();
        if (term.equals("e")){
            return "";
        }
        for (Product product : orderedProductList){
            if (product.getProductCategory().equals(ProductCategory.ACCESSORY)){
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
        ArrayList<Product> orderedProductList = Stock.getLoadedProducts();
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
