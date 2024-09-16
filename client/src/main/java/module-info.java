module com.example.client {
    requires javafx.controls;
    requires java.net.http;
    requires spring.web;
    requires spring.core;
    requires com.fasterxml.jackson.databind;
    requires spring.jcl;
    requires java.logging;
    exports com.example to javafx.graphics;
    exports com.example.state to javafx.graphics;
    exports com.example.ui to javafx.graphics;
    exports com.example.network to javafx.graphics;
    exports com.example.simulation to javafx.graphics;
    exports com.example.ui.menu to javafx.graphics, com.fasterxml.jackson.databind;
    exports com.example.ui.game to javafx.graphics;
    exports com.example.ui.lobby to com.fasterxml.jackson.databind, javafx.graphics;
}