package gui.app.controllers;

import Inventory.Product;
import Users.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows the basket and collects the choice of payment method, then hands off to the
 * payment screen. Nothing is charged here.
 */
public class CustomerPayController {

    /** One basket line. Keeps the Product so a Remove button can act on it. */
    private record BasketLine(Product product, String name, int amount, double lineTotal) {}

    private Customer currentUser;

    @FXML private TableView<BasketLine> basketTable;
    @FXML private TableColumn<BasketLine, String> nameColumn;
    @FXML private TableColumn<BasketLine, String> amountColumn;
    @FXML private TableColumn<BasketLine, String> lineTotalColumn;
    @FXML private TableColumn<BasketLine, Void> removeColumn;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    @FXML private RadioButton payPalRadio;
    @FXML private RadioButton creditCardRadio;
    @FXML private Button paymentButton;
    @FXML private Button clearBasketButton;

    public void initialize(Customer customer) {
        this.currentUser = customer;

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().amount())));
        lineTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().lineTotal())));

        // Without this the amount and total columns sort as text: 10 before 2.
        TableColumns.sortNumerically(amountColumn, lineTotalColumn);

        addRemoveButtonColumn();
        refresh();
    }

    private void addRemoveButtonColumn() {
        removeColumn.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.getStyleClass().add("secondary-button");
                removeButton.setOnAction(event -> {
                    BasketLine line = getTableRow() == null ? null : getTableRow().getItem();
                    if (line != null) {
                        removeLine(line);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : removeButton);
            }
        });
    }

    /** Removes the whole line - passing the full basket amount clears the product out. */
    private void removeLine(BasketLine line) {
        currentUser.basket.removeProduct(line.product(), line.amount());
        statusLabel.setText("Removed " + line.name());
        refresh();
    }

    /** Rebuilds the table, total, and the enabled state of the action buttons. */
    private void refresh() {
        List<BasketLine> lines = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : currentUser.basket.getItems().entrySet()) {
            Product product = entry.getKey();
            int amount = entry.getValue();
            lines.add(new BasketLine(product, product.getProductName(), amount, product.getPrice() * amount));
        }

        basketTable.setItems(FXCollections.observableArrayList(lines));
        totalLabel.setText(String.format("%.2f", currentUser.basket.getTotalPrice()));

        boolean empty = currentUser.basket.isEmpty();
        paymentButton.setDisable(empty);
        clearBasketButton.setDisable(empty);
        if (empty) {
            statusLabel.setText("Your basket is empty. Add products before paying.");
        }
    }

    @FXML
    private void handleClearBasket() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Clear Basket");
        confirm.setHeaderText("Are you sure you want to clear your basket?");
        confirm.setContentText("Everything currently in your basket will be removed.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            currentUser.basket.emptyBasket();
            statusLabel.setText("Basket cleared.");
            refresh();
        }
    }

    /** Hands the chosen method to the payment screen, which collects the details. */
    @FXML
    private void handlePayment() {
        if (currentUser.basket.isEmpty()) {
            statusLabel.setText("Your basket is empty. Add products before paying.");
            return;
        }

        CustomerPaymentController.Method method = creditCardRadio.isSelected()
                ? CustomerPaymentController.Method.CREDIT_CARD
                : CustomerPaymentController.Method.PAYPAL;

        try {
            CustomerPaymentController controller =
                    CustomerNavigation.swap(basketTable, "customer-payment.fxml", 700, 500);
            controller.initialize(currentUser, method);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not load the payment screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            CustomerMenuController controller =
                    CustomerNavigation.swap(basketTable, "customer-menu.fxml", 700, 500);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not return to menu: " + e.getMessage());
        }
    }
}
