package org.project.server;

import com.google.gson.Gson;
import org.project.client.views.ChatItemViewModel;
import org.project.models.*;
import org.project.server.db.*;

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
    }

    public Connection getConnection() {
        return conn;
    }

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

    public Set<UUID> getOnlineUserIds() {
        return onlineUsers.keySet();
    }

    private void broadcastPacket(Packet packet, UUID skipUserId) {
        for (Map.Entry<UUID, ClientHandler> entry : onlineUsers.entrySet()) {
            if (skipUserId == null || !entry.getKey().equals(skipUserId)) {
                entry.getValue().send(packet);
            }
        }
    }

    public void markMessagesAsRead(UUID readerId, UUID senderId) throws SQLException {
        messageDAO.markMessagesAsRead(readerId, senderId);
    }

    public UUID authenticateUser(String username, String password) throws SQLException { return userDAO.login(username, password); }
    public UUID registerUser(String username, String password, String profileName) throws SQLException { return userDAO.register(username, password, profileName); }

    public void sendPrivateMessage(Packet packet) throws SQLException {
        UUID senderId = packet.getSenderId();
        UUID receiverId = packet.getReceiverId();
        createPrivateChatIfNotExist(senderId, receiverId);
        String senderProfileName = userDAO.getProfileNameById(senderId); // Get sender's profile name
        Message message = new Message(UUID.randomUUID(), senderId, receiverId, packet.getContent(), packet.getTimestamp(), "SENT", senderProfileName);
        messageDAO.addMessage(message);
        ClientHandler receiverHandler = onlineUsers.get(receiverId);
        if (receiverHandler != null) {
            Packet newMsgPacket = new Packet(PacketType.NEW_MESSAGE);
            newMsgPacket.setContent(gson.toJson(message));
            receiverHandler.send(newMsgPacket);
        }
    }

    public void sendGroupOrChannelMessage(Packet packet) throws SQLException {
        String senderProfileName = userDAO.getProfileNameById(packet.getSenderId());
        Message message = new Message(UUID.randomUUID(), packet.getSenderId(), packet.getReceiverId(), packet.getContent(), packet.getTimestamp(), "SENT", senderProfileName);
        messageDAO.addMessage(message);

        List<UUID> memberIds;
        if (groupDAO.findGroupById(packet.getReceiverId()) != null) {
            memberIds = groupDAO.listMembers(packet.getReceiverId());
        } else {
            memberIds = channelDAO.listMembers(packet.getReceiverId());
        }

        Packet newMsgPacket = new Packet(PacketType.NEW_MESSAGE);
        newMsgPacket.setContent(gson.toJson(message));
        for (UUID memberId : memberIds) {
            if (!memberId.equals(packet.getSenderId())) {
                ClientHandler handler = onlineUsers.get(memberId);
                if (handler != null) {
                    handler.send(newMsgPacket);
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
        String[] parts = packet.getContent().split(";", 2);
        UUID chatId = UUID.fromString(parts[0]);
        String usernameToAdd = parts[1];

        UUID userIdToAdd = userDAO.findUserIdByUsername(usernameToAdd);
        if (userIdToAdd == null) {
            System.out.println("User not found: " + usernameToAdd);
            return;
        }

        if (groupDAO.findGroupById(chatId) != null) {
            groupDAO.addMemberToGroup(chatId, userIdToAdd);
        } else if (channelDAO.findChannelById(chatId) != null) {
            channelDAO.addMemberToChannel(chatId, userIdToAdd);
        }
    }

    public void sendMemberList(Packet packet) throws SQLException {
        UUID chatId = packet.getReceiverId();
        List<MemberViewModel> members = new ArrayList<>();

        List<UUID> memberIds;
        UUID creatorId = null;

        if (groupDAO.findGroupById(chatId) != null) {
            memberIds = groupDAO.listMembers(chatId);
            creatorId = groupDAO.getGroupCreatorId(chatId);
        } else {
            memberIds = channelDAO.listMembers(chatId);
            creatorId = channelDAO.getChannelOwnerId(chatId);
        }

        for (UUID memberId : memberIds) {
            String profileName = userDAO.getProfileNameById(memberId);
            if (profileName != null) {
                boolean isCreator = memberId.equals(creatorId);
                members.add(new MemberViewModel(memberId, profileName, isCreator));
            }
        }

        Packet response = new Packet(PacketType.MEMBERS_LIST);
        response.setContent(gson.toJson(members));
        onlineUsers.get(packet.getSenderId()).send(response);
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
            boolean isUser = userDAO.findUserById(receivedId);
            if (isUser) {
                history = messageDAO.getHistoryForPrivateChat(requesterId, receivedId);
            } else {
                history = messageDAO.getHistoryForGroupOrChannel(receivedId);
            }
            // Ensure senderProfileName is populated for messages
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