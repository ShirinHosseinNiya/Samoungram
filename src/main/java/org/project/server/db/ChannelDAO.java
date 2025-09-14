package org.project.server.db;

import org.project.models.Channel;
import java.sql.*;
import java.util.*;

public class ChannelDAO {
    private final Connection conn;

    public ChannelDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Channel> searchChannelsByName(String query) throws SQLException {
        List<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM channels WHERE channel_name ILIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                channels.add(new Channel(
                        (UUID) rs.getObject("channel_id"),
                        rs.getString("channel_name"),
                        (UUID) rs.getObject("channel_owner_id")
                ));
            }
        }
        return channels;
    }

    public void addChannel(UUID channelId, String name, UUID ownerId) throws SQLException {
        String sql = "INSERT INTO channels (channel_id, channel_name, channel_owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            stmt.setString(2, name);
            stmt.setObject(3, ownerId);
            stmt.executeUpdate();
        }
    }

    public void addMemberToChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO channel_members (channel_id, member_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            stmt.setObject(2, memberId);
            stmt.executeUpdate();
        }
    }

    public void removeMemberFromChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM channel_members WHERE channel_id = ? AND member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            stmt.setObject(2, memberId);
            stmt.executeUpdate();
        }
    }

    public UUID getChannelOwnerId(UUID channelId) throws SQLException {
        String sql = "SELECT channel_owner_id FROM channels WHERE channel_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("channel_owner_id");
            }
            return null;
        }
    }

    public Channel findChannelById(UUID channelId) throws SQLException {
        String sql = "SELECT * FROM channels WHERE channel_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Channel(
                        (UUID) rs.getObject("channel_id"),
                        rs.getString("channel_name"),
                        (UUID) rs.getObject("channel_owner_id")
                );
            }
        }
        return null;
    }

    public List<UUID> listMembers(UUID channelId) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String sql = "SELECT member_id FROM channel_members WHERE channel_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add((UUID) rs.getObject("member_id"));
            }
        }
        return members;
    }

    public void updateChannelName(UUID channelId, String newName) throws SQLException {
        String sql = "UPDATE channels SET channel_name = ? WHERE channel_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setObject(2, channelId);
            ps.executeUpdate();
        }
    }
}