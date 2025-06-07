package org.project;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }

    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime timestamp;
    private MessageStatus status;

    // constructor
    public Message (UUID senderId, UUID receiverId, String content) {
        this.messageId = UUID.randomUUID();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    //getters
    public UUID getMessageId() { return this.messageId; }
    public UUID getSenderId() { return this.senderId; }
    public UUID getReceiverId() { return this.receiverId; }
    public String getContent() { return this.content; }
    public LocalDateTime getTimestamp() { return this.timestamp; }
    public MessageStatus getStatus() { return this.status; }

    //setters
    public void setContent(String content) { this.content = content; }
    public void setStatus(MessageStatus status) { this.status = status; }
}
