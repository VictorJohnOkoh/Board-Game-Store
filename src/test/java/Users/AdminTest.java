package Users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers how the outcome of adding a product is read. The addProduct methods themselves are
 * not tested here because they call the Python bridge - these drive the mapping directly,
 * which is the part that decides whether an interface reports success or failure.
 */
class AdminTest {

    @Test
    @DisplayName("the ADDED token is read as a successful add")
    void readsAdded() {
        assertEquals(Admin.AddResult.ADDED, Admin.interpret("ADDED"));
    }

    @Test
    @DisplayName("the DUPLICATE_ID token is read as a refused add, never as a success")
    void readsDuplicateId() {
        assertEquals(Admin.AddResult.DUPLICATE_ID, Admin.interpret("DUPLICATE_ID"));
    }

    @Test
    @DisplayName("a failed bridge call is read as a failure rather than a success")
    void readsNullAsFailure() {
        assertEquals(Admin.AddResult.FAILED, Admin.interpret(null));
    }

    @Test
    @DisplayName("an unrecognised answer is read as a failure rather than a success")
    void readsUnknownTokenAsFailure() {
        assertEquals(Admin.AddResult.FAILED, Admin.interpret("Product with that ID already exists"));
        assertEquals(Admin.AddResult.FAILED, Admin.interpret(""));
    }
}
