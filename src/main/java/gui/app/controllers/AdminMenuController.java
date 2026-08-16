package gui.app.controllers;

import Users.Admin;
import Users.User;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/admin-view-products.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/add-product.fxml"));
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
            // The outcome comes back as a value: a failed restore used to be reported as a
            // success here, because the bridge swallowed its own errors and never threw.
            switch (Admin.rollbackDatabase()) {
                case RESTORED -> showInfo("Rollback Complete", "The database has been restored to its last backup.");
                case NO_BACKUP -> showAlert("Rollback Not Possible", "There is no backup to roll back to.");
                case FAILED -> showAlert("Rollback Error", "The database could not be rolled back.");
            }
        }
    }

    @FXML
    private void handleExit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/welcome-tray.fxml"));
            Scene scene = new Scene(loader.load(), 700, 500);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();


        } catch (IOException e){
            showAlert("Logout error","Could not return to the welcome screen: " + e.getMessage());
        }
    }

    /** Confirmation of something that worked, so it does not arrive wearing an error icon. */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
