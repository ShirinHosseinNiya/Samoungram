package org.project.server.db;

import org.project.client.views.ChatItemViewModel;
import org.project.models.PrivateChat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDAO {
    private final Connection conn;

    public ChatDAO(Connection conn) {
        this.conn = conn;
    }

    public List<ChatItemViewModel> getAllChatsForUser(UUID userId) throws SQLException {
        List<ChatItemViewModel> chats = new ArrayList<>();
        String sql =
                "SELECT " +
                        "pc.chat_id, " +
                        "u.profile_name AS display_name, " +
                        "'PRIVATE' AS type, " +
                        "mh.content AS last_message_content, " +
                        "mh.timestamp AS last_message_timestamp " +
                        "FROM private_chats pc " +
                        "JOIN users u ON u.id = CASE WHEN pc.participate1_id = ? THEN pc.participate2_id ELSE pc.participate1_id END " +
                        "LEFT JOIN message_history mh ON (mh.sender_id = pc.participate1_id AND mh.receiver_is = pc.participate2_id) OR (mh.sender_id = pc.participate2_id AND mh.receiver_is = pc.participate1_id) " +
                        "WHERE pc.participate1_id = ? OR pc.participate2_id = ? " +
                        "UNION ALL " +
                        "SELECT " +
                        "g.groupid AS chat_id, " +
                        "g.groupname AS display_name, " +
                        "'GROUP' AS type, " +
                        "mh.content AS last_message_content, " +
                        "mh.timestamp AS last_message_timestamp " +
                        "FROM groups g " +
                        "JOIN group_members gm ON g.groupid = gm.group_id " +
                        "LEFT JOIN message_history mh ON mh.receiver_is = g.groupid " +
                        "WHERE gm.member_id = ? " +
                        "UNION ALL " +
                        "SELECT " +
                        "c.channel_id, " +
                        "c.channel_name AS display_name, " +
                        "'CHANNEL' AS type, " +
                        "mh.content AS last_message_content, " +
                        "mh.timestamp AS last_message_timestamp " +
                        "FROM channels c " +
                        "JOIN channel_members cm ON c.channel_id = cm.channel_id " +
                        "LEFT JOIN message_history mh ON mh.receiver_is = c.channel_id " +
                        "WHERE cm.member_id = ? " +
                        "ORDER BY last_message_timestamp DESC NULLS LAST";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setObject(2, userId);
            ps.setObject(3, userId);
            ps.setObject(4, userId);
            ps.setObject(5, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long timestamp = rs.getTimestamp("last_message_timestamp") != null ? rs.getTimestamp("last_message_timestamp").getTime() : 0;
                String lastMessage = rs.getString("last_message_content") != null ? rs.getString("last_message_content") : "";
                ChatItemViewModel.ChatType type = ChatItemViewModel.ChatType.valueOf(rs.getString("type"));
                chats.add(new ChatItemViewModel(
                        (UUID) rs.getObject("chat_id"),
                        rs.getString("display_name"),
                        type,
                        lastMessage,
                        timestamp,
                        0
                ));
            }
        }
        return chats;
    }

    public void updatePVLastMessage(UUID senderId, UUID receiverId, Timestamp ts) throws SQLException {
    }

    public void updateGroupLastMessage(UUID groupId, Timestamp ts) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE chat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts);
            ps.setString(2, groupId.toString());
            ps.executeUpdate();
        }
    }

    public void updateChannelLastMessage(UUID channelId, Timestamp ts) throws SQLException {
        String sql = "UPDATE all_chats SET last_message = ? WHERE chat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts);
            ps.setString(2, channelId.toString());
            ps.executeUpdate();
        }
    }

    public PrivateChat findPrivateChat(UUID user1, UUID user2) throws SQLException {
        String sql = "SELECT * FROM private_chats WHERE (participate1_id = ? AND participate2_id = ?) OR (participate1_id = ? AND participate2_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user1);
            ps.setObject(2, user2);
            ps.setObject(3, user2);
            ps.setObject(4, user1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PrivateChat((UUID) rs.getObject("chat_id"), (UUID) rs.getObject("participate1_id"), (UUID) rs.getObject("participate2_id"));
            }
            return null;
        }
    }
}