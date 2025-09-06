package org.project.models;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private String profileName;
    private String status;
    private String bio;
    private String profilePicture;

    public User(UUID id, String username, String passwordHash, String profileName, String status, String bio, String profilePicture) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.profileName = profileName;
        this.status = status;
        this.bio = bio;
        this.profilePicture = profilePicture;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getStatus() {
        return status;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
