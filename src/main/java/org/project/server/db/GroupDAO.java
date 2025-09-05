package org.project.server.db;

import org.project.models.Group;
import org.project.server.db.DBConnection;

import java.sql.*;
import java.util.UUID;
public class GroupDAO {
    // creating new group
    public void addGroup(Group group) {
        String insertGroupSql = "INSERT INTO groups (groupid, groupname, groupcreatorid) VALUES (?, ?, ?)";
        String insertGroupMemberSql = "INSERT INTO group_members (group_id, member_id) VALUES (?, ?)";
        String insertAllChatsSql = "INSERT INTO all_chats (user_id, chat_id, last_message) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // adding the group
            try (PreparedStatement stmt1 = conn.prepareStatement(insertGroupSql)) {
                stmt1.setObject(1, group.getGroupId());
                stmt1.setString(2, group.getGroupName());
                stmt1.setObject(3, group.getGroupCreatorId());
                stmt1.executeUpdate();
            }

            // adding the creator to the group_members table
            try (PreparedStatement stmt2 = conn.prepareStatement(insertGroupMemberSql)) {
                stmt2.setObject(1, group.getGroupId());
                stmt2.setObject(2, group.getGroupCreatorId());
                stmt2.executeUpdate();
            }

            // adding the group to all_chats table
            try (PreparedStatement stmt3 = conn.prepareStatement(insertAllChatsSql)) {
                stmt3.setObject(1, group.getGroupCreatorId());
                stmt3.setObject(2, group.getGroupId());
                stmt3.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // زمان ساخت
                stmt3.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = DBConnection.getConnection()) {
                conn.rollback(); // in case something goes wrong, all the commits will roll back
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    // getting the group with id
    public Group getGroupById(UUID groupId) {
        String sql = "SELECT * FROM groups WHERE groupid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, groupId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Group(
                        (UUID) rs.getObject("groupid"),
                        rs.getString("groupname"),
                        (UUID) rs.getObject("groupcreatorid")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // deleting the group
    public boolean deleteGroup(UUID groupId) {
        String deleteGroupMembersSql = "DELETE FROM group_members WHERE group_id = ?";
        String deleteAllChatsSql = "DELETE FROM all_chats WHERE chat_id = ?";
        String deleteGroupSql = "DELETE FROM groups WHERE groupid = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // deleting members
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteGroupMembersSql)) {
                stmt1.setObject(1, groupId);
                stmt1.executeUpdate();
            }

            // deleting the group from all_chats table
            try (PreparedStatement stmt2 = conn.prepareStatement(deleteAllChatsSql)) {
                stmt2.setObject(1, groupId);
                stmt2.executeUpdate();
            }

            // deleting the group
            try (PreparedStatement stmt3 = conn.prepareStatement(deleteGroupSql)) {
                stmt3.setObject(1, groupId);
                int affected = stmt3.executeUpdate();

                conn.commit();
                return affected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = DBConnection.getConnection()) {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}