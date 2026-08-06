package Payment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import Bridge.AppPaths;
import Inventory.Product;
import Users.Address;


public abstract class Receipt {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Colons are illegal in Windows filenames, so the time is hyphen-separated.
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final LocalDateTime createdAt = LocalDateTime.now();
    private final String date = createdAt.format(DISPLAY_DATE);
    private final double amount;
    private final Address address;

    /** Where this receipt was written, or null if it has not been saved. */
    private Path savedPath;


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

    /** The file this receipt was written to, or null if saving did not happen or failed. */
    public Path getSavedPath() {
        return savedPath;
    }

    public abstract String toString();

    /**
     * Writes this receipt as a markdown file into {@code data/receipts}, named for the
     * moment it was created.
     * <p>
     * The items are passed in because a receipt is built from a total, not from a basket -
     * the caller must supply them before the basket is emptied.
     *
     * @param items the products sold and their quantities
     * @return the file that was written
     * @throws IOException if the directory or file could not be written
     */
    public Path save(Map<Product, Integer> items) throws IOException {
        return save(AppPaths.receiptsDir(), items);
    }

    /** Saves into a given directory. Exists so tests can write somewhere disposable
     * instead of the app's real data directory. */
    Path save(Path directory, Map<Product, Integer> items) throws IOException {
        Files.createDirectories(directory);

        Path target = nextAvailablePath(directory);
        Files.writeString(target, toMarkdown(items), StandardCharsets.UTF_8);
        savedPath = target;
        return target;
    }

    /** Two receipts in the same second would otherwise overwrite each other. */
    private Path nextAvailablePath(Path directory) {
        String stamp = createdAt.format(FILE_TIMESTAMP);
        Path candidate = directory.resolve(stamp + ".md");

        int suffix = 2;
        while (Files.exists(candidate)) {
            candidate = directory.resolve(stamp + "-" + suffix + ".md");
            suffix++;
        }
        return candidate;
    }

    /** Renders the receipt as markdown, itemising what was bought. */
    String toMarkdown(Map<Product, Integer> items) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Receipt - ").append(createdAt.format(DISPLAY_TIMESTAMP)).append("\n\n");

        if (items == null || items.isEmpty()) {
            markdown.append("_No items recorded._\n\n");
        } else {
            markdown.append("| Product | Qty | Total |\n");
            markdown.append("|---------|-----|-------|\n");
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                markdown.append(String.format("| %s | %d | £%.2f |%n",
                        product.getProductName(), quantity, product.getPrice() * quantity));
            }
            markdown.append('\n');
        }

        markdown.append(toString().trim()).append('\n');

        return markdown.toString();
    }
}
