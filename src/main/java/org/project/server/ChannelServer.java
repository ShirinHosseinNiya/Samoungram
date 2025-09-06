package org.project.server;

import org.project.server.db.ChannelDAO;
import org.project.server.db.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChannelServer {
    private final Connection conn;
    private final ChannelDAO channelDAO;

    public ChannelServer() throws SQLException {
        this.conn = DBConnection.getConnection();
        this.channelDAO = new ChannelDAO(conn);
    }

    public ChannelServer(Connection conn) {
        this.conn = conn;
        this.channelDAO = new ChannelDAO(conn);
    }

    public void createChannel(UUID channelId, String channelName, UUID ownerId) throws SQLException {
        channelDAO.createChannel(channelId, channelName, ownerId);
    }

    public UUID getChannelOwnerId(UUID channelId) throws SQLException {
        return channelDAO.getChannelOwnerId(channelId);
    }

    public void addMember(UUID channelId, UUID memberId) throws SQLException {
        channelDAO.addMemberToChannel(channelId, memberId);
    }

    public void removeMember(UUID channelId, UUID memberId) throws SQLException {
        channelDAO.removeMemberFromChannel(channelId, memberId);
    }

    public List<UUID> listMemberIds(UUID channelId) throws SQLException {
        return channelDAO.listMemberIds(channelId);
    }

    public Map<UUID, String> listMemberProfiles(UUID channelId) throws SQLException {
        return channelDAO.listMemberProfiles(channelId);
    }
}
