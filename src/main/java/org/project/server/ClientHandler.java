package org.project.server;

import com.google.gson.Gson;
import org.project.models.Packet;
import org.project.models.PacketType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private UUID userId;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Packet packet = (Packet) in.readObject();
                handlePacket(packet);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            server.removeOnlineUser(this.userId);
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void handlePacket(Packet packet) {
        try {
            switch (packet.getType()) {
                case LOGIN:
                    String[] loginInfo = packet.getContent().split(";", 2);
                    UUID loggedInUserId = server.authenticateUser(loginInfo[0], loginInfo[1]);
                    if (loggedInUserId != null) {
                        this.userId = loggedInUserId;
                        server.addOnlineUser(this.userId, this);
                        Packet response = new Packet(PacketType.LOGIN);
                        response.setSuccess(true);
                        response.setReceiverId(this.userId);
                        send(response);
                        Packet onlineListPacket = new Packet(PacketType.ONLINE_USERS_LIST);
                        onlineListPacket.setContent(gson.toJson(server.getOnlineUserIds()));
                        send(onlineListPacket);
                    } else {
                        Packet response = new Packet(PacketType.LOGIN);
                        response.setSuccess(false);
                        response.setErrorMessage("Invalid credentials.");
                        send(response);
                    }
                    break;
                case SEND_MESSAGE:
                    server.sendPrivateMessage(packet);
                    break;
                case FETCH_CHATS:
                    server.sendChatsList(packet);
                    break;
                case FETCH_CHAT_HISTORY:
                    server.sendChatHistory(packet);
                    break;
                case MARK_AS_READ:
                    server.markMessagesAsRead(packet.getSenderId(), packet.getReceiverId());
                    break;
                case SEARCH_USER:
                    server.searchAndSendResults(packet);
                    break;
                default:
                    System.out.println("Unknown packet type received: " + packet.getType());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send packet to " + (userId != null ? userId : socket.getInetAddress()));
        }
    }
}