module IntelliJ.GUI {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.ninia.jep;

    exports gui.app to javafx.graphics;
    exports gui.app.contollers to javafx.fxml;
    exports Users;
    exports Inventory;


    opens gui.app to javafx.graphics;
    opens Users to javafx.base;
    opens gui.app.contollers to javafx.fxml;
}