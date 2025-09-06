package org.project.server;

import org.project.models.Group;
import org.project.server.db.DBConnection;
import org.project.server.db.GroupDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupServer {
    private final Connection conn;
    private final GroupDAO groupDAO;

    public GroupServer() throws SQLException {
        this.conn = DBConnection.getConnection();
        this.groupDAO = new GroupDAO(conn);
    }

    public GroupServer(Connection connection) {
        this.conn = connection;
        this.groupDAO = new GroupDAO(conn);
    }

    public void createGroup(UUID groupId, String groupName, UUID creatorId) throws SQLException {
        groupDAO.createGroup(groupId, groupName, creatorId);
    }

    public void addGroup(UUID groupId, String groupName, UUID creatorId) throws SQLException {
        groupDAO.addGroup(groupId, groupName, creatorId);
    }

    public UUID getGroupCreatorId(UUID groupId) throws SQLException {
        return groupDAO.getGroupCreatorId(groupId);
    }

    public void addMember(UUID groupId, UUID memberId) throws SQLException {
        groupDAO.addMemberToGroup(groupId, memberId);
    }

    public void removeMember(UUID groupId, UUID memberId) throws SQLException {
        groupDAO.removeMemberFromGroup(groupId, memberId);
    }

    public List<UUID> listMemberIds(UUID groupId) throws SQLException {
        return groupDAO.listMemberIds(groupId);
    }

    public Map<UUID, String> listMemberProfiles(UUID groupId) throws SQLException {
        return groupDAO.listMemberProfiles(groupId);
    }
}
