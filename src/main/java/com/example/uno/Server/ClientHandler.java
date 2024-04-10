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
                    receiveEmail();
                } else if (clientRequest.equalsIgnoreCase("send")) {
                    sendEmail();
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
    private void receiveEmail() {
        try {
            // Thiết lập cấu hình cho kết nối IMAP
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.host", "imap.example.com");
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");

            // Tạo phiên làm việc với máy chủ IMAP
            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imap");
            store.connect("imap.example.com", "your-username", "your-password");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/emaildb", "username", "password");

            // Mở thư mục INBOX và lấy ra tất cả email
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            // Xử lý từng email nhận được (ở đây là in ra tiêu đề của email)
            for (Message message : messages) {
                // Lấy thông tin của email
                String subject = message.getSubject();
                String sender = InternetAddress.toString(message.getFrom());

                // Lưu thông tin email vào cơ sở dữ liệu
                saveEmailToDatabase(connection, sender, subject);
            }

            // Đóng kết nối IMAP
            inbox.close(false);
            store.close();

            // Gửi phản hồi cho client để xác nhận việc nhận email thành công
            out.println("Server: Email(s) received successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Server: Failed to receive email.");
        }
    }

    private String encryptContent(String content, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private void saveEmailToDatabase(Connection connection, String sender, String subject) {
        try {
            // Tạo câu lệnh SQL để chèn dữ liệu vào cơ sở dữ liệu
            String sql = "INSERT INTO emails (sender, subject) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sender);
            statement.setString(2, subject);

            // Thực thi câu lệnh SQL
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail() {
        // Triển khai logic để gửi email đến client hoặc đích đến khác
        // Lấy dữ liệu email từ client (tiêu đề, nội dung, người nhận, ...)
        // Gửi email đi và xác nhận việc gửi email thành công cho client
        out.println("Server: Email sent successfully.");
    }
}
