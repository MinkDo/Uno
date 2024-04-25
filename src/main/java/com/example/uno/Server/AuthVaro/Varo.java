package com.example.uno.Server.AuthVaro;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.concurrent.Worker;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Varo {
    private String _request;
    private Stage _stage;
    private WebView _webView;

    public Varo() {
        // Constructor
    }

    public void auth() {
        start();
    }

    private void start() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://auth.varo.domains/request"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleResponse)
                .join();
    }

    private void handleResponse(String jsonResponse) {
        JsonObject responseObject = new Gson().fromJson(jsonResponse, JsonObject.class);

        if (responseObject.get("success").getAsBoolean()) {
            JsonObject requestData = responseObject.getAsJsonObject("data");
            _request = requestData.get("request").getAsString();
            System.out.println(_request);
            Platform.runLater(() -> {
                _stage = new Stage();
                _webView = new WebView();
                WebEngine webEngine = _webView.getEngine();

                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        // Send message to JavaScript when page is loaded
                        String script = "window.postMessage(JSON.stringify({action: 'request', id: '" + _request + "'}), '*');";
                        webEngine.executeScript(script);
                    }
                });

                _stage.setScene(new Scene(_webView, 500, 500));
                _stage.show();
                _stage.setOnCloseRequest(event -> check());
                webEngine.load("https://auth.varo.domains");
            });
        }
    }

    private void check() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://auth.varo.domains/verify/" + _request))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleCheckResponse)
                .join();
    }

    private void handleCheckResponse(String jsonResponse) {
        // Handle response object
    }
}
