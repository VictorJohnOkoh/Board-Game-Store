package gui.app.contollers;

import Bridge.JavaPythonBridge;
import Users.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewProductsController {

    private int currentUserId;

    @FXML private TableView<ProductRow> productsTable;
    @FXML private TableColumn<ProductRow, String> idColumn;
    @FXML private TableColumn<ProductRow, String> categoryColumn;
    @FXML private TableColumn<ProductRow, String> typeColumn;
    @FXML private TableColumn<ProductRow, String> nameColumn;
    @FXML private TableColumn<ProductRow, String> priceColumn;
    @FXML private TableColumn<ProductRow, String> stockColumn;
    @FXML private TableColumn<ProductRow, String> costColumn;

    public void initialize(int userId) {
        this.currentUserId = userId;

        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().id())));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().price())));
        stockColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().stock())));
        // costColumn only exists in the admin view's FXML - the customer view doesn't inject it
        if (costColumn != null) {
            costColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().cost())));
        }

        loadProducts();
    }

    private void loadProducts() {
        String rawData = JavaPythonBridge.run(JavaPythonBridge.GET_ADMIN_PRODUCTS_RAW, currentUserId);
        productsTable.setItems(parseProducts(rawData));
    }

    private ObservableList<ProductRow> parseProducts(String rawData) {
        List<ProductRow> rows = new ArrayList<>();
        if (rawData != null && !rawData.isBlank()) {
            for (String line : rawData.split("\n")) {
                if (line.isBlank()) continue;
                String[] fields = line.split(";");
                if (fields.length < 8) continue;

                int id = Integer.parseInt(fields[0]);
                String category = fields[1].equalsIgnoreCase("boardgame") ? "Board Game" : "Accessory";
                String type = fields[2];
                String name = fields[3];
                double price = Double.parseDouble(fields[4]);
                int stock = Integer.parseInt(fields[5]);
                double cost = Double.parseDouble(fields[6]);

                rows.add(new ProductRow(id, category, type, name, price, stock, cost));
            }
        }
        return FXCollections.observableArrayList(rows);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/admin-menu.fxml"));
            Scene scene = new Scene(loader.load(), 700, 500);
            Stage stage = (Stage) productsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            AdminMenuController controller = loader.getController();
            controller.initialize(findCurrentUser());
        } catch (IOException e) {
            showAlert("Could not return to menu: " + e.getMessage());
        }
    }

    private User findCurrentUser() {
        return User.loadUsers().stream()
                .filter(u -> u.getUserID() == currentUserId)
                .findFirst()
                .orElse(null);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record ProductRow(int id, String category, String type, String name, double price, int stock, double cost) {}
}
