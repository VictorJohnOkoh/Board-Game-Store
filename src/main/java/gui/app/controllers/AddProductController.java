package gui.app.controllers;

import Inventory.Accessory;
import Inventory.AccessoryType;
import Inventory.BoardGame;
import Bridge.JavaPythonBridge;
import Users.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AddProductController {

    private int currentUserId;

    @FXML private TextField bgIdField;
    @FXML private TextField bgTypeField;
    @FXML private TextField bgNameField;
    @FXML private TextField bgPriceField;
    @FXML private TextField bgStockField;
    @FXML private TextField bgCostField;
    @FXML private TextField bgPlayersField;

    @FXML private TextField accIdField;
    @FXML private ChoiceBox<String> accTypeField;
    @FXML private TextField accNameField;
    @FXML private TextField accPriceField;
    @FXML private TextField accStockField;
    @FXML private TextField accCostField;
    @FXML private TextField accCompatField;

    @FXML private Label statusLabel;

    public void initialize(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    private void handleAddBoardGame() {
        try {
            int id = parseFourDigitId(bgIdField.getText());
            String type = requireLettersOnly(bgTypeField.getText());
            String name = requireNonEmpty(bgNameField.getText(), "Name");
            double price = Double.parseDouble(bgPriceField.getText().trim());
            double cost = Double.parseDouble(bgCostField.getText().trim());
            int players = Integer.parseInt(bgPlayersField.getText().trim());
            int stock = Integer.parseInt(bgStockField.getText().trim());

            BoardGame product = new BoardGame(id, type, name, price, cost, stock, players);
            JavaPythonBridge.run(JavaPythonBridge.ADD_BOARD_GAME, product);
            statusLabel.setText("Board game added successfully.");
            clearBoardGameFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please check the numeric fields are filled in correctly.");
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void handleAddAccessory() {
        try {
            int id = parseFourDigitId(accIdField.getText());
            String typeChoice = accTypeField.getValue();
            if (typeChoice == null) {
                throw new IllegalArgumentException("Please select an accessory type.");
            }
            AccessoryType type = AccessoryType.getValueOf(typeChoice);
            String name = requireNonEmpty(accNameField.getText(), "Name");
            double price = Double.parseDouble(accPriceField.getText().trim());
            double cost = Double.parseDouble(accCostField.getText().trim());
            String compatibility = requireNonEmpty(accCompatField.getText(), "Compatibility");
            int stock = Integer.parseInt(accStockField.getText().trim());

            Accessory product = new Accessory(id, type, name, price, cost, stock, compatibility);
            JavaPythonBridge.run(JavaPythonBridge.ADD_ACCESSORY, product);
            statusLabel.setText("Accessory added successfully.");
            clearAccessoryFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please check the numeric fields are filled in correctly.");
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/admin-menu.fxml"));
            Scene scene = new Scene(loader.load(), 700, 500);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            AdminMenuController controller = loader.getController();
            controller.initialize(findCurrentUser());
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not return to menu: " + e.getMessage());
        }
    }

    private User findCurrentUser() {
        return User.loadUsers().stream()
                .filter(u -> u.getUserID() == currentUserId)
                .findFirst()
                .orElse(null);
    }

    private int parseFourDigitId(String text) {
        int id = Integer.parseInt(text.trim());
        if (String.valueOf(id).length() != 4) {
            throw new IllegalArgumentException("Product ID must be exactly 4 digits.");
        }
        return id;
    }

    private String requireLettersOnly(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (!trimmed.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("Type must contain only English letters.");
        }
        return trimmed.toLowerCase();
    }

    private String requireNonEmpty(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return text.trim();
    }

    private void clearBoardGameFields() {
        bgIdField.clear();
        bgTypeField.clear();
        bgNameField.clear();
        bgPriceField.clear();
        bgStockField.clear();
        bgCostField.clear();
        bgPlayersField.clear();
    }

    private void clearAccessoryFields() {
        accIdField.clear();
        accTypeField.setValue(null);
        accNameField.clear();
        accPriceField.clear();
        accStockField.clear();
        accCostField.clear();
        accCompatField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
