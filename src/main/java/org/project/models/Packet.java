package org.project.models;

import java.io.Serializable;

public class Packet implements Serializable {
    private PacketType type;
    private Object data;

    public Packet(PacketType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}

