package org.project.server.db;

import org.project.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.UUID;

public class UserDAO {

    public boolean login(String username, String rawPassword) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return BCrypt.checkpw(rawPassword, storedHash);
            }

        } catch (SQLException e) {
            System.out.println("❌ Login error: " + e.getMessage());
        }

        return false;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, profile_name, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getHashedPassword());
            stmt.setString(4, user.getProfileName());
            stmt.setString(5, user.getStatus());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Register error: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("❌ Username check error: " + e.getMessage());
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("profile_name"),
                        rs.getString("status")
                );
            }

        } catch (SQLException e) {
            System.out.println("❌ Fetch user error: " + e.getMessage());
        }

        return null;
    }
}

