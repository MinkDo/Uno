package com.example.uno.Server.AuthVaro;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Varo varo = new Varo();
        varo.auth();
    }
}
