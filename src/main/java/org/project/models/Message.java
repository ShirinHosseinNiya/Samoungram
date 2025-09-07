package org.project.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID messageId;
    private final UUID senderId;
    private final UUID receiverId;
    private final String content;
    private final Timestamp timestamp;
    private String status;
    private String senderProfileName;

    public Message(UUID messageId, UUID senderId, UUID receiverId, String content, Timestamp timestamp, String status) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
        this.senderProfileName = null;
    }

    public Message(UUID messageId, UUID senderId, UUID receiverId, String content, Timestamp timestamp, String status, String senderProfileName) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
        this.senderProfileName = senderProfileName;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderProfileName() {
        return senderProfileName;
    }

    public void setSenderProfileName(String senderProfileName) {
        this.senderProfileName = senderProfileName;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", senderProfileName='" + senderProfileName + '\'' +
                '}';
    }
}