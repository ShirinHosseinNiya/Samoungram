package org.project.models;

import java.io.Serializable;

/**
 * بسته‌ی اطلاعاتی برای تبادل بین کلاینت و سرور
 */
public class Packet implements Serializable {
    private String type;   // نوع عملیات: مثلا LOGIN, MESSAGE, REGISTER, etc
    private Object data;   // داده‌ی ارسال‌شده: مثلا یک Message یا User یا String

    public Packet(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}
