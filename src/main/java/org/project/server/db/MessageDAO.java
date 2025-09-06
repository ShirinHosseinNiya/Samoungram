package org.project.server.db;

import org.project.models.Message;

import java.sql.*;
import java.util.*;

public class MessageDAO {
    private final Connection conn;

    public MessageDAO(Connection conn) {
        this.conn = conn;
    }

    // پیام خصوصی یا عمومی (هر receiver_is می‌تونه user_id یا group_id یا channel_id باشه)
    public void addMessage(Message message) throws SQLException {
        String sql = "INSERT INTO message_history (message_id, sender_id, receiver_is, content, \"timestamp\", status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, message.getMessageId());
            ps.setObject(2, message.getSenderId());
            ps.setObject(3, message.getReceiverId());
            ps.setString(4, message.getContent());
            ps.setTimestamp(5, message.getTimestamp());
            ps.setString(6, String.valueOf(message.getStatus()));
            ps.executeUpdate();
        }
    }

    public void saveMessage(UUID messageId, UUID senderId, UUID receiverId, String content, Timestamp timestamp, String status) throws SQLException {
        String sql = "INSERT INTO message_history (message_id, sender_id, receiver_is, content, \"timestamp\", status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, messageId);
            ps.setObject(2, senderId);
            ps.setObject(3, receiverId);
            ps.setString(4, content);
            ps.setTimestamp(5, timestamp);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }

    public void saveGroupMessage(UUID messageId, UUID senderId, UUID groupId, String content, Timestamp ts, String status) throws SQLException {
        saveMessage(messageId, senderId, groupId, content, ts, status);
    }

    public void saveChannelMessage(UUID messageId, UUID senderId, UUID channelId, String content, Timestamp ts, String status) throws SQLException {
        saveMessage(messageId, senderId, channelId, content, ts, status);
    }

    public List<String> getMessagesBetween(UUID user1, UUID user2) throws SQLException {
        String sql = "SELECT content FROM message_history WHERE (sender_id = ? AND receiver_is = ?) OR (sender_id = ? AND receiver_is = ?) ORDER BY \"timestamp\"";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user1);
            ps.setObject(2, user2);
            ps.setObject(3, user2);
            ps.setObject(4, user1);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> messages = new ArrayList<>();
                while (rs.next()) messages.add(rs.getString(1));
                return messages;
            }
        }
    }

    public List<String> getGroupMessages(UUID groupId) throws SQLException {
        String sql = "SELECT content FROM message_history WHERE receiver_is = ? ORDER BY \"timestamp\"";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> msgs = new ArrayList<>();
                while (rs.next()) msgs.add(rs.getString(1));
                return msgs;
            }
        }
    }

    public List<String> getChannelMessages(UUID channelId) throws SQLException {
        String sql = "SELECT content FROM message_history WHERE receiver_is = ? ORDER BY \"timestamp\"";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> msgs = new ArrayList<>();
                while (rs.next()) msgs.add(rs.getString(1));
                return msgs;
            }
        }
    }

    public void updateMessageStatus(UUID messageId, String status) throws SQLException {
        String sql = "UPDATE message_history SET status = ? WHERE message_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, messageId);
            ps.executeUpdate();
        }
    }
}
