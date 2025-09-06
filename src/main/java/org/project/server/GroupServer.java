package org.project.server;

import org.project.server.db.GroupDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class GroupServer {
    private final GroupDAO groupDAO;

    public GroupServer(Connection conn) throws SQLException {
        this.groupDAO = new GroupDAO(conn);
    }

    public void createGroup(UUID groupId, String groupName, UUID creatorId) throws SQLException {
        groupDAO.addGroup(groupId, groupName, creatorId);
    }

    public void addMember(UUID groupId, UUID memberId) throws SQLException {
        groupDAO.addMemberToGroup(groupId, memberId);
    }

    public void removeMember(UUID groupId, UUID memberId) throws SQLException {
        groupDAO.removeMemberFromGroup(groupId, memberId);
    }

    public UUID getGroupCreatorId(UUID groupId) throws SQLException {
        return groupDAO.getGroupCreatorId(groupId);
    }

    public List<UUID> listMembers(UUID groupId) throws SQLException {
        return groupDAO.listMembers(groupId);
    }
}
