package org.project.server;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.project.Message;
import org.project.models.Packet;

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
            case "SEND_MESSAGE":
                Message msg = (Message) packet.getData();
                // ذخیره در دیتابیس و ارسال به کلاینت مقصد
                Server.broadcast(new Packet("RECEIVE_MESSAGE", msg), this);
                break;
            // موارد دیگر مثل LOGIN، REGISTER، GROUP_CREATE و ...
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
