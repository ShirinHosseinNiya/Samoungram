package org.project.server.db;

import org.project.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public UUID register(String username, String rawPassword, String profileName) throws SQLException {
        if (username == null || rawPassword == null || profileName == null) {
            return null;
        }
        if (findUserIdByUsername(username) != null) {
            throw new SQLException("Username already exists.");
        }
        UUID id = UUID.randomUUID();
        String hash = PasswordUtil.hashPassword(rawPassword);
        String sql = "INSERT INTO users (id, username, password_hash, profile_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, profileName);
            ps.executeUpdate();
        }
        return id;
    }

    public UUID login(String username, String rawPassword) throws SQLException {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UUID id = (UUID) rs.getObject("id");
                String hash = rs.getString("password_hash");
                return PasswordUtil.checkPassword(rawPassword, hash) ? id : null;
            }
        }
    }

    public UUID findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return (UUID) rs.getObject(1);
                return null;
            }
        }
    }

    public boolean updateProfile(UUID userId, String profileName, String status, String bio, String profilePicture) throws SQLException {
        String sql = "UPDATE users SET profile_name = ?, status = ?, bio = ?, profile_picture = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profileName);
            ps.setString(2, status);
            ps.setString(3, bio);
            ps.setString(4, profilePicture);
            ps.setObject(5, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean changePassword(UUID userId, String oldRawPassword, String newRawPassword) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String currentHash = rs.getString(1);
                if (!PasswordUtil.checkPassword(oldRawPassword, currentHash)) return false;
            }
        }
        String newHash = PasswordUtil.hashPassword(newRawPassword);
        String upd = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, newHash);
            ps.setObject(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public String getProfile(UUID userId) throws SQLException {
        String sql = "SELECT profile_name, status, bio, profile_picture FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Name: " + rs.getString("profile_name") +
                            "\nStatus: " + rs.getString("status") +
                            "\nBio: " + rs.getString("bio") +
                            "\nPicture: " + rs.getString("profile_picture");
                }
                return null;
            }
        }
    }
}