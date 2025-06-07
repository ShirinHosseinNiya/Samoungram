package org.project;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class User {
    private UUID id;                        // internal unique id
    private String username;                // chosen by user, must be unique
    private String hashedPassword;
    private String profileName;
    private String status;
    //private String profileImageUrl;       // optional
    //private String bio;                   // optional


    //Constructor
    public User(String username, String rawPassword, String profileName, String status) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(rawPassword);
        this.profileName = profileName;
        this.status = status;
    }

    private String hashPassword (String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}