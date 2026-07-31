package gui.app.controllers;

import Users.User;
import Inventory.JavaPythonBridge;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminMenuController {

    private User currentUser;

    @FXML
    private javafx.scene.control.Label welcomeLabel;

    public void initialize(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole().toUpperCase() + ")");
        }
    }

    @FXML
    private void handleViewProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view-products.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            ViewProductsController controller = loader.getController();
            controller.initialize(currentUser.getUserID());
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load products view: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-product.fxml"));
            Scene scene = new Scene(loader.load(), 750, 600);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            AddProductController controller = loader.getController();
            controller.initialize(currentUser.getUserID());
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load add product view: " + e.getMessage());
        }
    }

    @FXML
    private void handleRollback() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Rollback");
        confirm.setHeaderText("Are you sure you want to rollback the database?");
        confirm.setContentText("This will restore the database to its last backup. All changes since the last backup will be lost.");

        if (confirm.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            try {
                JavaPythonBridge.rollback();
                showAlert("Success", "Database has been rolled back successfully.");
            } catch (Exception e) {
                showAlert("Rollback Error", "Failed to rollback database: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
        if (currentUser != null) {
            JavaPythonBridge.run(JavaPythonBridge.CLOSE_CONNECTION);
            JavaPythonBridge.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
