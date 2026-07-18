package Users;
import Inventory.*;

import java.util.ArrayList;

public class Basket {
    private final ArrayList<Product> basket = new ArrayList<>();
    private final ArrayList<Integer>  amount = new ArrayList<>(); // amount index matches the index of the related product in the basket

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

    public void addToBasket(Product product, int amount) {
        ArrayList<Product> basketItems = getBasket();
        ArrayList<Integer> amounts = getAmounts();

        // Check if item is already in the basket
        for (int i = 0; i < basketItems.size(); i++) {
            if (basketItems.get(i).getProductID() == product.getProductID()) {
                amounts.set(i, amounts.get(i) + amount);
                return;
            }
        }

        // Add new item
        basketItems.add(product);
        amounts.add(amount);
    }

    // adds a product to the basket, if it is already in it then it instead increments the amount in the amount ArrayList
    public void addShopping(Product product, int amount) {
        String result = JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCT_BY_ID, product.getProductID());

        if (result == null || "NOT_FOUND".equals(result)) {
            System.out.println("Product not found in database.");
            return;
        }

        String[] parts = result.split(";");
        // Expected format: id;category;type;name;price;quantity;purchase_cost;extra

        int id = Integer.parseInt(parts[0].trim());
        String category = parts[1].trim();
        String type = parts[2].trim();
        String name = parts[3].trim();
        double price = Double.parseDouble(parts[4].trim());
        int quantity = Integer.parseInt(parts[5].trim());
        double purchaseCost = Double.parseDouble(parts[6].trim());

        if ("boardgame".equalsIgnoreCase(category)) {
            int numPlayers = Integer.parseInt(parts[7].trim());
            // Note: Adjust constructor arguments to match your actual BoardGame class signature
            BoardGame game = new BoardGame(id, type, name, price, purchaseCost, quantity,  numPlayers);
            addToBasket(game, amount);
        } else if ("accessory".equalsIgnoreCase(category)) {
            String compatibility = parts[7].trim();
            AccessoryType accType = parseAccessoryType(type);
            Accessory acc = new Accessory(id, accType, name, price, purchaseCost, quantity, compatibility);
            addToBasket(acc, amount);
        } else {
            System.out.println("Unknown product category: " + category);
        }
    }

    private AccessoryType parseAccessoryType(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "accessory kit": return AccessoryType.accessory_kit;
            case "miniature": return AccessoryType.miniature;
            default: return AccessoryType.dice;
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
