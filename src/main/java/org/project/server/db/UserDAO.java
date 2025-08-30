package org.project.server.db;

import org.project.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.UUID;

public class UserDAO {

    // ✅ ثبت‌نام کاربر جدید
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, profile_name, bio, profile_picture, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getHashedPassword());
            stmt.setString(4, user.getProfileName());
            stmt.setString(5, user.getBio());
            stmt.setString(6, user.getProfilePicture());
            stmt.setString(7, user.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ ورود کاربر
    public User login(String username, String rawPassword) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(rawPassword, storedHash)) {
                    User user = new User(
                            rs.getObject("id", UUID.class),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("profile_name"),
                            rs.getString("status")
                    );
                    user.setBio(rs.getString("bio"));
                    user.setProfilePicture(rs.getString("profile_picture"));

                    // بروزرسانی وضعیت به آنلاین
                    updateStatus(user.getId(), "online");

                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // ورود ناموفق
    }

    // ✅ چک کردن تکراری بودن یوزرنیم
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ تغییر پروفایل کاربر
    public boolean updateProfile(UUID userId, String profileName, String bio, String picture, String status) {
        String sql = "UPDATE users SET profile_name=?, bio=?, profile_picture=?, status=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profileName);
            stmt.setString(2, bio);
            stmt.setString(3, picture);
            stmt.setString(4, status);
            stmt.setObject(5, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ تغییر وضعیت کاربر (Online/Offline)
    public boolean updateStatus(UUID userId, String status) {
        String sql = "UPDATE users SET status=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setObject(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ خروج کاربر (logout)
    public boolean logout(UUID userId) {
        return updateStatus(userId, "offline");
    }
}


