module com.example.uno {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires activation;
    requires java.sql;
    requires com.google.gson;
    requires jdk.httpserver;


    opens com.example.uno to javafx.fxml;
    exports com.example.uno.Server;
    exports com.example.uno.Server.AuthVaro;
}