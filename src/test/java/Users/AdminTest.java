package Users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers how the outcomes of the admin's database actions are read. The addProduct and
 * rollbackDatabase methods themselves are not tested here because they call the Python
 * bridge - these drive the mappings directly, which is the part that decides whether an
 * interface reports success or failure.
 */
class AdminTest {

    @Test
    @DisplayName("the ADDED token is read as a successful add")
    void readsAdded() {
        assertEquals(Admin.AddResult.ADDED, Admin.interpretAdd("ADDED"));
    }

    @Test
    @DisplayName("a duplicate ID is read as a refused add, never as a success")
    void readsDuplicateId() {
        assertEquals(Admin.AddResult.DUPLICATE_ID, Admin.interpretAdd("DUPLICATE_ID"));
    }

    @Test
    @DisplayName("a duplicate name is read as a refused add, never as a success")
    void readsDuplicateName() {
        assertEquals(Admin.AddResult.DUPLICATE_NAME, Admin.interpretAdd("DUPLICATE_NAME"));
    }

    @Test
    @DisplayName("a clash on both the ID and the name is kept distinct from either alone")
    void readsDuplicateBoth() {
        assertEquals(Admin.AddResult.DUPLICATE_BOTH, Admin.interpretAdd("DUPLICATE_BOTH"));
    }

    @Test
    @DisplayName("a failed bridge call is read as a failed add rather than a success")
    void readsNullAsFailedAdd() {
        assertEquals(Admin.AddResult.FAILED, Admin.interpretAdd(null));
    }

    @Test
    @DisplayName("an unrecognised answer is read as a failed add rather than a success")
    void readsUnknownTokenAsFailedAdd() {
        assertEquals(Admin.AddResult.FAILED, Admin.interpretAdd("Product with that ID already exists"));
        assertEquals(Admin.AddResult.FAILED, Admin.interpretAdd(""));
    }

    @Test
    @DisplayName("the ROLLED_BACK token is read as a restored database")
    void readsRestored() {
        assertEquals(Admin.RollbackResult.RESTORED, Admin.interpretRollback("ROLLED_BACK"));
    }

    @Test
    @DisplayName("a missing backup is reported rather than passed off as a restore")
    void readsNoBackup() {
        assertEquals(Admin.RollbackResult.NO_BACKUP, Admin.interpretRollback("NO_BACKUP"));
    }

    @Test
    @DisplayName("a failed bridge call is read as a failed rollback rather than a restore")
    void readsNullAsFailedRollback() {
        assertEquals(Admin.RollbackResult.FAILED, Admin.interpretRollback(null));
        assertEquals(Admin.RollbackResult.FAILED, Admin.interpretRollback("anything else"));
    }
}
