package org.project.server.db;

import org.project.models.User;
import org.project.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public void updateUserProfile(UUID userId, String profileName, String bio, String profilePicture) throws SQLException {
        String sql = "UPDATE users SET profile_name = ?, bio = ?, profile_picture = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profileName);
            ps.setString(2, bio);
            ps.setString(3, profilePicture);
            ps.setObject(4, userId);
            ps.executeUpdate();
        }
    }

    public void updateUserPassword(UUID userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setObject(2, userId);
            ps.executeUpdate();
        }
    }

    public User findUserById(UUID userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            (UUID) rs.getObject("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("profile_name"),
                            rs.getString("status"),
                            rs.getString("bio"),
                            rs.getString("profile_picture")
                    );
                }
            }
        }
        return null;
    }

    public String getProfileNameById(UUID userId) throws SQLException {
        String sql = "SELECT profile_name FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("profile_name");
                }
                return null;
            }
        }
    }

    public List<User> searchUsers(String query) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, profile_name, status, bio, profile_picture FROM users WHERE username ILIKE ? OR profile_name ILIKE ? OR id::text ILIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String likeQuery = "%" + query + "%";
            ps.setString(1, likeQuery);
            ps.setString(2, likeQuery);
            ps.setString(3, likeQuery);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            (UUID) rs.getObject("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("profile_name"),
                            rs.getString("status"),
                            rs.getString("bio"),
                            rs.getString("profile_picture")
                    ));
                }
            }
        }
        return users;
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
}