package org.project.server.db;

import java.sql.*;
import java.util.*;

public class ChatDAO {
    // updating last message in all_chats
    public void updatePVLastMessage(UUID userId, String chatId, Timestamp timestamp) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, timestamp);
            stmt.setObject(2, userId);
            stmt.setObject(3, chatId);
            stmt.executeUpdate();
        }
    }
    public void updateGroupChannelLastMessage(UUID userId, UUID chatId, Timestamp timestamp) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, timestamp);
            stmt.setObject(2, userId);
            stmt.setObject(3, chatId);
            stmt.executeUpdate();
        }
    }

    // getting group members
    public List<UUID> getGroupMembers(UUID groupId) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add((UUID) rs.getObject("user_id"));
            }
        }
        return members;
    }
}