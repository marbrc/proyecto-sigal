module mx.utng {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.mail;
    requires org.apache.pdfbox;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens mx.utng.controller to javafx.fxml;
    opens mx.utng.model to javafx.base;

    exports mx.utng;
    exports mx.utng.controller;
    exports mx.utng.model;
}