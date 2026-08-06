package gui.app.controllers;

import Users.User;
import Bridge.JavaPythonBridge;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomerMenuController {

    private User currentUser;

    @FXML
    private Label welcomeLabel;

    public void initialize(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole().toUpperCase() + ")");
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

        } catch (Exception e) {
            showAlert("Logout Error", "Could not return to welcome screen" + e.getMessage());
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
