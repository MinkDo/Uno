package com.example.uno.Server;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailSender {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/yourdatabase";
    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        List<EmailSenderInfo> senderInfos = getEmailSendersFromDatabase();
        for (EmailSenderInfo senderInfo : senderInfos) {
            sendEmailWithAttachments(senderInfo);
        }
    }

    public static void sendEmailWithAttachments(EmailSenderInfo senderInfo) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderInfo.getEmail(), senderInfo.getPassword());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderInfo.getEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(senderInfo.getRecipient()));
            message.setSubject(senderInfo.getSubject());

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(senderInfo.getBody());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (AttachmentInfo attachment : senderInfo.getAttachments()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                attachmentBodyPart.attachFile(new File(attachment.getFilePath()));
                multipart.addBodyPart(attachmentBodyPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Email sent successfully to " + senderInfo.getRecipient());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send email to " + senderInfo.getRecipient());
        }
    }

    public static List<EmailSenderInfo> getEmailSendersFromDatabase() {
        List<EmailSenderInfo> senderInfos = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT sender_email, password, recipient, subject, body FROM sender_table";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String senderEmail = resultSet.getString("sender_email");
                    String password = resultSet.getString("password");
                    String recipient = resultSet.getString("recipient");
                    String subject = resultSet.getString("subject");
                    String body = resultSet.getString("body");
                    List<AttachmentInfo> attachments = getAttachmentsFromDatabase(connection, recipient); // Lấy thông tin đính kèm từ cơ sở dữ liệu
                    senderInfos.add(new EmailSenderInfo(senderEmail, password, recipient, subject, body, attachments));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return senderInfos;
    }

    public static List<AttachmentInfo> getAttachmentsFromDatabase(Connection connection, String recipient) {
        List<AttachmentInfo> attachments = new ArrayList<>();
        try {
            String sql = "SELECT file_name, file_path FROM attachment_table WHERE recipient = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, recipient);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String fileName = resultSet.getString("file_name");
                        String filePath = resultSet.getString("file_path");
                        attachments.add(new AttachmentInfo(fileName, filePath));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attachments;
    }
}

class AttachmentInfo {
    private String fileName;
    private String filePath;

    public AttachmentInfo(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}

class EmailSenderInfo {
    private String email;
    private String password;
    private String recipient;
    private String subject;
    private String body;
    private List<AttachmentInfo> attachments;

    public EmailSenderInfo(String email, String password, String recipient, String subject, String body, List<AttachmentInfo> attachments) {
        this.email = email;
        this.password = password;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public List<AttachmentInfo> getAttachments() {
        return attachments;
    }
}
