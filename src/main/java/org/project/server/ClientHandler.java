package org.project.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import org.project.models.Packet;
import org.project.models.PacketType;
import org.project.models.Message;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void run() {
        try {
            while (true) {
                Packet packet = (Packet) in.readObject();
                handle(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle(Packet packet) {
        switch (packet.getType()) {
            case SEND_MESSAGE:
                Message msg = (Message) packet.getData();
                Server.broadcast(new Packet(PacketType.RECEIVE_MESSAGE, msg), this);
                break;

            case LOGIN:
                Map<String, String> loginData = (Map<String, String>) packet.getData();
                String username = loginData.get("username");
                String password = loginData.get("password");

                boolean loginSuccess = username.equals("admin") && password.equals("1234");
                Packet response = new Packet(PacketType.LOGIN, loginSuccess ? "success" : "fail");
                send(response);
                break;

            default:
                System.out.println("Unhandled packet type: " + packet.getType());
        }
    }

    public void send(Packet packet) {
        try {
            out.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}