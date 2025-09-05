package org.project.server;

import org.project.models.Message;
import org.project.models.PrivateChat;
import org.project.server.db.ChatDAO;
import org.project.server.db.DBConnection;
import org.project.server.db.MessageDAO;

import java.sql.*;
import java.util.*;

public class MessageServer {
    private final MessageDAO messageDAO = new MessageDAO();
    private final ChatDAO chatDAO = new ChatDAO();

    // PV
    public void sendMessageToUser(UUID senderId, UUID receiverId, String content) throws SQLException {
        Message message = new Message(senderId, receiverId, content);
        messageDAO.insertMessage(message);

        // updating last_message for both users in all_chats table
        chatDAO.updatePVLastMessage(senderId, PrivateChat.generateChatId(senderId, receiverId), message.getTimestamp());
        chatDAO.updatePVLastMessage(receiverId, PrivateChat.generateChatId(senderId, receiverId), message.getTimestamp());
    }

    // groups
    public void sendMessageToGroup(UUID senderId, UUID groupId, String content) throws SQLException {
        Message message = new Message(senderId, groupId, content);
        messageDAO.insertMessage(message);

        // updating last_message for all members
        List<UUID> members = chatDAO.getGroupMembers(groupId);
        for (UUID memberId : members) {
            chatDAO.updateGroupChannelLastMessage(memberId, groupId, message.getTimestamp());
        }
    }

    // channels
    public void sendMessageToChannel(UUID senderId, UUID channelId, String content) throws SQLException {
        Message message = new Message(senderId, channelId, content);
        messageDAO.insertMessage(message);

        // updating last_message for all members
        chatDAO.updateGroupChannelLastMessage(senderId, channelId, message.getTimestamp());
    }
}