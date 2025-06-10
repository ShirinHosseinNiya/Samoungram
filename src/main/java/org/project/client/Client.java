package org.project.client;

import org.project.models.Packet;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * کلاس مدیریت اتصال کلاینت به سرور
 */
public class Client {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        new Thread(() -> listen()).start();
    }

    public void send(Packet packet) throws IOException {
        out.writeObject(packet);
    }

    private void listen() {
        try {
            while (true) {
                Packet packet = (Packet) in.readObject();
                // نمایش پیام دریافتی در UI یا کنسول
                System.out.println("دریافت شد: " + packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
