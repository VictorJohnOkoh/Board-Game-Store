package Users;

import Inventory.Accessory;
import Inventory.BoardGame;
import Bridge.JavaPythonBridge;
import Inventory.Product;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Represents a shopping basket that can hold both {@link BoardGame} and
 * {@link Accessory} products (or any other {@link Product} subtype).
 * The basket tracks each distinct product together with the quantity of
 * that product currently placed in the basket.
 */
public class newBasket {

    /**
     * The outcome of an {@link #addShopping(int, int)} call, so callers can tell
     * the customer why an add did not happen. Returning this instead of printing
     * keeps the basket usable from both the CLI and the GUI - System.out messages
     * are invisible in a JavaFX window.
     */
    public enum AddResult {
        ADDED,
        NOT_FOUND,
        INVALID_AMOUNT,
        INSUFFICIENT_STOCK
    }

    // Preserves insertion order so the basket prints in the order items were added.
    private final Map<Product, Integer> items = new LinkedHashMap<>();

    /**
     * Adds {@code amount} units of {@code product} to the basket.
     * <p>
     * If the product is not already in the basket, it is added with its
     * amount set to {@code amount} (as long as stock allows it).
     * If the product is already in the basket, {@code amount} is added to
     * its current basket quantity.
     * <p>
     * If the resulting total quantity for that product exceeds the
     * product's {@code quantityInStock}, the whole operation is cancelled
     * and the basket is left unchanged.
     *
     * @param pid the ID for the product to add
     * @param amount  how many units to add
     * @return what happened, so the caller can report it to the user
     */
    public AddResult addShopping(int pid, int amount) {
        String productData = JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCT_BY_ID, pid);
        if (productData == null || productData.equals("NOT_FOUND")) {
            return AddResult.NOT_FOUND;
        }
        return addShopping(Product.buildProduct(productData), amount);
    }

    /**
     * The basket rules, once the product has been looked up. Kept separate from the
     * database lookup above so the quantity and stock rules can be exercised directly.
     */
    AddResult addShopping(Product product, int amount) {
        if (amount <= 0) {
            return AddResult.INVALID_AMOUNT;
        }

        int currentAmount = items.getOrDefault(product, 0);
        int newTotal = currentAmount + amount;

        if (newTotal > product.getQuantityInStock()) {
            return AddResult.INSUFFICIENT_STOCK;
        }

        items.put(product, newTotal);
        return AddResult.ADDED;
    }

    /** Convenience overload that adds a single unit of the product. */
    public AddResult addShopping(int pid) {
        return addShopping(pid, 1);
    }

    /**
     * Removes {@code amount} units of {@code product} from the basket.
     * If the resulting amount is zero or less, the product is removed
     * entirely from the basket.
     *
     * @return false when the product was not in the basket, so the caller can say so in
     *         its own words - this is reached from the GUI, where a printed line is lost
     */
    public boolean removeProduct(Product product, int amount) {
        if (!items.containsKey(product)) {
            return false;
        }

        int currentAmount = items.get(product);
        int newAmount = currentAmount - amount;

        if (newAmount <= 0) {
            items.remove(product);
        } else {
            items.put(product, newAmount);
        }
        return true;
    }

    /** Empties the basket completely. */
    public void emptyBasket() {
        items.clear();
    }

    /** Returns the quantity of the given product currently in the basket (0 if absent). */
    public int getAmount(Product product) {
        return items.getOrDefault(product, 0);
    }

    /** Returns true if the basket contains no items. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Returns the total price of a single product line (price * amount in basket). */
    public double getTotalPriceForProduct(Product product) {
        Integer amount = items.get(product);
        if (amount == null) {
            return 0.0;
        }
        return product.getPrice() * amount;
    }

    /** Returns the total price of everything currently in the basket. */
    public double getTotalPrice() {
        double total = 0.0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    /** Returns an unmodifiable view of the basket contents (product -> amount). */
    public Map<Product, Integer> getItems() {
        return java.util.Collections.unmodifiableMap(items);
    }

    /** Prints the current contents of the basket, including per-line and grand totals. */
    public void printBasket() {
        if (items.isEmpty()) {
            System.out.println("The basket is empty.");
            return;
        }

        System.out.println("----- Basket -----");
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int amount = entry.getValue();
            double lineTotal = product.getPrice() * amount;
            System.out.printf("%-40s x%-3d  £%.2f%n", product.getProductName(), amount, lineTotal);
        }
        System.out.println("------------------");
        System.out.printf("Total: £%.2f%n", getTotalPrice());
    }

    /** Returns the product ID and the amount in the format 'id1:amount;id2:amount; ...'
     * to be passed to update the amount of the product in stock
     * */
    public String getProductAmount(){
        StringBuilder contents = new StringBuilder();
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            if (!contents.isEmpty()) {
                contents.append(';');
            }
            contents.append(entry.getKey().getProductID()).append(':').append(entry.getValue());
        }
        return contents.toString();
    }
}
