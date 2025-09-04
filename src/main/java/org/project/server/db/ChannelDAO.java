package org.project.server.db;

import org.project.models.Channel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class ChannelDAO {
    private Connection conn;

    public ChannelDAO(Connection conn) {
        this.conn = conn;
    }

    // creating new channels
    public void addChannel(Channel channel) throws SQLException {
        String insertChannel = "INSERT INTO channels (channel_id, channel_name, channel_owner_id) VALUES (?, ?, ?)";
        String insertAllChats = "INSERT INTO all_chats (user_id, chat_id, last_message) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(insertChannel);
            PreparedStatement stmt2 = conn.prepareStatement(insertAllChats)) {

                // insert into channels
                stmt1.setObject(1, channel.getChannelId());
                stmt1.setString(2, channel.getChannelName());
                stmt1.setObject(3, channel.getChannelOwnerId());
                stmt1.executeUpdate();

                // insert into all_chats
                stmt2.setObject(1, channel.getChannelOwnerId());
                stmt2.setObject(2, channel.getChannelId());
                stmt2.setObject(3, LocalDateTime.now());
                stmt2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // getting channel by ID
    public Channel getChannelById(UUID channelId) throws SQLException {
        String sql = "SELECT * FROM channels WHERE channel_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, channelId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Channel(
                        rs.getString("channel_name"),
                        UUID.fromString(rs.getString("channel_owner_id"))
                );
            }
        }
        return null;
    }

    // deleting channel
    public boolean deleteChannel(UUID channelId) {
        String deleteAllChatsSql = "DELETE FROM all_chats WHERE chat_id = ?";
        String deleteChannelSql = "DELETE FROM channels WHERE channel_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // deleting from all_chats
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteAllChatsSql)) {
                stmt1.setObject(1, channelId);
                stmt1.executeUpdate();
            }

            // deleting from channels
            try (PreparedStatement stmt2 = conn.prepareStatement(deleteChannelSql)) {
                stmt2.setObject(1, channelId);
                int affected = stmt2.executeUpdate();

                conn.commit();
                return affected > 0; // if the channel was deleted
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // if an error happened
                DBConnection.getConnection().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
