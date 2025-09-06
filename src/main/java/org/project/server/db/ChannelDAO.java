package org.project.server.db;

import java.sql.*;
import java.util.*;

public class ChannelDAO {
    private final Connection conn;

    public ChannelDAO(Connection conn) {
        this.conn = conn;
    }

    public void addChannel(UUID channelId, String name, UUID ownerId) throws SQLException {
        String sql = "INSERT INTO channels (id, name, owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channelId.toString());
            stmt.setString(2, name);
            stmt.setString(3, ownerId.toString());
            stmt.executeUpdate();
        }
    }

    public void addMemberToChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO channel_members (channel_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channelId.toString());
            stmt.setString(2, memberId.toString());
            stmt.executeUpdate();
        }
    }

    public void removeMemberFromChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM channel_members WHERE channel_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channelId.toString());
            stmt.setString(2, memberId.toString());
            stmt.executeUpdate();
        }
    }

    public UUID getChannelOwnerId(UUID channelId) throws SQLException {
        String sql = "SELECT owner_id FROM channels WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channelId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("owner_id"));
            }
            return null;
        }
    }

    public List<UUID> listMembers(UUID channelId) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String sql = "SELECT user_id FROM channel_members WHERE channel_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channelId.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("user_id")));
            }
        }
        return members;
    }
}
