package gui.app.controllers;

import Bridge.JavaPythonBridge;
import Users.Customer;
import Users.newBasket;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The customer's product browser: search, and add to basket straight from a row.
 * <p>
 * Reads the customer-facing raw bridge calls, which deliberately exclude the purchase
 * cost - a customer must never see what the shop paid for a product.
 */
public class CustomerProductsController {

    /** One row of the products table. Note there is no cost field, by design. */
    private record ProductRow(int id, String category, String type, String name,
                              double price, int stock, String detail) {}

    private static final int EXPECTED_FIELDS = 7;

    private Customer currentUser;

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label basketLabel;
    @FXML private TableView<ProductRow> productsTable;
    @FXML private TableColumn<ProductRow, String> idColumn;
    @FXML private TableColumn<ProductRow, String> categoryColumn;
    @FXML private TableColumn<ProductRow, String> typeColumn;
    @FXML private TableColumn<ProductRow, String> nameColumn;
    @FXML private TableColumn<ProductRow, String> detailColumn;
    @FXML private TableColumn<ProductRow, String> priceColumn;
    @FXML private TableColumn<ProductRow, String> stockColumn;
    @FXML private TableColumn<ProductRow, Void> addColumn;

    public void initialize(Customer customer) {
        this.currentUser = customer;

        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().id())));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        detailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().detail()));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().price())));
        stockColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().stock())));

        // Without this the ID, price and stock columns sort as text: 10 before 2.
        TableColumns.sortNumerically(idColumn, priceColumn, stockColumn);

        addAddButtonColumn();
        addDoubleClickToAdd();

        loadAllProducts();
        refreshBasketLabel();
    }

    /** Puts an Add button on every row. It shares one handler with the double-click
     * gesture below, so there is a single path into the basket. */
    private void addAddButtonColumn() {
        addColumn.setCellFactory(column -> new TableCell<>() {
            private final Button addButton = new Button("Add");

            {
                addButton.getStyleClass().add("primary-button");
                addButton.setOnAction(event -> addToBasket(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : addButton);
            }
        });
    }

    private void addDoubleClickToAdd() {
        productsTable.setRowFactory(table -> {
            TableRow<ProductRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    addToBasket(row.getItem());
                }
            });
            return row;
        });
    }

    /** The single place a product enters the basket from this screen. */
    private void addToBasket(ProductRow product) {
        if (product == null) {
            return;
        }

        newBasket.AddResult result = currentUser.basket.addShopping(product.id());
        switch (result) {
            case ADDED -> {
                statusLabel.setText("Added " + product.name() + " to basket");
                refreshBasketLabel();
            }
            case NOT_FOUND -> CustomerNavigation.showError("Could Not Add",
                    product.name() + " could not be found. It may have been removed from the store.");
            case INVALID_AMOUNT -> CustomerNavigation.showError("Could Not Add",
                    "The amount to add must be greater than zero.");
            case INSUFFICIENT_STOCK -> CustomerNavigation.showError("Not Enough Stock",
                    "There isn't enough of " + product.name() + " in stock.");
        }
    }

    private void refreshBasketLabel() {
        int lines = currentUser.basket.getItems().size();
        basketLabel.setText(lines == 0
                ? "Basket empty"
                : String.format("Basket: %d item%s, £%.2f", lines, lines == 1 ? "" : "s",
                        currentUser.basket.getTotalPrice()));
    }

    /** Searches by product ID when the term is a positive integer, and by compatibility
     * otherwise - the same rule the CLI applies. */
    @FXML
    private void handleSearch() {
        String term = searchField.getText();
        if (term == null || term.isBlank()) {
            loadAllProducts();
            return;
        }

        term = term.trim();
        String rawData;
        if (isPositiveInteger(term)) {
            rawData = JavaPythonBridge.run_result(JavaPythonBridge.FILTER_ID_RAW, Integer.parseInt(term));
        } else {
            rawData = JavaPythonBridge.run_result(JavaPythonBridge.FILTER_COMPATIBILITY_RAW, term);
        }

        ObservableList<ProductRow> rows = parseProducts(rawData);
        productsTable.setItems(rows);
        statusLabel.setText(rows.isEmpty() ? "No products matched \"" + term + "\"" : rows.size() + " match(es)");
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadAllProducts();
    }

    private void loadAllProducts() {
        String rawData = JavaPythonBridge.run_result(JavaPythonBridge.GET_PRODUCTS_RAW);
        productsTable.setItems(parseProducts(rawData));
        statusLabel.setText("");
    }

    private static boolean isPositiveInteger(String input) {
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Parses 'pid;category;type;name;price;quantity;extra' lines. */
    private ObservableList<ProductRow> parseProducts(String rawData) {
        List<ProductRow> rows = new ArrayList<>();
        if (rawData != null && !rawData.isBlank()) {
            for (String line : rawData.split("\n")) {
                if (line.isBlank()) continue;
                String[] fields = line.split(";");
                if (fields.length < EXPECTED_FIELDS) continue;

                try {
                    rows.add(new ProductRow(
                            Integer.parseInt(fields[0]),
                            fields[1].equalsIgnoreCase("boardgame") ? "Board Game" : "Accessory",
                            fields[2],
                            fields[3],
                            Double.parseDouble(fields[4]),
                            Integer.parseInt(fields[5]),
                            fields[6]));
                } catch (NumberFormatException e) {
                    // A malformed row is skipped rather than taking down the whole table.
                }
            }
        }
        return FXCollections.observableArrayList(rows);
    }

    @FXML
    private void handleBack() {
        try {
            CustomerMenuController controller =
                    CustomerNavigation.swap(productsTable, "customer-menu.fxml", 700, 500);
            controller.initialize(currentUser);
        } catch (IOException e) {
            CustomerNavigation.showError("Navigation Error", "Could not return to menu: " + e.getMessage());
        }
    }
}
