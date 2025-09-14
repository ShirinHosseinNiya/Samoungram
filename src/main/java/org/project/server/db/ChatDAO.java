package org.project.server.db;

import org.project.client.views.ChatItemViewModel;
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
                "SELECT chat_id, display_name, type, last_message_content, last_message_timestamp, unread_count, owner_id FROM (" +
                        "   SELECT" +
                        "       other_user.id AS chat_id, other_user.profile_name AS display_name, 'PRIVATE' AS type, " +
                        "       (SELECT content FROM message_history mh WHERE (mh.sender_id = pc.participate1_id AND mh.receiver_is = pc.participate2_id) OR (mh.sender_id = pc.participate2_id AND mh.receiver_is = pc.participate1_id) ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_content, " +
                        "       (SELECT timestamp FROM message_history mh WHERE (mh.sender_id = pc.participate1_id AND mh.receiver_is = pc.participate2_id) OR (mh.sender_id = pc.participate2_id AND mh.receiver_is = pc.participate1_id) ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_timestamp, " +
                        "       (SELECT COUNT(*) FROM message_history WHERE receiver_is = ? AND sender_id = other_user.id AND status <> 'READ') AS unread_count, " +
                        "       NULL AS owner_id " +
                        "   FROM private_chats pc JOIN users other_user ON other_user.id = CASE WHEN pc.participate1_id = ? THEN pc.participate2_id ELSE pc.participate1_id END " +
                        "   WHERE pc.participate1_id = ? OR pc.participate2_id = ? " +
                        "   UNION " +
                        "   SELECT " +
                        "       g.groupid AS chat_id, g.groupname AS display_name, 'GROUP' AS type, " +
                        "       (SELECT content FROM message_history mh WHERE mh.receiver_is = g.groupid ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_content, " +
                        "       (SELECT timestamp FROM message_history mh WHERE mh.receiver_is = g.groupid ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_timestamp, " +
                        "       (SELECT COUNT(*) FROM message_history mh WHERE mh.receiver_is = g.groupid AND mh.sender_id <> ? AND mh.timestamp > COALESCE((SELECT last_read_timestamp FROM read_markers rm WHERE rm.user_id = ? AND rm.chat_id = g.groupid), '1970-01-01')) AS unread_count, " +
                        "       g.groupcreatorid AS owner_id " +
                        "   FROM groups g JOIN group_members gm ON g.groupid = gm.group_id WHERE gm.member_id = ? " +
                        "   UNION " +
                        "   SELECT " +
                        "       c.channel_id AS chat_id, c.channel_name AS display_name, 'CHANNEL' AS type, " +
                        "       (SELECT content FROM message_history mh WHERE mh.receiver_is = c.channel_id ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_content, " +
                        "       (SELECT timestamp FROM message_history mh WHERE mh.receiver_is = c.channel_id ORDER BY mh.timestamp DESC LIMIT 1) AS last_message_timestamp, " +
                        "       (SELECT COUNT(*) FROM message_history mh WHERE mh.receiver_is = c.channel_id AND mh.sender_id <> ? AND mh.timestamp > COALESCE((SELECT last_read_timestamp FROM read_markers rm WHERE rm.user_id = ? AND rm.chat_id = c.channel_id), '1970-01-01')) AS unread_count, " +
                        "       c.channel_owner_id AS owner_id " +
                        "   FROM channels c JOIN channel_members cm ON c.channel_id = cm.channel_id WHERE cm.member_id = ? " +
                        ") AS all_chats_query ORDER BY last_message_timestamp DESC NULLS LAST";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setObject(2, userId);
            ps.setObject(3, userId);
            ps.setObject(4, userId);
            ps.setObject(5, userId);
            ps.setObject(6, userId);
            ps.setObject(7, userId);
            ps.setObject(8, userId);
            ps.setObject(9, userId);
            ps.setObject(10, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("last_message_timestamp");
                long timestamp = (ts != null) ? ts.getTime() : 0;
                String lastMessage = rs.getString("last_message_content");
                if (lastMessage == null) lastMessage = "";

                chats.add(new ChatItemViewModel(
                        (UUID) rs.getObject("chat_id"),
                        rs.getString("display_name"),
                        ChatItemViewModel.ChatType.valueOf(rs.getString("type")),
                        lastMessage,
                        timestamp,
                        rs.getInt("unread_count"),
                        (UUID) rs.getObject("owner_id")
                ));
            }
        }
        return chats;
    }
}