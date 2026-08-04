package gui.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Absolute classpath paths: the FXML lives at the root of the resources folder,
        // not under this class's package, so a relative lookup never resolves.
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controllers/welcome-tray.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
        stage.setTitle("Board Game Store");

        // The icon is bundled as a classpath resource, so it must be read from the
        // classpath - a plain File lookup would depend on the working directory and
        // would never find it inside the jar.
        try (InputStream icon = getClass().getResourceAsStream("/images/chess.png")) {
            if (icon != null) {
                stage.getIcons().add(new Image(icon));
            }
        }

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        MainApp.launch(args);
    }
}
