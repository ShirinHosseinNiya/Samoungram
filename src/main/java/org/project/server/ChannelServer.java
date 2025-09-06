package org.project.server;

import org.project.server.db.ChannelDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ChannelServer {
    private final ChannelDAO channelDAO;

    public ChannelServer(Connection conn) throws SQLException {
        this.channelDAO = new ChannelDAO(conn);
    }

    public void createChannel(UUID channelId, String channelName, UUID ownerId) throws SQLException {
        channelDAO.addChannel(channelId, channelName, ownerId);
    }

    public void addMember(UUID channelId, UUID memberId) throws SQLException {
        channelDAO.addMemberToChannel(channelId, memberId);
    }

    public void removeMember(UUID channelId, UUID memberId) throws SQLException {
        channelDAO.removeMemberFromChannel(channelId, memberId);
    }

    public UUID getChannelOwnerId(UUID channelId) throws SQLException {
        return channelDAO.getChannelOwnerId(channelId);
    }

    public List<UUID> listMembers(UUID channelId) throws SQLException {
        return channelDAO.listMembers(channelId);
    }
}
