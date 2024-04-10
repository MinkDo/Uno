package com.example.uno.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class MailServer {
    private Connection connection;
    private ServerSocket serverSocket;
    private boolean isRunning;
    public MailServer(int port){
        try{
            this.serverSocket = new ServerSocket(port);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            String url = "jdbc:mysql://localhost:3306/unomail";
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void start(){
        isRunning = true;
        System.out.println("Mail server is starting...");

        while (isRunning){
            try {
                Socket clientSocket = serverSocket.accept();

                Thread clientHandler = new Thread(new ClientHandler(clientSocket));
            }catch (Exception e){

            }
        }
    }
    public boolean authenticateUser(String username, String password){
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }
    public void stop() {
        isRunning = false;
        System.out.println("Mail server is stopping...");

        try {
            // Đóng server socket để dừng lắng nghe các kết nối đến
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        MailServer mailServer = new MailServer(25);
        mailServer.authenticateUser("hahah","jajaja");
    }

}
