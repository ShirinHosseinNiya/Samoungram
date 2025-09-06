package org.project.server.db;

import java.sql.*;
import java.util.*;

public class ChatDAO {
    private final Connection conn;

    public ChatDAO(Connection conn) {
        this.conn = conn;
    }

    public void updatePVLastMessage(UUID senderId, UUID receiverId, Timestamp ts) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE (user_id = ? AND chat_id = ?) OR (user_id = ? AND chat_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts);
            ps.setObject(2, senderId);
            ps.setString(3, receiverId.toString());
            ps.setObject(4, receiverId);
            ps.setString(5, senderId.toString());
            ps.executeUpdate();
        }
    }

    public void updateGroupLastMessage(UUID groupId, Timestamp ts) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE chat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts);
            ps.setString(2, groupId.toString());
            ps.executeUpdate();
        }
    }

    public void updateChannelLastMessage(UUID channelId, Timestamp ts) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE chat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts);
            ps.setString(2, channelId.toString());
            ps.executeUpdate();
        }
    }
}
