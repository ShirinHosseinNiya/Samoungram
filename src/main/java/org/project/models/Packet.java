package org.project.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public class Packet implements Serializable {
    private PacketType type;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private Timestamp timestamp;
    private boolean success;
    private String errorMessage;

    public Packet(PacketType type) {
        this.type = type;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
