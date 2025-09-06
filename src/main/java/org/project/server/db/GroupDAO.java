package org.project.server.db;

import java.sql.*;
import java.util.*;

public class GroupDAO {
    private final Connection conn;

    public GroupDAO(Connection conn) {
        this.conn = conn;
    }

    public void createGroup(UUID groupId, String groupName, UUID creatorId) throws SQLException {
        String sql = "INSERT INTO groups (groupid, groupname, groupcreatorid) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            ps.setString(2, groupName);
            ps.setObject(3, creatorId);
            ps.executeUpdate();
        }
    }

    public void addGroup(UUID groupId, String groupName, UUID creatorId) throws SQLException {
        createGroup(groupId, groupName, creatorId);
    }

    public UUID getGroupCreatorId(UUID groupId) throws SQLException {
        String sql = "SELECT groupcreatorid FROM groups WHERE groupid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return (UUID) rs.getObject(1);
                return null;
            }
        }
    }

    public void addMemberToGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO group_members (group_id, member_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            ps.setObject(2, memberId);
            ps.executeUpdate();
        }
    }

    public void removeMemberFromGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            ps.setObject(2, memberId);
            ps.executeUpdate();
        }
    }

    public List<UUID> listMemberIds(UUID groupId) throws SQLException {
        String sql = "SELECT member_id FROM group_members WHERE group_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                List<UUID> ids = new ArrayList<>();
                while (rs.next()) ids.add((UUID) rs.getObject(1));
                return ids;
            }
        }
    }

    public Map<UUID, String> listMemberProfiles(UUID groupId) throws SQLException {
        String sql = "SELECT u.id, u.profile_name FROM group_members gm JOIN users u ON u.id = gm.member_id WHERE gm.group_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<UUID, String> out = new LinkedHashMap<>();
                while (rs.next()) out.put((UUID) rs.getObject(1), rs.getString(2));
                return out;
            }
        }
    }
}
