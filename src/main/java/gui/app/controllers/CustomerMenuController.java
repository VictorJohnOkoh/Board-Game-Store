package gui.app.controllers;

import Users.Customer;
import Users.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomerMenuController {

    private Customer currentUser;

    @FXML
    private Label welcomeLabel;

    /** {@link User#buildUser} already returns a Customer for the customer role, so this
     * cast is safe - and it is what gives every customer screen access to the basket. */
    public void initialize(User user) {
        this.currentUser = (Customer) user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole().toUpperCase() + ")");
        }
    }

    @FXML
    private void handleViewProducts() {
        try {
            CustomerProductsController controller =
                    CustomerNavigation.swap(welcomeLabel, "customer-products.fxml", 800, 600);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not load the products view: " + e.getMessage());
        }
    }

    @FXML
    private void handlePay() {
        try {
            CustomerPayController controller =
                    CustomerNavigation.swap(welcomeLabel, "customer-pay.fxml", 800, 600);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not load the pay screen: " + e.getMessage());
        }
    }

    /** Clears the basket from the menu. Confirms first, because from here the customer
     * cannot see what they are about to discard. */
    @FXML
    private void handleClearBasket() {
        if (currentUser.basket.isEmpty()) {
            CustomerNavigation.showInfo("Basket", "Your basket is already empty.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Clear Basket");
        confirm.setHeaderText("Are you sure you want to clear your basket?");
        confirm.setContentText("Everything currently in your basket will be removed.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            currentUser.basket.emptyBasket();
            CustomerNavigation.showInfo("Basket", "Your basket has been cleared.");
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
            CustomerNavigation.showError("Logout Error", "Could not return to welcome screen" + e.getMessage());
        }

    }

}
