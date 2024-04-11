package com.example.uno.Server.AuthVaro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class MainAuth {
    private static final int PORT = 8000;
    private static final String AUTH_URL = "https://auth.varo.domains/v1";
    private static List<Map<String, String>> cookie = new ArrayList<>();

    public static void main(String[] args) {
        // Tạo dữ liệu form giả lập
        Map<String, String[]> formData = new HashMap<>();
        formData.put("data", new String[] { "{\"request\": \"eyJ1c2VybmFtZSI6ICJhZG1pbiIsICJwYXNzd29yZCI6ICJhZG1pbiJ9\"}" });

        // Xử lý dữ liệu đăng nhập
        AuthManager authManager = new AuthManager();
        String username = authManager.flaskLogin(formData);

        // Xử lý kết quả
        if (username != null) {
            System.out.println("Đăng nhập thành công với tên: " + username);
        } else {
            System.out.println("Đăng nhập thất bại!");
        }

        // Khởi động máy chủ và lắng nghe các yêu cầu
        startServer();
    }

    public static void startServer() {
        try {
            int port = 8000;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new IndexHandler());
            server.createContext("/auth", new AuthHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server is listening on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (checkCookie(exchange.getRequestHeaders().getFirst("Cookie"))) {
                String response = String.format("<h1>Index Page</h1><p>Welcome %s</p>", getUsernameFromCookie(exchange.getRequestHeaders().getFirst("Cookie")));
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            } else {
                String response = "<h1>Index Page</h1><script src=\"https://code.jquery.com/jquery-3.6.4.min.js\"></script><script type=\"text/javascript\" src=\"https://auth.varo.domains/v1\"></script><script>var varo = new Varo();</script><button onclick='varo.auth().then(auth => { if (auth.success) { $.post(\"/auth\", JSON.stringify(auth.data), (response) => { window.location.reload(); }); } });'>Login</button>";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.close();
        }
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> requestBody = new Gson().fromJson(new String(exchange.getRequestBody().readAllBytes()), HashMap.class);
                String auth = flaskLogin(requestBody);
                if (auth != null) {
                    String authCookie = generateCookie();
                    cookie.add(Map.of("name", auth, "cookie", authCookie));
                    exchange.getResponseHeaders().set("Set-Cookie", "test_auth=" + authCookie);
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    return;
                }
            }
            String response = "Error";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    private static String flaskLogin(Map<String, String> requestDataMap) {
        // Custom implementation for parsing request data and performing authentication
        // You need to replace this with your actual authentication logic
        // For simplicity, let's assume it returns the username as authentication success
        return requestDataMap.get("username");
    }

    private static boolean checkCookie(String cookieString) {
        if (cookieString != null) {
            String[] cookies = cookieString.split(";");
            for (String cookieEntry : cookies) {
                String[] keyValue = cookieEntry.trim().split("=");
                if (keyValue.length == 2 && keyValue[0].equals("test_auth")) {
                    String cookieValue = keyValue[1];
                    for (Map<String, String> c : cookie) {
                        if (c.get("cookie").equals(cookieValue)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static String getUsernameFromCookie(String cookieString) {
        if (cookieString != null) {
            String[] cookies = cookieString.split(";");
            for (String cookieEntry : cookies) {
                String[] keyValue = cookieEntry.trim().split("=");
                if (keyValue.length == 2 && keyValue[0].equals("test_auth")) {
                    String cookieValue = keyValue[1];
                    for (Map<String, String> c : cookie) {
                        if (c.get("cookie").equals(cookieValue)) {
                            return c.get("name");
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String generateCookie() {
        // Custom implementation for generating cookie
        // You can replace this with any desired logic
        return java.util.UUID.randomUUID().toString();
    }
}
