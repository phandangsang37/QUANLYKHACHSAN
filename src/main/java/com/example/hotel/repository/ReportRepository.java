package com.example.hotel.repository;

import com.example.hotel.database.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportRepository {

    public static double getTotalRevenue(
            LocalDate fromDate, LocalDate toDate) {

        String sql = """
                SELECT COALESCE(SUM(total_price), 0)
                FROM bookings
                WHERE status = 'Đã trả phòng'
                  AND check_out_date BETWEEN ? AND ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy doanh thu", e);
        }
    }

    public static int getCompletedBookingCount(
            LocalDate fromDate, LocalDate toDate) {

        String sql = """
                SELECT COUNT(*)
                FROM bookings
                WHERE status = 'Đã trả phòng'
                  AND check_out_date BETWEEN ? AND ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể đếm đơn hoàn thành", e);
        }
    }

    public static Map<LocalDate, Double> getDailyRevenue(
            LocalDate fromDate, LocalDate toDate) {

        Map<LocalDate, Double> revenue = new LinkedHashMap<>();

        String sql = """
                SELECT check_out_date, SUM(total_price) AS revenue
                FROM bookings
                WHERE status = 'Đã trả phòng'
                  AND check_out_date BETWEEN ? AND ?
                GROUP BY check_out_date
                ORDER BY check_out_date
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revenue.put(
                            rs.getDate("check_out_date").toLocalDate(),
                            rs.getDouble("revenue"));
                }
            }

            return revenue;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải biểu đồ doanh thu", e);
        }
    }
}