package org.project.models;

import java.util.UUID;

public class PrivateChat {
    private UUID chatId;
    private UUID participate1Id;
    private UUID participate2Id;

    public PrivateChat(UUID chatId, UUID participate1Id, UUID participate2Id) {
        this.chatId = chatId;
        this.participate1Id = participate1Id;
        this.participate2Id = participate2Id;
    }

    public UUID getChatId() {
        return chatId;
    }

    public UUID getParticipate1Id() {
        return participate1Id;
    }

    public UUID getParticipate2Id() {
        return participate2Id;
    }
}
