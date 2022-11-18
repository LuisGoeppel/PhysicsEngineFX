module com.example.physicsenginefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.jupiter.api;

    opens Controller to javafx.fxml;
    exports Controller;
}
