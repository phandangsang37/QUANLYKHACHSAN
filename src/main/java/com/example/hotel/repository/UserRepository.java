package com.example.hotel.repository;

import com.example.hotel.database.Database;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public static boolean authenticate(
            String username,
            String password) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return false;
        }

        String sql = """
                SELECT password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                String storedHash = rs.getString("password_hash");

                return storedHash != null
                        && BCrypt.checkpw(password, storedHash);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra tài khoản trong MariaDB", e);
        }
    }

    public static String authenticateAndGetRole(
            String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        String sql = """
                SELECT password_hash, role
                FROM users
                WHERE username = ?
                  AND enabled = 1
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String hash = rs.getString("password_hash");

                if (hash != null
                        && BCrypt.checkpw(password, hash)) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xác thực tài khoản", e);
        }

        return null;
    }
}