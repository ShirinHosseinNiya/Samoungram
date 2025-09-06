package org.project.server.db;

import java.sql.*;
import java.util.*;

public class GroupDAO {
    private final Connection conn;

    public GroupDAO(Connection conn) {
        this.conn = conn;
    }

    public void addGroup(UUID groupId, String name, UUID creatorId) throws SQLException {
        String sql = "INSERT INTO groups (id, name, creator_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId.toString());
            stmt.setString(2, name);
            stmt.setString(3, creatorId.toString());
            stmt.executeUpdate();
        }
    }

    public void addMemberToGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId.toString());
            stmt.setString(2, memberId.toString());
            stmt.executeUpdate();
        }
    }

    public void removeMemberFromGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId.toString());
            stmt.setString(2, memberId.toString());
            stmt.executeUpdate();
        }
    }

    public UUID getGroupCreatorId(UUID groupId) throws SQLException {
        String sql = "SELECT creator_id FROM groups WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("creator_id"));
            }
            return null;
        }
    }

    public List<UUID> listMembers(UUID groupId) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("user_id")));
            }
        }
        return members;
    }
}
