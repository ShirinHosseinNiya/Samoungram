package org.project.models;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String hashedPassword;
    private String profileName;
    private String status;

    // سازنده‌ی کامل
    public User(UUID id, String username, String hashedPassword, String profileName, String status) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.profileName = profileName;
        this.status = status;
    }

    // سازنده‌ی ساده با رمز خام (برای ثبت‌نام جدید)
    public User(String username, String rawPassword, String profileName) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(rawPassword);
        this.profileName = profileName;
        this.status = "online";
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Getterها
    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getStatus() {
        return status;
    }
}

