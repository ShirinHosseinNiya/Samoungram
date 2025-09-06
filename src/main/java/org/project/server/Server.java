package org.project.server;

import org.project.models.Message;
import org.project.models.Packet;
import org.project.models.PacketType;
import org.project.server.db.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Map<UUID, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final GroupDAO groupDAO;
    private final ChannelDAO channelDAO;
    private final PrivateChatDAO privateChatDAO;


    public Server(Connection conn) {
        this.userDAO = new UserDAO(conn);
        this.messageDAO = new MessageDAO(conn);
        this.groupDAO = new GroupDAO(conn);
        this.channelDAO = new ChannelDAO(conn);
        this.privateChatDAO = new PrivateChatDAO(conn);
    }

    public UUID authenticateUser(String username, String password) throws SQLException {
        return userDAO.login(username, password);
    }

    public UUID registerUser(String username, String password, String profileName) throws SQLException {
        return userDAO.register(username, password, profileName);
    }

    public void addOnlineUser(UUID userId, ClientHandler handler) {
        onlineUsers.put(userId, handler);
        System.out.println("User online: " + userId + " | Total online: " + onlineUsers.size());
    }

    public void removeOnlineUser(UUID userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
            System.out.println("User offline: " + userId + " | Total online: " + onlineUsers.size());
        }
    }

    public void sendPrivateMessage(Packet packet) throws SQLException {
        Message message = new Message(UUID.randomUUID(), packet.getSenderId(), packet.getReceiverId(), packet.getContent(), packet.getTimestamp(), "SENT");
        messageDAO.addMessage(message);

        ClientHandler receiverHandler = onlineUsers.get(packet.getReceiverId());
        if (receiverHandler != null) {
            receiverHandler.send(packet);
        }

        ClientHandler senderHandler = onlineUsers.get(packet.getSenderId());
        if (senderHandler != null) {
            senderHandler.send(packet);
        }
    }

    public void broadcastToGroup(UUID groupId, Packet packet) throws SQLException {
        Message message = new Message(UUID.randomUUID(), packet.getSenderId(), groupId, packet.getContent(), packet.getTimestamp(), "SENT");
        messageDAO.addMessage(message);

        for (UUID memberId : groupDAO.listMembers(groupId)) {
            ClientHandler handler = onlineUsers.get(memberId);
            if (handler != null) {
                handler.send(packet);
            }
        }
    }

    public void broadcastToChannel(UUID channelId, Packet packet) throws SQLException {
        Message message = new Message(UUID.randomUUID(), packet.getSenderId(), channelId, packet.getContent(), packet.getTimestamp(), "SENT");
        messageDAO.addMessage(message);

        for (UUID memberId : channelDAO.listMembers(channelId)) {
            ClientHandler handler = onlineUsers.get(memberId);
            if (handler != null) {
                handler.send(packet);
            }
        }
    }

    public UUID createGroup(String name, UUID creatorId) throws SQLException {
        UUID groupId = UUID.randomUUID();
        groupDAO.addGroup(groupId, name, creatorId);
        groupDAO.addMemberToGroup(groupId, creatorId);
        return groupId;
    }

    public void addMemberToGroup(UUID groupId, UUID memberId) throws SQLException {
        groupDAO.addMemberToGroup(groupId, memberId);
    }

    public UUID createChannel(String name, UUID ownerId) throws SQLException {
        UUID channelId = UUID.randomUUID();
        channelDAO.addChannel(channelId, name, ownerId);
        channelDAO.addMemberToChannel(channelId, ownerId);
        return channelId;
    }

    public void addMemberToChannel(UUID channelId, UUID memberId) throws SQLException {
        channelDAO.addMemberToChannel(channelId, memberId);
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
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, server);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}