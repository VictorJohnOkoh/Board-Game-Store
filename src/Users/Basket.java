package Users;
import Inventory.Product;
import Inventory.Stock;

import java.util.ArrayList;

public class Basket {
    private final ArrayList<Product> basket = new ArrayList<>();
    private final ArrayList<Integer> amount = new ArrayList<>(); // amount index matches the index of the related product in the basket

    public ArrayList<Product> getBasket() {
        return basket;
    }

    public ArrayList<Integer> getAmounts(){
        return amount;
    }

    // Shows the name, price and quantity of the products in the customer's basket
    public String showBasket(){
        StringBuilder basketContents = new StringBuilder();
        for (int i = 0; i< basket.size(); i++){
            Product product = basket.get(i);
            String new_content = (String.format("%-27s %5c £%.2f %c x%d", product.getProductName(), '|',product.getPrice(), '|',amount.get(i)));
            basketContents.append(new_content);
            basketContents.append("\n");
        }
        basketContents.append(String.format("--------------------------------\nTotal : £%.2f", getTotalPrice()));
        return basketContents.toString();
    }

    // empties the basket
    public void emptyBasket(){
        basket.clear();
        amount.clear();
        System.out.println("Basket empty");
    }

    // adds a product to the basket, if it is already in it then it instead increments the amount in the amount ArrayList
    public void addShopping(int productID){
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
            ArrayList<Product> orderedLines = Stock.getLoadedProducts();
            for (Product product : orderedLines) {
                if (product.getProductID() == productID){
                    basket.add(product);
                    amount.add(1);
                }
            }

        }
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

}
