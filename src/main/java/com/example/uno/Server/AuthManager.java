package com.example.uno.Server;

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
        // Bạn có thể thử nghiệm phương thức login ở đây
        // Ví dụ:
        // String userName = login("your_request_data");
        // if (userName != null) {
        //     System.out.println("Authenticated user: " + userName);
        // } else {
        //     System.out.println("Authentication failed.");
        // }
    }
}
