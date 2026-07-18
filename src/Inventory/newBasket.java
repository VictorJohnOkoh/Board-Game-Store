package Inventory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a shopping basket that can hold both {@link BoardGame} and
 * {@link Accessory} products (or any other {@link Product} subtype).
 * The basket tracks each distinct product together with the quantity of
 * that product currently placed in the basket.
 */
public class newBasket {

    // Preserves insertion order so the basket prints in the order items were added.
    private final Map<Product, Integer> items = new LinkedHashMap<>();

    /**
     * Adds {@code amount} units of {@code product} to the basket.
     *
     * If the product is not already in the basket, it is added with its
     * amount set to {@code amount} (as long as stock allows it).
     * If the product is already in the basket, {@code amount} is added to
     * its current basket quantity.
     *
     * If the resulting total quantity for that product would exceed the
     * product's {@code quantityInStock}, the whole operation is cancelled
     * (the basket is left unchanged) and a message is printed to the CLI.
     *
     * @param pid the ID for the product to add
     * @param amount  how many units to add
     */
    public void addShopping(int pid, int amount) {
        Product product;
        String productData = JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCT_BY_ID, pid);
        assert productData != null;
        if (!productData.equals("NOT_FOUND")){
            product = Product.buildProduct(productData);
        } else {
            System.out.println("The product wasn't found, try entering the correct Product ID again");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount to add must be greater than zero.");
            return;
        }

        int currentAmount = items.getOrDefault(product, 0);
        int newTotal = currentAmount + amount;

        if (newTotal > product.getQuantityInStock()) {
            System.out.println("Transaction cancelled: not enough stock for \""
                    + product.getProductName() + "\". Requested: " + newTotal
                    + ", In stock: " + product.getQuantityInStock());
            return;
        }

        items.put(product, newTotal);
    }

    /** Convenience overload that adds a single unit of the product. */
    public void addShopping(int pid) {
        addShopping(pid, 1);
    }

    /**
     * Removes {@code amount} units of {@code product} from the basket.
     * If the resulting amount is zero or less, the product is removed
     * entirely from the basket.
     */
    public void removeProduct(Product product, int amount) {
        if (!items.containsKey(product)) {
            System.out.println("\"" + product.getProductName() + "\" is not in the basket.");
            return;
        }

        int currentAmount = items.get(product);
        int newAmount = currentAmount - amount;

        if (newAmount <= 0) {
            items.remove(product);
        } else {
            items.put(product, newAmount);
        }
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

    /** Returns the product ID and the amount in the format 'id1:amount;id2:amount;...'
     * to be passed to update the amount of the product in stock
     * */
    public String getProductAmount(){
        StringBuilder contents = new StringBuilder();
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            int pid = entry.getKey().getProductID();
            int amount = entry.getValue();
            String newProduct = pid + ":" + amount;
            if (items.entrySet().iterator().hasNext()){
                newProduct += ";";
            }
            contents.append(newProduct);
        }
        return contents.toString();
    }
}
