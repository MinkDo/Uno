package com.example.uno.Server;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Connection connection;
    private PublicKey publicKey;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Khởi tạo luồng đọc và ghi dữ liệu từ client
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

            // Gửi thông điệp chào mừng đến client
            out.println("Welcome to the Mail Server!");

            // Đọc dữ liệu từ client và gửi lại
            String clientRequest;
            while ((clientRequest = in.readLine()) != null) {
                System.out.println("Client: " + clientRequest);

                // Phân tích và xử lý yêu cầu từ client
                if (clientRequest.equalsIgnoreCase("receive")) {
                    EmailReceiver emailReceiver = new EmailReceiver();
                    emailReceiver.receiveEmail();
                } else if (clientRequest.equalsIgnoreCase("send")) {
                    EmailSender emailSender = new EmailSender();

                    emailSender.sendEmail("","","");
                } else {
                    // Đáp ứng yêu cầu không hợp lệ từ client
                    out.println("Server: Invalid request.");
                }

                // Gửi phản hồi lại cho client
                out.println("Server: I received your message: " + clientRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Đóng luồng đọc và ghi
                in.close();
                out.close();
                // Đóng kết nối với client
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String encryptContent(String content, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }




}
