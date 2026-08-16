package CLIbasis.CLIbasis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the confirmation prompt guarding the destructive admin actions. The console
 * input is supplied from a string rather than the keyboard, so the answers a real admin
 * could type can all be driven through it.
 */
class ValidationUtilsTest {

    private static boolean answer(String typed) {
        return ValidationUtils.confirm(new Scanner(typed), "Proceed?");
    }

    @Test
    @DisplayName("an explicit yes goes ahead, in either spelling or casing")
    void acceptsYes() {
        assertTrue(answer("y\n"));
        assertTrue(answer("yes\n"));
        assertTrue(answer("Y\n"));
        assertTrue(answer("  YES  \n"));
    }

    @Test
    @DisplayName("an explicit no does not go ahead")
    void acceptsNo() {
        assertFalse(answer("n\n"));
        assertFalse(answer("no\n"));
        assertFalse(answer("N\n"));
    }

    @Test
    @DisplayName("pressing Enter without answering does not go ahead")
    void treatsEmptyAsNo() {
        assertFalse(answer("\n"));
        assertFalse(answer("   \n"));
    }

    @Test
    @DisplayName("an unrecognised answer re-prompts rather than being guessed at")
    void rePromptsOnUnrecognisedAnswer() {
        assertTrue(answer("maybe\ny\n"));
        assertFalse(answer("maybe\nn\n"));
    }
}
