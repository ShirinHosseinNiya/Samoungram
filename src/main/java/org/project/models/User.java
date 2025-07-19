package org.project.models;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String hashedPassword;
    private String profileName;
    private String status;

    public User(String username, String rawPassword, String profileName, String status) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(rawPassword);
        this.profileName = profileName;
        this.status = status;
    }

    private String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
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

    public UUID getId() {
        return id;
    }
}
