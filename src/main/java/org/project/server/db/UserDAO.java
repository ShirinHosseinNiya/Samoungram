package org.project.server.db;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import org.project.models.User;

public class UserDAO {

    public static boolean registerUser(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password, profile_name, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId().toString());             // UUID → String
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getHashedPassword());
            stmt.setString(4, user.getProfileName());
            stmt.setString(5, user.getStatus());
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean validateUser(Connection conn, String username, String rawPassword) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    return BCrypt.checkpw(rawPassword, hashedPassword);
                }
            }
        }
        return false;
    }

    public static String getUserIdByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        }
        return null;
    }
}
