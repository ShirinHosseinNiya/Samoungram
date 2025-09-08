package org.project.server;

import com.google.gson.Gson;
import org.project.client.views.ChatItemViewModel;
import org.project.models.Channel;
import org.project.models.Group;
import org.project.models.MemberViewModel;
import org.project.models.Message;
import org.project.models.Packet;
import org.project.models.PacketType;
import org.project.models.User;
import org.project.server.db.*;
import org.project.util.PasswordUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Map<UUID, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final PrivateChatDAO privateChatDAO;
    private final ChatDAO chatDAO;
    private final GroupDAO groupDAO;
    private final ChannelDAO channelDAO;
    private final ReadMarkerDAO readMarkerDAO;
    private final Connection conn;
    private final Gson gson = new Gson();

    public Server(Connection conn) {
        this.conn = conn;
        this.userDAO = new UserDAO(conn);
        this.messageDAO = new MessageDAO(conn);
        this.privateChatDAO = new PrivateChatDAO(conn);
        this.chatDAO = new ChatDAO(conn);
        this.groupDAO = new GroupDAO(conn);
        this.channelDAO = new ChannelDAO(conn);
        this.readMarkerDAO = new ReadMarkerDAO(conn);
    }

    public Connection getConnection() { return conn; }

    public void addOnlineUser(UUID userId, ClientHandler handler) {
        onlineUsers.put(userId, handler);
        Packet onlinePacket = new Packet(PacketType.USER_ONLINE);
        onlinePacket.setContent(userId.toString());
        broadcastPacket(onlinePacket, userId);
    }

    public void removeOnlineUser(UUID userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
            Packet offlinePacket = new Packet(PacketType.USER_OFFLINE);
            offlinePacket.setContent(userId.toString());
            broadcastPacket(offlinePacket, null);
        }
    }

    public Set<UUID> getOnlineUserIds() { return onlineUsers.keySet(); }

    private void broadcastPacket(Packet packet, UUID skipUserId) {
        for (Map.Entry<UUID, ClientHandler> entry : onlineUsers.entrySet()) {
            if (skipUserId == null || !entry.getKey().equals(skipUserId)) {
                entry.getValue().send(packet);
            }
        }
    }

    public void markMessagesAsRead(UUID readerId, UUID chatId) throws SQLException {
        User user = userDAO.findUserById(chatId);
        if (user != null) {
            messageDAO.markMessagesAsRead(readerId, chatId);
        } else {
            readMarkerDAO.updateLastReadTimestamp(readerId, chatId);
        }
    }

    public void handleUpdateProfile(Packet packet) throws SQLException {
        String[] parts = packet.getContent().split(";", 3);
        userDAO.updateUserProfile(packet.getSenderId(), parts[0], parts[1], parts[2]);
        Packet response = new Packet(PacketType.SUCCESS);
        response.setContent("Profile updated successfully.");
        onlineUsers.get(packet.getSenderId()).send(response);
    }

    public void handleChangePassword(Packet packet) throws SQLException {
        String[] parts = packet.getContent().split(";", 2);
        String currentPassword = parts[0];
        String newPassword = parts[1];
        User user = userDAO.findUserById(packet.getSenderId());
        Packet response = new Packet(PacketType.CHANGE_PASSWORD);
        if (user != null && PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            String newHash = PasswordUtil.hashPassword(newPassword);
            userDAO.updateUserPassword(user.getId(), newHash);
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setErrorMessage("Current password is incorrect.");
        }
        onlineUsers.get(packet.getSenderId()).send(response);
    }

    public void sendProfileDetails(Packet packet) throws SQLException {
        UUID profileOwnerId = packet.getReceiverId();
        User user = userDAO.findUserById(profileOwnerId);
        if (user != null) {
            Packet response = new Packet(PacketType.PROFILE_DETAILS);
            response.setSenderId(profileOwnerId); // خط اصلاح شده
            response.setContent(gson.toJson(user));
            onlineUsers.get(packet.getSenderId()).send(response);
        }
    }

    public UUID authenticateUser(String username, String password) throws SQLException { return userDAO.login(username, password); }
    public UUID registerUser(String username, String password, String profileName) throws SQLException { return userDAO.register(username, password, profileName); }

    public void sendPrivateMessage(Packet packet) throws SQLException {
        UUID senderId = packet.getSenderId();
        UUID receiverId = packet.getReceiverId();
        createPrivateChatIfNotExist(senderId, receiverId);
        String senderProfileName = userDAO.getProfileNameById(senderId);
        Message message = new Message(UUID.randomUUID(), senderId, receiverId, packet.getContent(), packet.getTimestamp(), "SENT", senderProfileName);
        messageDAO.addMessage(message);

        ClientHandler receiverHandler = onlineUsers.get(receiverId);
        if (receiverHandler != null) {
            Packet newMsgPacket = new Packet(PacketType.NEW_MESSAGE);
            newMsgPacket.setContent(gson.toJson(message));
            receiverHandler.send(newMsgPacket);
            Packet refreshPacket = new Packet(PacketType.FETCH_CHATS);
            refreshPacket.setSenderId(receiverId);
            sendChatsList(refreshPacket);
        }
    }

    public void sendGroupOrChannelMessage(Packet packet) throws SQLException {
        UUID chatId = packet.getReceiverId();
        UUID senderId = packet.getSenderId();
        Channel channel = channelDAO.findChannelById(chatId);
        if (channel != null && !channel.getChannelOwnerId().equals(senderId)) return;

        String senderProfileName = userDAO.getProfileNameById(senderId);
        Message message = new Message(UUID.randomUUID(), senderId, chatId, packet.getContent(), packet.getTimestamp(), "SENT", senderProfileName);
        messageDAO.addMessage(message);

        List<UUID> memberIds = (channel != null) ? channelDAO.listMembers(chatId) : groupDAO.listMembers(chatId);

        Packet newMsgPacket = new Packet(PacketType.NEW_MESSAGE);
        newMsgPacket.setContent(gson.toJson(message));

        for (UUID memberId : memberIds) {
            if (!memberId.equals(senderId)) {
                ClientHandler handler = onlineUsers.get(memberId);
                if (handler != null) {
                    handler.send(newMsgPacket);
                    Packet refreshPacket = new Packet(PacketType.FETCH_CHATS);
                    refreshPacket.setSenderId(memberId);
                    sendChatsList(refreshPacket);
                }
            }
        }
    }

    private void createPrivateChatIfNotExist(UUID user1, UUID user2) throws SQLException {
        if (!privateChatDAO.privateChatExists(user1, user2)) {
            privateChatDAO.createPrivateChat(user1, user2);
        }
    }

    public void createGroup(UUID creatorId, String groupName) throws SQLException {
        UUID groupId = UUID.randomUUID();
        groupDAO.addGroup(groupId, groupName, creatorId);
        groupDAO.addMemberToGroup(groupId, creatorId);
    }

    public void createChannel(UUID creatorId, String channelName) throws SQLException {
        UUID channelId = UUID.randomUUID();
        channelDAO.addChannel(channelId, channelName, creatorId);
        channelDAO.addMemberToChannel(channelId, creatorId);
    }

    public void addMemberToChat(Packet packet) throws SQLException {
        UUID requesterId = packet.getSenderId();
        String[] parts = packet.getContent().split(";", 2);
        UUID chatId = UUID.fromString(parts[0]);
        String usernameOrId = parts[1];

        Channel channel = channelDAO.findChannelById(chatId);
        if (channel != null && !channel.getChannelOwnerId().equals(requesterId)) return;

        UUID userIdToAdd;
        try {
            userIdToAdd = UUID.fromString(usernameOrId);
        } catch (IllegalArgumentException e) {
            userIdToAdd = userDAO.findUserIdByUsername(usernameOrId);
        }

        if (userIdToAdd == null) return;

        if (channel != null) {
            channelDAO.addMemberToChannel(chatId, userIdToAdd);
        } else if (groupDAO.findGroupById(chatId) != null) {
            groupDAO.addMemberToGroup(chatId, userIdToAdd);
        }

        ClientHandler newlyAddedMemberHandler = onlineUsers.get(userIdToAdd);
        if (newlyAddedMemberHandler != null) {
            Packet refreshPacket = new Packet(PacketType.FETCH_CHATS);
            refreshPacket.setSenderId(userIdToAdd);
            sendChatsList(refreshPacket);
        }
    }

    public void sendMemberList(Packet packet) throws SQLException {
        UUID chatId = packet.getReceiverId();
        UUID requesterId = packet.getSenderId();
        Channel channel = channelDAO.findChannelById(chatId);
        if (channel != null && !channel.getChannelOwnerId().equals(requesterId)) return;

        List<MemberViewModel> members = new ArrayList<>();
        List<UUID> memberIds;
        UUID creatorId = null;

        if (channel != null) {
            memberIds = channelDAO.listMembers(chatId);
            creatorId = channel.getChannelOwnerId();
        } else {
            memberIds = groupDAO.listMembers(chatId);
            creatorId = groupDAO.getGroupCreatorId(chatId);
        }

        for (UUID memberId : memberIds) {
            String profileName = userDAO.getProfileNameById(memberId);
            if (profileName != null) {
                members.add(new MemberViewModel(memberId, profileName, memberId.equals(creatorId)));
            }
        }

        Packet response = new Packet(PacketType.MEMBERS_LIST);
        response.setContent(gson.toJson(members));
        onlineUsers.get(requesterId).send(response);
    }

    public void kickMember(Packet packet) throws SQLException {
        UUID kickerId = packet.getSenderId();
        String[] parts = packet.getContent().split(";", 2);
        UUID chatId = UUID.fromString(parts[0]);
        UUID memberToKickId = UUID.fromString(parts[1]);

        UUID ownerId = null;
        Channel channel = channelDAO.findChannelById(chatId);
        if (channel != null) {
            ownerId = channel.getChannelOwnerId();
        } else {
            ownerId = groupDAO.getGroupCreatorId(chatId);
        }

        if (ownerId != null && ownerId.equals(kickerId) && !kickerId.equals(memberToKickId)) {
            if (channel != null) {
                channelDAO.removeMemberFromChannel(chatId, memberToKickId);
            } else {
                groupDAO.removeMemberFromGroup(chatId, memberToKickId);
            }
        }
    }

    public void leaveChat(Packet packet) throws SQLException {
        UUID memberId = packet.getSenderId();
        UUID chatId = UUID.fromString(packet.getContent());
        if (groupDAO.findGroupById(chatId) != null) {
            groupDAO.removeMemberFromGroup(chatId, memberId);
        } else if (channelDAO.findChannelById(chatId) != null) {
            channelDAO.removeMemberFromChannel(chatId, memberId);
        }
    }

    public void renameChat(Packet packet) throws SQLException {
        UUID requesterId = packet.getSenderId();
        String[] parts = packet.getContent().split(";", 2);
        UUID chatId = UUID.fromString(parts[0]);
        String newName = parts[1];
        List<UUID> membersToNotify = new ArrayList<>();

        Channel channel = channelDAO.findChannelById(chatId);
        if (channel != null) {
            if (channel.getChannelOwnerId().equals(requesterId)) {
                channelDAO.updateChannelName(chatId, newName);
                membersToNotify = channelDAO.listMembers(chatId);
            }
        } else {
            Group group = groupDAO.findGroupById(chatId);
            if (group != null && group.getGroupCreatorId().equals(requesterId)) {
                groupDAO.updateGroupName(chatId, newName);
                membersToNotify = groupDAO.listMembers(chatId);
            }
        }

        for (UUID memberId : membersToNotify) {
            ClientHandler handler = onlineUsers.get(memberId);
            if (handler != null) {
                Packet refreshPacket = new Packet(PacketType.FETCH_CHATS);
                refreshPacket.setSenderId(memberId);
                sendChatsList(refreshPacket);
            }
        }
    }

    public void searchAndSendChannels(Packet packet) {
        try {
            List<Channel> channels = channelDAO.searchChannelsByName(packet.getContent());
            String jsonResults = gson.toJson(channels);
            Packet response = new Packet(PacketType.SEARCH_RESULTS);
            response.setContent(jsonResults);
            onlineUsers.get(packet.getSenderId()).send(response);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void searchAndSendResults(Packet packet) {
        try {
            List<User> users = userDAO.searchUsers(packet.getContent());
            String jsonResults = gson.toJson(users);
            Packet response = new Packet(PacketType.SEARCH_RESULTS);
            response.setContent(jsonResults);
            ClientHandler senderHandler = onlineUsers.get(packet.getSenderId());
            if (senderHandler != null) { senderHandler.send(response); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void sendChatsList(Packet packet) {
        try {
            List<ChatItemViewModel> chats = chatDAO.getAllChatsForUser(packet.getSenderId());
            String jsonResponse = gson.toJson(chats);
            Packet response = new Packet(PacketType.CHATS_LIST);
            response.setContent(jsonResponse);
            onlineUsers.get(packet.getSenderId()).send(response);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void sendChatHistory(Packet packet) {
        try {
            UUID receivedId = packet.getReceiverId();
            UUID requesterId = packet.getSenderId();
            List<Message> history;
            User user = userDAO.findUserById(receivedId);
            if (user != null) {
                history = messageDAO.getHistoryForPrivateChat(requesterId, receivedId);
            } else {
                history = messageDAO.getHistoryForGroupOrChannel(receivedId);
            }
            for (Message msg : history) {
                if (msg.getSenderProfileName() == null) {
                    msg.setSenderProfileName(userDAO.getProfileNameById(msg.getSenderId()));
                }
            }
            String jsonResponse = gson.toJson(history);
            Packet response = new Packet(PacketType.MESSAGES_LIST);
            response.setContent(jsonResponse);
            onlineUsers.get(requesterId).send(response);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            Server server = new Server(conn);
            int port = 12345;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, server);
                new Thread(handler).start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}