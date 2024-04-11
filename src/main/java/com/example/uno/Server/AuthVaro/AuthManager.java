package com.example.uno.Server.AuthVaro;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AuthManager {
    private static final String AUTH_URL = "https://auth.varo.domains/verify/";

    public static String flaskLogin(Map<String, String[]> formData) {
        String requestData = formData.get("data")[0]; // Lấy dữ liệu từ form data
        Gson gson = new Gson();
        Map<String, String> requestDataMap = gson.fromJson(requestData, HashMap.class);
        String authRequest = requestDataMap.get("request"); // Trích xuất request từ dữ liệu

        return login(authRequest);
    }

    public static String login(String request) {
        try {
            URL url = new URL(AUTH_URL + request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                String responseData = response.toString();
                Gson gson = new Gson();
                Map<String, Object> responseMap = gson.fromJson(responseData, HashMap.class);
                boolean success = (boolean) responseMap.get("success");
                if (success) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                    if (data != null && data.containsKey("name")) {
                        return (String) data.get("name");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
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
    }


}
