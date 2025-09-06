package org.project.server.db;

import java.sql.*;
import java.util.*;

public class ChannelDAO {
    private final Connection conn;

    public ChannelDAO(Connection conn) {
        this.conn = conn;
    }

    public void createChannel(UUID channelId, String channelName, UUID ownerId) throws SQLException {
        String sql = "INSERT INTO channels (channel_id, channel_name, channel_owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            ps.setString(2, channelName);
            ps.setObject(3, ownerId);
            ps.executeUpdate();
        }
    }

    public UUID getChannelOwnerId(UUID channelId) throws SQLException {
        String sql = "SELECT channel_owner_id FROM channels WHERE channel_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return (UUID) rs.getObject(1);
                return null;
            }
        }
    }

    public void addMemberToChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO channel_members (channel_id, member_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            ps.setObject(2, memberId);
            ps.executeUpdate();
        }
    }

    public void removeMemberFromChannel(UUID channelId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM channel_members WHERE channel_id = ? AND member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            ps.setObject(2, memberId);
            ps.executeUpdate();
        }
    }

    public List<UUID> listMemberIds(UUID channelId) throws SQLException {
        String sql = "SELECT member_id FROM channel_members WHERE channel_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                List<UUID> ids = new ArrayList<>();
                while (rs.next()) ids.add((UUID) rs.getObject(1));
                return ids;
            }
        }
    }

    public Map<UUID, String> listMemberProfiles(UUID channelId) throws SQLException {
        String sql = "SELECT u.id, u.profile_name FROM channel_members cm JOIN users u ON u.id = cm.member_id WHERE cm.channel_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<UUID, String> out = new LinkedHashMap<>();
                while (rs.next()) out.put((UUID) rs.getObject(1), rs.getString(2));
                return out;
            }
        }
    }
}
