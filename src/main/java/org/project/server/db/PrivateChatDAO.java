package org.project.server.db;

import java.sql.*;
import java.util.*;

public class PrivateChatDAO {
    private final Connection conn;

    public PrivateChatDAO(Connection conn) {
        this.conn = conn;
    }

    public UUID createPrivateChat(UUID user1, UUID user2) throws SQLException {
        UUID chatId = UUID.randomUUID();
        String sql = "INSERT INTO private_chats (chat_id, user1_id, user2_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, chatId);
            ps.setObject(2, user1);
            ps.setObject(3, user2);
            ps.executeUpdate();
        }
        return chatId;
    }

    public UUID getPrivateChat(UUID user1, UUID user2) throws SQLException {
        String sql = "SELECT chat_id FROM private_chats WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user1);
            ps.setObject(2, user2);
            ps.setObject(3, user2);
            ps.setObject(4, user1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return (UUID) rs.getObject(1);
                return null;
            }
        }
    }
}
