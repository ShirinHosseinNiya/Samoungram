package org.project.server.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.project.models.Message;

public class MessageDAO {

    public static boolean saveMessage(Connection conn, Message msg) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, msg.getSenderId().toString());     // UUID → String
            stmt.setString(2, msg.getReceiverId().toString());   // UUID → String
            stmt.setString(3, msg.getContent());
            stmt.setTimestamp(4, Timestamp.valueOf(msg.getTimestamp()));
            stmt.setString(5, msg.getStatus().name());
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<Message> getMessagesBetweenUsers(Connection conn, UUID user1, UUID user2) throws SQLException {
        String sql = "SELECT * FROM messages WHERE " +
                "(sender_id = ? AND receiver_id = ?) OR " +
                "(sender_id = ? AND receiver_id = ?) ORDER BY timestamp";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user1.toString());
            stmt.setString(2, user2.toString());
            stmt.setString(3, user2.toString());
            stmt.setString(4, user1.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) {
                    Message msg = new Message(
                            UUID.fromString(rs.getString("sender_id")),
                            UUID.fromString(rs.getString("receiver_id")),
                            rs.getString("content")
                    );
                    msg.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    msg.setStatus(Message.MessageStatus.valueOf(rs.getString("status")));
                    messages.add(msg);
                }
                return messages;
            }
        }
    }
}

