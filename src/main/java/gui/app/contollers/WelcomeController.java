package gui.app.contollers;

import Users.User;
import Bridge.JavaPythonBridge;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WelcomeController {

    @FXML
    private FlowPane userGrid;

    private final List<User> userList = new ArrayList<>();

    // Cycled through for each tile's icon color, since real users don't carry a color of their own.
    private static final String[] TOKEN_COLORS = {
            "#C1502E", "#3E6A8F", "#D9A441", "#7A5A9E", "#4B7F52", "#2F6F63"
    };

    private User selectedUser;
    private VBox selectedTile;

    public void initialize() {
        loadUsers();
        populateGrid();
    }

    private void loadUsers() {
        try {
            userList.addAll(User.loadUsers());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users", e.getMessage());
        }
    }

    /** Clears the static sample tiles defined in the FXML and builds one tile per real user. */
    private void populateGrid() {
        if (userGrid == null) {
            System.err.println("Error: 'userGrid' is not injected. Check that fx:id='userGrid' matches the field name in WelcomeController.java");
            return;
        }
        
        userGrid.getChildren().clear();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String color = TOKEN_COLORS[i % TOKEN_COLORS.length];
            userGrid.getChildren().add(createUserTile(user, color));
        }
    }

    private VBox createUserTile(User user, String tokenColor) {
        Circle icon = new Circle(32);
        icon.getStyleClass().add("icon-token");
        icon.setStyle("-fx-fill: " + tokenColor + ";");

        Label initials = new Label(getInitials(user.getName()));
        initials.getStyleClass().add("initials-label");

        StackPane iconStack = new StackPane(icon, initials);

        Region leader = new Region();
        leader.getStyleClass().add("leader-line");

        Label name = new Label(user.getName());
        name.getStyleClass().add("name-label");

        boolean isAdmin = user.getRole().equalsIgnoreCase("admin");
        Label roleStamp = new Label(user.getRole().toUpperCase());
        roleStamp.getStyleClass().addAll("stamp-badge", isAdmin ? "role-admin" : "role-customer");

        VBox tile = new VBox(6, iconStack, leader, name, roleStamp);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("user-component");
        tile.setOnMouseClicked(e -> selectTile(tile, user));

        return tile;
    }

    /** Highlights the clicked tile and clears the highlight from whichever tile was selected before. */
    private void selectTile(VBox tile, User user) {
        if (selectedTile != null) {
            selectedTile.getStyleClass().remove("selected");
        }
        tile.getStyleClass().add("selected");
        selectedTile = tile;
        selectedUser = user;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    @FXML
    private void handleLogin() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user", "Choose a player tile above.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/controllers/" + getSceneFile(selectedUser.getRole()) + ".fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) userGrid.getScene().getWindow();
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load menu", e.getMessage());
        }
    }

    private String getSceneFile(String role) {
        return role.equalsIgnoreCase("admin") ? "admin-menu" : "customer-menu";
    }

    @FXML
    private void handleExit() {
        JavaPythonBridge.run(JavaPythonBridge.CLOSE_CONNECTION);
        JavaPythonBridge.close();
        Stage stage = (Stage) userGrid.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
