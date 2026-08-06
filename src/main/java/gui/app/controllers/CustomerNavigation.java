package gui.app.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Swaps the window's scene for another customer screen.
 * <p>
 * The customer screens all pass the same {@link Users.Customer} instance between
 * themselves, because the shopping basket lives on that object - reloading the user
 * from the database mid-session would silently hand back an empty basket.
 */
final class CustomerNavigation {

    private CustomerNavigation() {
    }

    /**
     * Replaces the scene in the window that {@code source} belongs to.
     *
     * @return the new screen's controller, so the caller can hand it the Customer
     */
    static <T> T swap(Node source, String fxmlName, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(CustomerNavigation.class.getResource("/controllers/" + fxmlName));
        Scene scene = new Scene(loader.load(), width, height);
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        return loader.getController();
    }

    /** Shows an error dialog - used when a screen cannot be loaded at all. */
    static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    /** Shows an informational dialog. */
    static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
