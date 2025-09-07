package org.project.server;

import org.project.models.Message;
import org.project.server.db.MessageDAO;
import org.project.server.db.ChatDAO;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class MessageServer {
    private final MessageDAO messageDAO;
    private final ChatDAO chatDAO;

    public MessageServer(MessageDAO messageDAO, ChatDAO chatDAO) {
        this.messageDAO = messageDAO;
        this.chatDAO = chatDAO;
    }

    public void sendMessageToUser(UUID senderId, UUID receiverId, String content) throws SQLException {
        Message message = new Message(
                UUID.randomUUID(),
                senderId,
                receiverId,
                content,
                new Timestamp(System.currentTimeMillis()),
                "sent"
        );
        messageDAO.addMessage(message);
    }

    public void sendMessageToGroup(UUID senderId, UUID groupId, String content) throws SQLException {
        Message message = new Message(
                UUID.randomUUID(),
                senderId,
                groupId,
                content,
                new Timestamp(System.currentTimeMillis()),
                "sent"
        );
        messageDAO.addMessage(message);
    }

    public void sendMessageToChannel(UUID senderId, UUID channelId, String content) throws SQLException {
        Message message = new Message(
                UUID.randomUUID(),
                senderId,
                channelId,
                content,
                new Timestamp(System.currentTimeMillis()),
                "sent"
        );
        messageDAO.addMessage(message);
    }
}