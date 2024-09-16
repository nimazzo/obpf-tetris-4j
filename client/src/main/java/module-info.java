module com.example.client {
    requires javafx.controls;
    requires java.net.http;
    requires spring.web;
    requires spring.core;
    requires com.fasterxml.jackson.databind;
    requires spring.jcl;
    requires java.logging;

    exports com.example to javafx.graphics;
    exports com.example.ui.views.game to com.fasterxml.jackson.databind;
    opens com.example.ui.controllers to com.fasterxml.jackson.databind;
}