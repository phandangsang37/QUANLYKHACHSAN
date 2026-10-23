package com.example.hotel.database;

import org.mindrot.jbcrypt.BCrypt;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class CreateAdmin {

    public static void main(String[] args) {

        String password;

        Console console = System.console();

        if (console != null) {
            char[] chars = console.readPassword(
                    "Nhập mật khẩu cho tài khoản admin: ");

            if (chars == null) {
                return;
            }

            password = new String(chars);
            java.util.Arrays.fill(chars, '\0');

        } else {
            // Một số cách chạy Java trong VS Code không có Console
            Scanner scanner = new Scanner(System.in);

            System.out.print("Nhập mật khẩu admin: ");
            password = scanner.nextLine();

            // Trong trường hợp này ký tự có thể hiển thị
            // trên terminal, chỉ dùng để khởi tạo cục bộ.
        }

        if (password.length() < 12) {
            System.out.println(
                    "Mật khẩu cần có ít nhất 12 ký tự.");
            return;
        }

        String hash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12));

        String sql = """
                INSERT INTO users
                    (username, password_hash, role)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "staff01");
            stmt.setString(2, hash);
            stmt.setString(3, "staff");

            stmt.executeUpdate();

            System.out.println(
                    "Tạo tài khoản admin thành công!");

        } catch (SQLException e) {
            System.out.println(
                    "Không thể tạo admin. Tài khoản có thể đã tồn tại.");
            e.printStackTrace();
        }
    }
}