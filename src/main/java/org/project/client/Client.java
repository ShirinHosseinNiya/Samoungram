package org.project.client;

import org.project.models.Packet;
import org.project.models.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        new Thread(this::listen).start();
    }

    public void send(Packet packet) throws IOException {
        out.writeObject(packet);
    }

    private void listen() {
        try {
            while (true) {
                Packet packet = (Packet) in.readObject();
                switch (packet.getType()) {
                    case RECEIVE_MESSAGE:
                        System.out.println("📩 New message: " + packet.getData());
                        break;
                    case LOGIN:
                        System.out.println("🔐 Login result: " + packet.getData());
                        break;
                    default:
                        System.out.println("📦 Received: " + packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sample login method (can be called from UI)
    public void login(String username, String password) throws IOException {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        send(new Packet(PacketType.LOGIN, data));
    }
}

