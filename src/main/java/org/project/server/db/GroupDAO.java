package org.project.server.db;

import org.project.models.Group;
import java.sql.*;
import java.util.*;

public class GroupDAO {
    private final Connection conn;

    public GroupDAO(Connection conn) {
        this.conn = conn;
    }

    public void addGroup(UUID groupId, String name, UUID creatorId) throws SQLException {
        String sql = "INSERT INTO groups (groupid, groupname, groupcreatorid) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            stmt.setString(2, name);
            stmt.setObject(3, creatorId);
            stmt.executeUpdate();
        }
    }

    public void addMemberToGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "INSERT INTO group_members (group_id, member_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            stmt.setObject(2, memberId);
            stmt.executeUpdate();
        }
    }

    public void removeMemberFromGroup(UUID groupId, UUID memberId) throws SQLException {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            stmt.setObject(2, memberId);
            stmt.executeUpdate();
        }
    }

    public UUID getGroupCreatorId(UUID groupId) throws SQLException {
        String sql = "SELECT groupcreatorid FROM groups WHERE groupid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("groupcreatorid");
            }
            return null;
        }
    }

    public Group findGroupById(UUID groupId) throws SQLException {
        String sql = "SELECT * FROM groups WHERE groupid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Group(
                        (UUID) rs.getObject("groupid"),
                        rs.getString("groupname"),
                        (UUID) rs.getObject("groupcreatorid")
                );
            }
        }
        return null;
    }

    public List<UUID> listMembers(UUID groupId) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String sql = "SELECT member_id FROM group_members WHERE group_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add((UUID) rs.getObject("member_id"));
            }
        }
        return members;
    }

    public void updateGroupName(UUID groupId, String newName) throws SQLException {
        String sql = "UPDATE groups SET groupname = ? WHERE groupid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setObject(2, groupId);
            ps.executeUpdate();
        }
    }
}
