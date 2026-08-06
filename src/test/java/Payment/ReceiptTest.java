package Payment;

import Inventory.BoardGame;
import Inventory.Product;
import Users.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptTest {

    private static final Address ADDRESS = new Address(12, "AB1 2CD", "Leeds");

    private static Product boardGame(int id, String name, double price) {
        return new BoardGame(id, "strategy", name, price, 10.0, 20, 4);
    }

    private static Map<Product, Integer> basket() {
        Map<Product, Integer> items = new LinkedHashMap<>();
        items.put(boardGame(1, "Catan", 34.99), 2);
        items.put(boardGame(2, "Ticket to Ride", 4.99), 1);
        return items;
    }

    @Test
    @DisplayName("markdown itemises each product with its quantity and line total")
    void markdownItemisesProducts() {
        Receipt receipt = new PayPalReceipt(74.97, ADDRESS, "victor@gmail.com");

        String markdown = receipt.toMarkdown(basket());

        assertTrue(markdown.startsWith("# Receipt - "), "should open with a heading");
        assertTrue(markdown.contains("| Product | Qty | Total |"), "should contain a table header");
        assertTrue(markdown.contains("| Catan | 2 | 69.98 |"), "line total should be price x quantity");
        assertTrue(markdown.contains("| Ticket to Ride | 1 | 4.99 |"));
        assertTrue(markdown.contains("**Total:** 74.97"));
        assertTrue(markdown.contains("paid via PayPal using victor@gmail.com"),
                "should include the payment method line");
    }

    @Test
    @DisplayName("markdown copes with an empty item list")
    void markdownHandlesNoItems() {
        Receipt receipt = new PayPalReceipt(0.0, ADDRESS, "victor@gmail.com");

        String markdown = receipt.toMarkdown(Map.of());

        assertTrue(markdown.contains("_No items recorded._"));
    }

    @Test
    @DisplayName("saves a .md file named for the timestamp, with no colons in the name")
    void savesTimestampedMarkdownFile(@TempDir Path directory) throws IOException {
        Receipt receipt = new PayPalReceipt(74.97, ADDRESS, "victor@gmail.com");

        Path saved = receipt.save(directory, basket());

        assertTrue(Files.exists(saved));
        assertTrue(saved.getFileName().toString().endsWith(".md"));
        // Colons are illegal in Windows paths, so the time must not use them.
        assertTrue(saved.getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.md"),
                "unexpected filename: " + saved.getFileName());
        assertTrue(Files.readString(saved).contains("| Catan | 2 | 69.98 |"));
    }

    @Test
    @DisplayName("two receipts in the same second do not overwrite each other")
    void doesNotOverwriteOnCollision(@TempDir Path directory) throws IOException {
        Receipt first = new PayPalReceipt(10.0, ADDRESS, "a@example.com");
        Receipt second = new PayPalReceipt(20.0, ADDRESS, "b@example.com");

        Path firstPath = first.save(directory, basket());
        Path secondPath = second.save(directory, basket());

        assertNotEquals(firstPath, secondPath, "the second receipt must get its own file");
        assertTrue(Files.exists(firstPath));
        assertTrue(Files.exists(secondPath));
        assertEquals(2, Files.list(directory).count());
    }

    @Test
    @DisplayName("creates the receipts directory when it does not exist yet")
    void createsDirectory(@TempDir Path parent) throws IOException {
        Path receipts = parent.resolve("receipts");
        Receipt receipt = new PayPalReceipt(5.0, ADDRESS, "victor@gmail.com");

        receipt.save(receipts, basket());

        assertTrue(Files.isDirectory(receipts));
    }

    @Test
    @DisplayName("savedPath is null until the receipt is saved, then points at the file")
    void savedPathTracksTheFile(@TempDir Path directory) throws IOException {
        Receipt receipt = new PayPalReceipt(5.0, ADDRESS, "victor@gmail.com");
        assertNull(receipt.getSavedPath());

        Path saved = receipt.save(directory, basket());

        assertEquals(saved, receipt.getSavedPath());
    }
}
