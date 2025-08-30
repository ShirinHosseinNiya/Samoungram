package org.project.models;

import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String hashedPassword;
    private String profileName;
    private String status;
    private String bio;
    private String profilePicture;

    // 🔹 سازنده‌ی کامل (برای بارگذاری از دیتابیس)
    public User(UUID id, String username, String hashedPassword, String profileName, String status) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.profileName = profileName;
        this.status = status;
    }

    // 🔹 سازنده‌ی ساده با رمز خام (برای ثبت‌نام جدید)
    public User(String username, String rawPassword, String profileName) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hashedPassword = hashPassword(rawPassword);
        this.profileName = profileName;
        this.status = "online";
        this.bio = "";
        this.profilePicture = "";
    }

    // متد هش کردن پسورد
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // ---------- Getter ها ----------
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

    public String getBio() {
        return bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    // ---------- Setter ها ----------
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    // ---------- متدهای کمکی ----------
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", profileName='" + profileName + '\'' +
                ", status='" + status + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return this.id.equals(other.id);
    }
}

