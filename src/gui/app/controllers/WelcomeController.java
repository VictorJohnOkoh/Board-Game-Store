package gui.app.controllers;

import Users.User;
import Inventory.JavaPythonBridge;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    public void initialize() {
        setupColumns();
        loadUsers();
        userTable.setItems(userList);
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadUsers() {
        try {
            java.util.List<User> users = User.loadUsers();
            userList.addAll(users);
        } catch (Exception e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load users");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogin() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("Please select a user");
            alert.setContentText("Choose a user from the list above.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/views/" + getSceneFile(selectedUser.getRole()) + ".fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            if (selectedUser.getRole().equalsIgnoreCase("admin")) {
                AdminMenuController controller = loader.getController();
                controller.initialize(selectedUser);
            } else {
                CustomerMenuController controller = loader.getController();
                controller.initialize(selectedUser);
            }
        } catch (IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load menu");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private String getSceneFile(String role) {
        if (role.equalsIgnoreCase("admin")) {
            return "admin-menu";
        } else {
            return "customer-menu";
        }
    }

    @FXML
    private void handleExit() {
        JavaPythonBridge.run(JavaPythonBridge.CLOSE_CONNECTION);
        JavaPythonBridge.close();
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
    }
}
