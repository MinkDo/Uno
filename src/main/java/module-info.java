module com.example.uno {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires activation;
    requires java.sql;
    requires com.google.gson;


    opens com.example.uno to javafx.fxml;
    exports com.example.uno.Server;
}