package org.project.server;

import com.google.gson.Gson;
import org.project.client.views.ChatItemViewModel;
import org.project.models.Message;
import org.project.models.Packet;
import org.project.models.PacketType;
import org.project.models.User;
import org.project.models.PrivateChat;
import org.project.server.db.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
    private final ChatDAO chatDAO;
    private final Gson gson = new Gson();

    public Server(Connection conn) {
        this.userDAO = new UserDAO(conn);
        this.messageDAO = new MessageDAO(conn);
        this.groupDAO = new GroupDAO(conn);
        this.channelDAO = new ChannelDAO(conn);
        this.privateChatDAO = new PrivateChatDAO(conn);
        this.chatDAO = new ChatDAO(conn);
    }

    public UUID authenticateUser(String username, String password) throws SQLException {
        return userDAO.login(username, password);
    }

    public UUID registerUser(String username, String password, String profileName) throws SQLException {
        return userDAO.register(username, password, profileName);
    }

    public void addOnlineUser(UUID userId, ClientHandler handler) {
        onlineUsers.put(userId, handler);
    }

    public void removeOnlineUser(UUID userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }

    public void sendPrivateMessage(Packet packet) throws SQLException {
        UUID senderId = packet.getSenderId();
        UUID receiverId = packet.getReceiverId();

        createPrivateChatIfNotExist(senderId, receiverId);

        Message message = new Message(
                UUID.randomUUID(),
                senderId,
                receiverId,
                packet.getContent(),
                packet.getTimestamp(),
                "SENT"
        );
        messageDAO.addMessage(message);

        ClientHandler receiverHandler = onlineUsers.get(receiverId);
        if (receiverHandler != null) {
            Packet newMsgPacket = new Packet(PacketType.NEW_MESSAGE);
            newMsgPacket.setContent(gson.toJson(message));
            receiverHandler.send(newMsgPacket);
        }
    }

    private void createPrivateChatIfNotExist(UUID user1, UUID user2) throws SQLException {
        if (!privateChatDAO.privateChatExists(user1, user2)) {
            privateChatDAO.createPrivateChat(user1, user2);
        }
    }

    public void searchAndSendResults(Packet packet) {
        try {
            List<User> users = userDAO.searchUsers(packet.getContent());
            String jsonResults = gson.toJson(users);
            Packet response = new Packet(PacketType.SEARCH_RESULTS);
            response.setContent(jsonResults);
            ClientHandler senderHandler = onlineUsers.get(packet.getSenderId());
            if (senderHandler != null) {
                senderHandler.send(response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendChatsList(Packet packet) {
        try {
            List<ChatItemViewModel> chats = chatDAO.getAllChatsForUser(packet.getSenderId());
            String jsonResponse = gson.toJson(chats);
            Packet response = new Packet(PacketType.CHATS_LIST);
            response.setContent(jsonResponse);
            onlineUsers.get(packet.getSenderId()).send(response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendChatHistory(Packet packet) {
        try {
            UUID receivedId = packet.getReceiverId(); // This can be a userId, groupId, or channelId
            UUID requesterId = packet.getSenderId();
            List<Message> history;

            boolean isUser = userDAO.findUserById(receivedId);
            if (isUser) {
                history = messageDAO.getHistoryForPrivateChat(requesterId, receivedId);
            } else {
                history = messageDAO.getHistoryForGroupOrChannel(receivedId);
            }

            String jsonResponse = gson.toJson(history);
            Packet response = new Packet(PacketType.MESSAGES_LIST);
            response.setContent(jsonResponse);
            onlineUsers.get(requesterId).send(response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}