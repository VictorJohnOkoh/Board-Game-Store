package gui.app.controllers;

import Users.User;
import Bridge.JavaPythonBridge;
import javafx.fxml.FXML;
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
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
        if (currentUser != null) {
            JavaPythonBridge.run(JavaPythonBridge.CLOSE_CONNECTION);
            JavaPythonBridge.close();
        }
    }
}
