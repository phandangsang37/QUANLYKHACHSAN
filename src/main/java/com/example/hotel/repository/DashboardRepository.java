package com.example.hotel.repository;

import com.example.hotel.database.Database;
import com.example.hotel.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardRepository {

    public static DashboardStats getDashboardStats() {

        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM rooms) AS total_rooms,

                    (SELECT COUNT(DISTINCT room_number)
                     FROM bookings
                     WHERE status = 'Đang ở') AS occupied_rooms,

                    (SELECT COUNT(*)
                     FROM bookings
                     WHERE check_in_date = CURRENT_DATE()
                       AND status <> 'Đã hủy') AS today_check_ins,

                    (SELECT COUNT(*)
                     FROM bookings
                     WHERE check_out_date = CURRENT_DATE()
                       AND status <> 'Đã hủy') AS today_check_outs,

                    (SELECT COALESCE(SUM(total_price), 0)
                     FROM bookings
                     WHERE check_out_date = CURRENT_DATE()
                       AND status = 'Đã trả phòng') AS today_revenue
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new DashboardStats(
                        rs.getInt("total_rooms"),
                        rs.getInt("occupied_rooms"),
                        rs.getInt("today_check_ins"),
                        rs.getInt("today_check_outs"),
                        rs.getDouble("today_revenue"));
            }

            throw new IllegalStateException(
                    "Không đọc được dữ liệu thống kê.");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải thống kê Dashboard", e);
        }
    }
}