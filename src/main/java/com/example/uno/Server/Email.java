package com.example.uno.Server;

import java.util.List;

public class Email {
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    private List<String> attachments; // Danh sách các đường dẫn đến file cần gửi kèm

    // Constructor
    public Email(String sender, String recipient, String subject, String body, List<String> attachments) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    // Getters and Setters

    public String getBody() {
        return body;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    // Override toString() method
    @Override
    public String toString() {
        return "Email{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    // Method to add attachment
    public void addAttachment(String filePath) {
        attachments.add(filePath);
    }
}
