package org.project.server;

import org.project.models.Channel;
import org.project.server.db.ChannelDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class ChannelServer {
    private ChannelDAO channelDAO;

    public ChannelServer(Connection conn) {
        this.channelDAO = new ChannelDAO(conn);
    }

    // creating new channels
    public Channel createChannel(String channelName, UUID channelOwnerId) {
        Channel newChannel = new Channel(channelName, channelOwnerId);
        try {
            channelDAO.addChannel(newChannel);
            return newChannel;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // getting channel by ID
    public Channel getChannel(UUID channelId) {
        try {
            return channelDAO.getChannelById(channelId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // deleting channel
    public boolean removeChannel(UUID channelId) {
        channelDAO.deleteChannel(channelId);
        return true;
    }
}