package org.project.server.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.project.models.Message;

public class MessageDAO {

    // saving new messages in database
    public void insertMessage(Message message) throws SQLException {
        String sql = "INSERT INTO message_history (sender_id, receiver_id, content, timestamp, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, message.getSenderId());
            stmt.setObject(2, message.getReceiverId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getTimestamp());
            stmt.setString(5, message.getStatus().toString());
            stmt.executeUpdate();
        }
    }

    // getting messages from database
    public List<Message> getMessagesByChatId(UUID receiverId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message_history WHERE receiver_id = ? ORDER BY timestamp";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, receiverId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        (UUID) rs.getObject("sender_id"),
                        (UUID) rs.getObject("receiver_id"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp").toLocalDateTime(),
                        Message.MessageStatus.valueOf(rs.getString("status"))
                ));
            }
        }
        return messages;
    }
}