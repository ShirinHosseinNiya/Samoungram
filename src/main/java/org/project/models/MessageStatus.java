package org.project.models;

public enum MessageStatus {
    SENT,
    DELIVERED,
    READ;

    public static MessageStatus fromDb(String s) {
        if (s == null) return SENT;
        try { return MessageStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return SENT; }
    }

    public String toDb() {
        return name();
    }
}
