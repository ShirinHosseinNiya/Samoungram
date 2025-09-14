package org.project.server.db;

import org.project.models.PrivateChat;
import java.sql.*;
import java.util.UUID;

public class PrivateChatDAO {
    private final Connection conn;

    public PrivateChatDAO(Connection conn) {
        this.conn = conn;
    }

    public void createPrivateChat(UUID user1, UUID user2) throws SQLException {
        String sql = "INSERT INTO private_chats (chat_id, participate1_id, participate2_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, user1);
            ps.setObject(3, user2);
            ps.executeUpdate();
        }
    }

    public boolean privateChatExists(UUID user1, UUID user2) throws SQLException {
        String sql = "SELECT 1 FROM private_chats WHERE (participate1_id = ? AND participate2_id = ?) OR (participate1_id = ? AND participate2_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user1);
            ps.setObject(2, user2);
            ps.setObject(3, user2);
            ps.setObject(4, user1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public PrivateChat findPrivateChatById(UUID chatId) throws SQLException {
        String sql = "SELECT * FROM private_chats WHERE chat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, chatId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PrivateChat(
                        (UUID) rs.getObject("chat_id"),
                        (UUID) rs.getObject("participate1_id"),
                        (UUID) rs.getObject("participate2_id")
                );
            }
        }
        return null;
    }
}