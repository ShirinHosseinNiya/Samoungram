package org.project.client;

import org.project.models.Packet;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkClient {
    private final String host = "localhost";
    private final int port = 12345;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final BlockingQueue<Packet> receivedPackets = new LinkedBlockingQueue<>();

    public NetworkClient() {
    }

    public void connect() throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        Thread listenerThread = new Thread(this::listenToServer);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenToServer() {
        try {
            while (!socket.isClosed()) {
                Packet packetFromServer = (Packet) in.readObject();
                receivedPackets.put(packetFromServer);
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }

    public void sendPacket(Packet packet) {
        try {
            if (out != null) {
                out.writeObject(packet);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Packet getReceivedPacket() throws InterruptedException {
        return receivedPackets.take();
    }

    public Packet pollReceivedPacket() {
        return receivedPackets.poll();
    }
}