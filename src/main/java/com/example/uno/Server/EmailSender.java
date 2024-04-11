package com.example.uno.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/uno";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "your-db-password";
    private static final String EMAIL_USERNAME = "your-email@example.com";
    private static final String EMAIL_PASSWORD = "your-email-password";

    public static void sendEmail(String recipient, String subject, String body) {
        // Thiết lập cấu hình cho kết nối SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Địa chỉ SMTP server của bạn
        props.put("mail.smtp.port", "587"); // Cổng SMTP server của bạn

        // Tạo phiên làm việc với máy chủ SMTP
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            // Kết nối đến cơ sở dữ liệu
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                // Lấy dữ liệu từ cơ sở dữ liệu
                String query = "SELECT * FROM recipients WHERE email = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, recipient);
                    ResultSet resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        System.out.println("Recipient not found in the database.");
                        return;
                    }
                }

                // Tạo đối tượng MimeMessage để xây dựng email
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_USERNAME)); // Địa chỉ email người gửi
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient)); // Địa chỉ email người nhận
                message.setSubject(subject); // Chủ đề của email
                message.setText(body); // Nội dung của email

                // Gửi email
                Transport.send(message);

                System.out.println("Email sent successfully.");
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database connection error.");
        }
    }

    public static void main(String[] args) {
        // Gọi phương thức sendEmail để gửi email
        sendEmail("recipient@example.com", "Test Email", "This is a test email.");
    }
}
