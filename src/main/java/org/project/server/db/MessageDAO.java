package org.project.server.db;

import org.project.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDAO {
    private final Connection conn;

    public MessageDAO(Connection conn) {
        this.conn = conn;
    }

    public void addMessage(Message message) throws SQLException {
        String sql = "INSERT INTO message_history (message_id, sender_id, receiver_is, content, \"timestamp\", status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, message.getMessageId());
            ps.setObject(2, message.getSenderId());
            ps.setObject(3, message.getReceiverId());
            ps.setString(4, message.getContent());
            ps.setTimestamp(5, message.getTimestamp());
            ps.setString(6, message.getStatus());
            ps.executeUpdate();
        }
    }

    public List<Message> getHistoryForPrivateChat(UUID user1, UUID user2) throws SQLException {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT mh.*, u.profile_name as sender_profile_name FROM message_history mh JOIN users u ON mh.sender_id = u.id WHERE (sender_id = ? AND receiver_is = ?) OR (sender_id = ? AND receiver_is = ?) ORDER BY \"timestamp\" ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user1);
            ps.setObject(2, user2);
            ps.setObject(3, user2);
            ps.setObject(4, user1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.add(new Message(
                        (UUID) rs.getObject("message_id"),
                        (UUID) rs.getObject("sender_id"),
                        (UUID) rs.getObject("receiver_is"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("status"),
                        rs.getString("sender_profile_name")
                ));
            }
        }
        return history;
    }

    public List<Message> getHistoryForGroupOrChannel(UUID chatId) throws SQLException {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT mh.*, u.profile_name as sender_profile_name FROM message_history mh JOIN users u ON mh.sender_id = u.id WHERE receiver_is = ? ORDER BY \"timestamp\" ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.add(new Message(
                        (UUID) rs.getObject("message_id"),
                        (UUID) rs.getObject("sender_id"),
                        (UUID) rs.getObject("receiver_is"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("status"),
                        rs.getString("sender_profile_name")
                ));
            }
        }
        return history;
    }

    public void markMessagesAsRead(UUID readerId, UUID chatId) throws SQLException {
        String sql = "UPDATE message_history SET status = 'READ' WHERE receiver_is = ? AND sender_id = ? AND status <> 'READ'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, readerId);
            ps.setObject(2, chatId);
            ps.executeUpdate();
        }
    }
}