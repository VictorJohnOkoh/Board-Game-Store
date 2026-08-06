package Users;

import Inventory.BoardGame;
import Inventory.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the basket rules. The ID-based addShopping is not tested here because it calls
 * the Python bridge for the product lookup - these drive the rules through the
 * product-based overload instead.
 */
class newBasketTest {

    private static Product product(int id, String name, double price, int stock) {
        return new BoardGame(id, "strategy", name, price, 10.0, stock, 4);
    }

    @Test
    @DisplayName("a new basket is empty and costs nothing")
    void startsEmpty() {
        newBasket basket = new newBasket();

        assertTrue(basket.isEmpty());
        assertEquals(0.0, basket.getTotalPrice());
        assertEquals("", basket.getProductAmount());
    }

    @Test
    @DisplayName("adding a product puts it in the basket")
    void addsProduct() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);

        assertEquals(newBasket.AddResult.ADDED, basket.addShopping(catan, 1));
        assertEquals(1, basket.getAmount(catan));
        assertFalse(basket.isEmpty());
    }

    @Test
    @DisplayName("adding the same product again accumulates the quantity")
    void accumulatesQuantity() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);

        basket.addShopping(catan, 2);
        basket.addShopping(catan, 3);

        assertEquals(5, basket.getAmount(catan));
        assertEquals(1, basket.getItems().size(), "should stay one line, not two");
    }

    @Test
    @DisplayName("adding zero or fewer is rejected and changes nothing")
    void rejectsNonPositiveAmounts() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);

        assertEquals(newBasket.AddResult.INVALID_AMOUNT, basket.addShopping(catan, 0));
        assertEquals(newBasket.AddResult.INVALID_AMOUNT, basket.addShopping(catan, -1));
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("cannot add more than is in stock")
    void rejectsMoreThanStock() {
        newBasket basket = new newBasket();
        Product scarce = product(1, "Scarce Game", 20.0, 3);

        assertEquals(newBasket.AddResult.INSUFFICIENT_STOCK, basket.addShopping(scarce, 4));
        assertTrue(basket.isEmpty(), "a rejected add must leave the basket untouched");
    }

    @Test
    @DisplayName("the stock cap counts what is already in the basket")
    void stockCapCountsExistingBasketContents() {
        newBasket basket = new newBasket();
        Product scarce = product(1, "Scarce Game", 20.0, 3);

        assertEquals(newBasket.AddResult.ADDED, basket.addShopping(scarce, 2));
        assertEquals(newBasket.AddResult.INSUFFICIENT_STOCK, basket.addShopping(scarce, 2));
        assertEquals(2, basket.getAmount(scarce), "the failed add must not change the quantity");
    }

    @Test
    @DisplayName("adding exactly the stock level is allowed")
    void allowsExactlyStock() {
        newBasket basket = new newBasket();
        Product scarce = product(1, "Scarce Game", 20.0, 3);

        assertEquals(newBasket.AddResult.ADDED, basket.addShopping(scarce, 3));
        assertEquals(3, basket.getAmount(scarce));
    }

    @Test
    @DisplayName("the total is the sum of price times quantity across every line")
    void totalsEveryLine() {
        newBasket basket = new newBasket();
        basket.addShopping(product(1, "Catan", 34.99, 10), 2);
        basket.addShopping(product(2, "Dice", 4.99, 10), 1);

        assertEquals(74.97, basket.getTotalPrice(), 0.001);
    }

    @Test
    @DisplayName("removing part of a line leaves the rest")
    void removesPartOfALine() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);
        basket.addShopping(catan, 3);

        basket.removeProduct(catan, 1);

        assertEquals(2, basket.getAmount(catan));
    }

    @Test
    @DisplayName("removing the whole quantity drops the line entirely")
    void removesWholeLine() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);
        basket.addShopping(catan, 2);

        basket.removeProduct(catan, 2);

        assertTrue(basket.isEmpty());
        assertEquals(0, basket.getAmount(catan));
    }

    @Test
    @DisplayName("emptying clears every line")
    void emptiesBasket() {
        newBasket basket = new newBasket();
        basket.addShopping(product(1, "Catan", 34.99, 10), 1);
        basket.addShopping(product(2, "Dice", 4.99, 10), 1);

        basket.emptyBasket();

        assertTrue(basket.isEmpty());
        assertEquals(0.0, basket.getTotalPrice());
    }

    @Test
    @DisplayName("the stock update string has no trailing separator")
    void buildsProductAmountString() {
        newBasket basket = new newBasket();
        basket.addShopping(product(1, "Catan", 34.99, 10), 2);
        basket.addShopping(product(2, "Dice", 4.99, 10), 1);

        assertEquals("1:2;2:1", basket.getProductAmount());
    }

    @Test
    @DisplayName("the exposed items map cannot be modified from outside")
    void itemsViewIsUnmodifiable() {
        newBasket basket = new newBasket();
        Product catan = product(1, "Catan", 34.99, 10);
        basket.addShopping(catan, 1);

        assertThrows(UnsupportedOperationException.class, () -> basket.getItems().clear());
    }
}
