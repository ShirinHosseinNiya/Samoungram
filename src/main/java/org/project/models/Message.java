package org.project.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Message implements Serializable {

    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }

    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime timestamp;
    private MessageStatus status;

    // Constructor for new message creation
    public Message(UUID senderId, UUID receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    // Optional constructor with all fields
    public Message(UUID senderId, UUID receiverId, String content, LocalDateTime timestamp, MessageStatus status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters
    public UUID getSenderId() {
        return senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    // Setters
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
