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
                "(SELECT " +
                        "    other_user.id AS chat_id, " +
                        "    other_user.profile_name AS display_name, " +
                        "    'PRIVATE' AS type, " +
                        "    mh.content AS last_message_content, " +
                        "    mh.timestamp AS last_message_timestamp, " +
                        "    (SELECT COUNT(*) FROM message_history WHERE receiver_is = ? AND sender_id = other_user.id AND status <> 'READ') AS unread_count " +
                        "FROM private_chats pc " +
                        "JOIN users other_user ON other_user.id = CASE WHEN pc.participate1_id = ? THEN pc.participate2_id ELSE pc.participate1_id END " +
                        "LEFT JOIN LATERAL ( " +
                        "    SELECT content, \"timestamp\" FROM message_history " +
                        "    WHERE (sender_id = pc.participate1_id AND receiver_is = pc.participate2_id) OR (sender_id = pc.participate2_id AND receiver_is = pc.participate1_id) " +
                        "    ORDER BY \"timestamp\" DESC LIMIT 1 " +
                        ") mh ON true " +
                        "WHERE pc.participate1_id = ? OR pc.participate2_id = ?) " +

                        "ORDER BY last_message_timestamp DESC NULLS LAST";


        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setObject(2, userId);
            ps.setObject(3, userId);
            ps.setObject(4, userId);

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
                        rs.getInt("unread_count")
                ));
            }
        }
        return chats;
    }
}