package org.project.models;

import java.util.UUID;
import java.sql.Timestamp;

public class Message {
    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private Timestamp timestamp;
    private String status;

    public Message(UUID messageId, UUID senderId, UUID receiverId, String content, Timestamp timestamp, String status) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    public UUID getMessageId() { return messageId; }
    public UUID getSenderId() { return senderId; }
    public UUID getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
