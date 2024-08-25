module com.example.client {
    requires javafx.controls;
    exports com.example to javafx.graphics;
    exports com.example.ui to javafx.graphics;
    exports com.example.network to javafx.graphics;
}