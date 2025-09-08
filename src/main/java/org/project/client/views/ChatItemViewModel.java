package org.project.client.views;

import java.util.UUID;

public class ChatItemViewModel {
    public enum ChatType { PRIVATE, GROUP, CHANNEL }

    private final UUID chatId;
    private final String displayName;
    private final ChatType type;
    private final String lastMessage;
    private final long lastTimestamp;
    private int unread;
    private final UUID ownerId;

    public ChatItemViewModel(UUID chatId, String displayName, ChatType type,
                             String lastMessage, long lastTimestamp, int unread, UUID ownerId) {
        this.chatId = chatId;
        this.displayName = displayName;
        this.type = type;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.unread = unread;
        this.ownerId = ownerId;
    }

    public ChatItemViewModel(UUID chatId, String displayName, ChatType type,
                             String lastMessage, long lastTimestamp, int unread) {
        this(chatId, displayName, type, lastMessage, lastTimestamp, unread, null);
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public UUID getChatId() { return chatId; }
    public String getDisplayName() { return displayName; }
    public ChatType getType() { return type; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }
    public int getUnread() { return unread; }
    public UUID getOwnerId() { return ownerId; }
}