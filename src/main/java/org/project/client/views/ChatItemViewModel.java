package org.project.client.views;


import java.util.UUID;


public class ChatItemViewModel {
    public enum ChatType { PRIVATE, GROUP, CHANNEL }


    private final UUID chatId;
    private final String displayName;
    private final ChatType type;
    private final String lastMessage;
    private final long lastTimestamp; // epoch millis
    private final int unread;


    public ChatItemViewModel(UUID chatId, String displayName, ChatType type,
                             String lastMessage, long lastTimestamp, int unread) {
        this.chatId = chatId; this.displayName = displayName; this.type = type;
        this.lastMessage = lastMessage; this.lastTimestamp = lastTimestamp; this.unread = unread;
    }
    public UUID getChatId() { return chatId; }
    public String getDisplayName() { return displayName; }
    public ChatType getType() { return type; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }
    public int getUnread() { return unread; }
}