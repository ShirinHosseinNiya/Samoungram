package org.project.server;

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
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void handlePacket(Packet packet) {
        Packet response;
        try {
            switch (packet.getType()) {
                case SIGN_UP:
                    String[] signUpInfo = packet.getContent().split(";", 3);
                    server.registerUser(signUpInfo[0], signUpInfo[1], signUpInfo[2]);
                    response = new Packet(PacketType.SUCCESS);
                    response.setSuccess(true);
                    send(response);
                    break;

                case LOGIN:
                    String[] loginInfo = packet.getContent().split(";", 2);
                    UUID loggedInUserId = server.authenticateUser(loginInfo[0], loginInfo[1]);
                    response = new Packet(PacketType.LOGIN);
                    if (loggedInUserId != null) {
                        this.userId = loggedInUserId;
                        server.addOnlineUser(this.userId, this);
                        response.setSuccess(true);
                        response.setReceiverId(this.userId);
                    } else {
                        response.setSuccess(false);
                        response.setErrorMessage("Invalid credentials.");
                    }
                    send(response);
                    break;

                case FETCH_CHATS:
                    server.sendChatsList(packet);
                    break;

                case FETCH_CHAT_HISTORY:
                    server.sendChatHistory(packet);
                    break;

                case SEND_MESSAGE:
                    server.sendPrivateMessage(packet);
                    break;

                case SEARCH_USER:
                    server.searchAndSendResults(packet);
                    break;

                default:
                    System.out.println("Unknown packet type received: " + packet.getType());
            }
        } catch (SQLException e) {
            response = new Packet(PacketType.ERROR);
            response.setSuccess(false);
            if (e.getMessage().contains("Username already exists.")) {
                response.setErrorMessage("Username is already taken.");
            } else {
                response.setErrorMessage("A database error occurred.");
            }
            send(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = new Packet(PacketType.ERROR);
            response.setSuccess(false);
            response.setErrorMessage("An unexpected server error occurred.");
            send(response);
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