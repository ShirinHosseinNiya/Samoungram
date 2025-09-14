package org.project.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class ReadMarkerDAO {
    private final Connection conn;

    public ReadMarkerDAO(Connection conn) {
        this.conn = conn;
    }

    public void updateLastReadTimestamp(UUID userId, UUID chatId) throws SQLException {
        String sql = "INSERT INTO read_markers (user_id, chat_id, last_read_timestamp) VALUES (?, ?, NOW()) " +
                "ON CONFLICT (user_id, chat_id) DO UPDATE SET last_read_timestamp = NOW()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setObject(2, chatId);
            ps.executeUpdate();
        }
    }
}