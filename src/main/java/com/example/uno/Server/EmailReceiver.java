package com.example.uno.Server;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Properties;

public class EmailReceiver {
    private static final String EMAIL_HOST = "imap.gmail.com";
    private static final String EMAIL_USERNAME = "your-username";
    private static final String EMAIL_PASSWORD = "your-password";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/uno";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";
    private static final int BATCH_SIZE = 10;

    public void receiveEmail() {
        try {
            // Thiết lập cấu hình cho kết nối IMAP
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.host", EMAIL_HOST);
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");

            // Tạo phiên làm việc với máy chủ IMAP
            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imap");
            store.connect(EMAIL_HOST, EMAIL_USERNAME, EMAIL_PASSWORD);

            // Tạo kết nối đến cơ sở dữ liệu
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                // Mở thư mục INBOX và lấy ra tất cả email chưa được đọc
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                // Tạo và khởi chạy các luồng xử lý email
                int totalEmails = messages.length;
                for (int i = 0; i < totalEmails; i += BATCH_SIZE) {
                    // Lấy một batch email
                    Message[] batch = Arrays.copyOfRange(messages, i, Math.min(i + BATCH_SIZE, totalEmails));

                    // Tạo và khởi chạy một luồng xử lý email cho batch này
                    EmailHandlerThread thread = new EmailHandlerThread(batch, connection);
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                store.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EmailHandlerThread extends Thread {
        private Message[] messages;
        private Connection connection;

        public EmailHandlerThread(Message[] messages, Connection connection) {
            this.messages = messages;
            this.connection = connection;
        }

        @Override
        public void run() {
            // Xử lý email theo batch
            try {
                processBatchEmail(messages, connection);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        private void processBatchEmail(Message[] batch, Connection connection) throws MessagingException {
            // Thực hiện các xử lý cho từng email trong batch
            for (Message message : batch) {
                String subject = message.getSubject();
                String sender = InternetAddress.toString(message.getFrom());
                // Lưu thông tin email vào cơ sở dữ liệu
                saveEmailToDatabase(connection, sender, subject);
            }
        }
    }

    private void saveEmailToDatabase(Connection connection, String sender, String subject) {
        // Thực hiện lưu thông tin email vào cơ sở dữ liệu
        String query = "INSERT INTO emails (sender, subject) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sender);
            statement.setString(2, subject);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
