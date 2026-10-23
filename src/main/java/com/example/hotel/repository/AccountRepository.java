package com.example.hotel.repository;

import com.example.hotel.database.Database;
import com.example.hotel.model.UserAccount;
import com.example.hotel.model.UserSession;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private static void requireAdmin() {
        if (!UserSession.isAdmin()) {
            throw new SecurityException(
                    "Chỉ Admin được quản lý tài khoản.");
        }
    }

    private static void validateRole(String role) {
        if (!"admin".equals(role) && !"staff".equals(role)) {
            throw new IllegalArgumentException(
                    "Vai trò không hợp lệ.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải có ít nhất 12 ký tự.");
        }
    }

    // Lấy danh sách tài khoản
    public static List<UserAccount> getAccounts() {
        requireAdmin();

        List<UserAccount> result = new ArrayList<>();

        String sql = """
                SELECT user_id, username, role, enabled
                FROM users
                ORDER BY user_id
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(new UserAccount(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")));
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách tài khoản.", e);
        }
    }

    // Tạo tài khoản mới
    public static void createAccount(
            String username, String password, String role) {

        requireAdmin();
        validateRole(role);
        validatePassword(password);

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên đăng nhập.");
        }

        String hash = BCrypt.hashpw(
                password, BCrypt.gensalt(12));

        String sql = """
                INSERT INTO users
                    (username, password_hash, role, enabled)
                VALUES (?, ?, ?, 1)
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hash);
            stmt.setString(3, role);

            stmt.executeUpdate();

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "Tên đăng nhập đã tồn tại.", e);
            }

            throw new RuntimeException(
                    "Không thể tạo tài khoản.", e);
        }
    }

    // Lấy tên người dùng theo ID
    private static String findUsername(
            Connection conn, long userId) throws SQLException {

        String sql = """
                SELECT username
                FROM users
                WHERE user_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }

        throw new IllegalStateException(
                "Không tìm thấy tài khoản.");
    }

    // Đổi vai trò admin / staff
    public static void updateRole(long userId, String newRole) {
        requireAdmin();
        validateRole(newRole);

        String sql = """
                UPDATE users
                SET role = ?
                WHERE user_id = ?
                """;

        try (Connection conn = Database.getConnection()) {

            String username = findUsername(conn, userId);

            // Không cho Admin đang đăng nhập tự hạ quyền.
            if (username.equals(UserSession.getUsername())
                    && !"admin".equals(newRole)) {

                throw new IllegalStateException(
                        "Không thể tự hạ quyền tài khoản đang đăng nhập.");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, newRole);
                stmt.setLong(2, userId);

                if (stmt.executeUpdate() != 1) {
                    throw new IllegalStateException(
                            "Không thể thay đổi vai trò.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật quyền tài khoản.", e);
        }
    }

    // Khóa hoặc mở khóa tài khoản
    public static void setEnabled(long userId, boolean enabled) {
        requireAdmin();

        String sql = """
                UPDATE users
                SET enabled = ?
                WHERE user_id = ?
                """;

        try (Connection conn = Database.getConnection()) {

            String username = findUsername(conn, userId);

            // Không để Admin tự khóa chính mình.
            if (!enabled
                    && username.equals(UserSession.getUsername())) {

                throw new IllegalStateException(
                        "Bạn không thể tự khóa tài khoản đang đăng nhập.");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setBoolean(1, enabled);
                stmt.setLong(2, userId);

                if (stmt.executeUpdate() != 1) {
                    throw new IllegalStateException(
                            "Không thể thay đổi trạng thái tài khoản.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể khóa hoặc mở khóa tài khoản.", e);
        }
    }

    // Admin đặt lại mật khẩu cho tài khoản
    public static void resetPassword(
            long userId, String newPassword) {

        requireAdmin();
        validatePassword(newPassword);

        String hash = BCrypt.hashpw(
                newPassword, BCrypt.gensalt(12));

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE user_id = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hash);
            stmt.setLong(2, userId);

            if (stmt.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Không tìm thấy tài khoản cần đổi mật khẩu.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể đặt lại mật khẩu.", e);
        }
    }
}