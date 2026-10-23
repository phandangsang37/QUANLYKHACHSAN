package com.example.hotel.repository;

import com.example.hotel.database.Database;
import com.example.hotel.model.Booking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;

public class BookingRepository {

    private static final ObservableList<Booking> bookings = FXCollections.observableArrayList();

    static {
        loadBookings();
    }

    // 1. Đọc danh sách đặt phòng từ MySQL
    public static void loadBookings() {

        bookings.clear();

        String sql = """
                SELECT
                    b.booking_id,
                    b.customer_id,
                    b.customer_name,
                    r.room_type,
                    b.room_number,
                    b.check_in_date,
                    b.check_out_date,
                    b.total_price,
                    b.status
                FROM bookings b
                JOIN rooms r ON b.room_number = r.room_number
                ORDER BY b.booking_id
                """;

        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                LocalDate checkIn = result.getDate("check_in_date")
                        .toLocalDate();

                LocalDate checkOut = result.getDate("check_out_date")
                        .toLocalDate();

                String roomName = result.getString("room_type")
                        + " "
                        + result.getString("room_number");

                Booking booking = new Booking(
                        result.getString("booking_id"),
                        result.getString("customer_name"),
                        roomName,
                        checkIn + " - " + checkOut,
                        result.getDouble("total_price"),
                        result.getString("status"));

                booking.setCheckInDate(checkIn);
                booking.setCheckOutDate(checkOut);
                long customerId = result.getLong("customer_id");

                booking.setCustomerId(
                        result.wasNull() ? null : customerId);

                bookings.add(booking);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải đơn đặt phòng từ MySQL", e);
        }
    }

    // 2. Lấy danh sách hiện tại
    public static ObservableList<Booking> getBookings() {
        return bookings;
    }

    // 3. Tạo mã đơn mới
    public static String generateBookingId() {

        String sql = """
                SELECT COALESCE(
                    MAX(CAST(SUBSTRING(booking_id, 5)
                        AS UNSIGNED)), 0
                ) AS max_id
                FROM bookings
                WHERE booking_id REGEXP '^SDZ-[0-9]+$'
                """;

        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                int nextId = result.getInt("max_id") + 1;
                return String.format("SDZ-%03d", nextId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tạo mã đặt phòng", e);
        }

        throw new IllegalStateException(
                "Không đọc được mã đặt phòng từ MySQL");
    }

    // 4. Kiểm tra phòng có bị trùng lịch không
    public static boolean isRoomAvailable(
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut) {

        if (roomName == null
                || checkIn == null
                || checkOut == null
                || !checkOut.isAfter(checkIn)) {
            return false;
        }

        String roomNumber = extractRoomNumber(roomName);

        String sql = """
                                SELECT COUNT(*) AS booking_count
                FROM bookings
                WHERE room_number = ?
                  AND status <> 'Đã hủy'
                  AND check_in_date < ?
                  AND check_out_date > ?
                                """;

        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, roomNumber);
            statement.setDate(2, Date.valueOf(checkOut));
            statement.setDate(3, Date.valueOf(checkIn));

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("booking_count") == 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra lịch phòng", e);
        }

        return false;
    }

    // 5. Lưu đơn mới vào MySQL
    public static void addBooking(Booking booking) {

        String sql = """
                INSERT INTO bookings
                (booking_id, customer_id, customer_name,
                 room_number, check_in_date, check_out_date,
                 total_price, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, booking.getBookingId());

            if (booking.getCustomerId() == null) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn khách hàng.");
            }

            statement.setLong(2, booking.getCustomerId());

            statement.setString(3, booking.getCustomerName());

            statement.setString(
                    4, extractRoomNumber(booking.getRoomName()));

            statement.setDate(
                    5, Date.valueOf(booking.getCheckInDate()));

            statement.setDate(
                    6, Date.valueOf(booking.getCheckOutDate()));

            statement.setDouble(7, booking.getTotalPrice());
            statement.setString(8, booking.getStatus());

            statement.executeUpdate();

            bookings.add(booking);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể lưu đơn đặt phòng", e);
        }
    }

    // 6. Lưu trạng thái Nhận phòng / Trả phòng
    public static void updateBookingStatus(
            Booking booking,
            String newStatus) {

        String sql = """
                UPDATE bookings
                SET status = ?
                WHERE booking_id = ?
                """;

        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus);
            statement.setString(2, booking.getBookingId());

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "Không tìm thấy đơn: "
                                + booking.getBookingId());
            }

            booking.setStatus(newStatus);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật trạng thái đặt phòng", e);
        }
    }

    // 7. Lấy số phòng từ "Deluxe 502", "Suite 601", ...
    private static String extractRoomNumber(String roomName) {

        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên phòng không được để trống");
        }

        int lastSpace = roomName.lastIndexOf(' ');

        if (lastSpace < 0) {
            throw new IllegalArgumentException(
                    "Tên phòng không hợp lệ: " + roomName);
        }

        return roomName.substring(lastSpace + 1);
    }

    public static void cancelBooking(Booking booking) {

        if (!"Chờ nhận phòng".equals(booking.getStatus())) {
            throw new IllegalStateException(
                    "Chỉ được hủy đơn đang chờ nhận phòng.");
        }

        updateBookingStatus(booking, "Đã hủy");
    }

    public static void updateBooking(
            Booking booking,
            String customerName,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut) {

        if (booking == null) {
            throw new IllegalArgumentException("Đơn đặt phòng không hợp lệ.");
        }
        if (booking.getCustomerId() != null
                && !booking.getCustomerName().equals(
                        customerName.trim())) {

            throw new IllegalArgumentException(
                    "Không thể sửa tên khách hàng của đơn đã liên kết.");
        }

        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng.");
        }

        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phòng.");
        }

        if (checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Ngày lưu trú không hợp lệ.");
        }

        String roomNumber = extractRoomNumber(roomName);

        try (Connection conn = Database.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Khóa đơn đang sửa
                String bookingSql = """
                        SELECT status
                        FROM bookings
                        WHERE booking_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement stmt = conn.prepareStatement(bookingSql)) {

                    stmt.setString(1, booking.getBookingId());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "Không tìm thấy đơn đặt phòng.");
                        }

                        if (!"Chờ nhận phòng".equals(rs.getString("status"))) {
                            throw new IllegalStateException(
                                    "Chỉ được sửa đơn đang chờ nhận phòng.");
                        }
                    }
                }

                // Kiểm tra phòng và lấy giá mới
                String roomSql = """
                        SELECT price_per_night
                        FROM rooms
                        WHERE room_number = ?
                        FOR UPDATE
                        """;

                double pricePerNight;

                try (PreparedStatement stmt = conn.prepareStatement(roomSql)) {

                    stmt.setString(1, roomNumber);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "Không tìm thấy phòng.");
                        }

                        pricePerNight = rs.getDouble("price_per_night");
                    }
                }

                if (!Double.isFinite(pricePerNight)
                        || pricePerNight <= 0) {
                    throw new IllegalStateException(
                            "Giá phòng không hợp lệ.");
                }

                // Kiểm tra trùng lịch, loại trừ đơn đang sửa
                String checkSql = """
                        SELECT COUNT(*) AS total
                        FROM bookings
                        WHERE room_number = ?
                          AND booking_id <> ?
                          AND status <> 'Đã hủy'
                          AND check_in_date < ?
                          AND check_out_date > ?
                        """;

                try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {

                    stmt.setString(1, roomNumber);
                    stmt.setString(2, booking.getBookingId());
                    stmt.setDate(3, Date.valueOf(checkOut));
                    stmt.setDate(4, Date.valueOf(checkIn));

                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();

                        if (rs.getInt("total") > 0) {
                            throw new IllegalStateException(
                                    "Phòng đã có người đặt trong khoảng ngày này.");
                        }
                    }
                }

                long nights = java.time.temporal.ChronoUnit.DAYS
                        .between(checkIn, checkOut);

                double totalPrice = pricePerNight * nights;

                // Cập nhật đơn vào MariaDB
                String updateSql = """
                        UPDATE bookings
                        SET customer_name = ?,
                            room_number = ?,
                            check_in_date = ?,
                            check_out_date = ?,
                            total_price = ?
                        WHERE booking_id = ?
                          AND status = 'Chờ nhận phòng'
                        """;

                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                    stmt.setString(1, customerName.trim());
                    stmt.setString(2, roomNumber);
                    stmt.setDate(3, Date.valueOf(checkIn));
                    stmt.setDate(4, Date.valueOf(checkOut));
                    stmt.setDouble(5, totalPrice);
                    stmt.setString(6, booking.getBookingId());

                    if (stmt.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Không thể cập nhật đơn đặt phòng.");
                    }
                }

                conn.commit();

                // Chỉ cập nhật đối tượng Java sau khi DB lưu thành công
                booking.setCustomerName(customerName.trim());
                booking.setRoomName(roomName);
                booking.setCheckInDate(checkIn);
                booking.setCheckOutDate(checkOut);
                booking.setStayDate(checkIn + " - " + checkOut);
                booking.setTotalPrice(totalPrice);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể sửa đơn: " + e.getMessage(), e);
        }
    }

    public static ObservableList<Booking> getBookingsByCustomerId(
            long customerId) {

        ObservableList<Booking> history = FXCollections.observableArrayList();

        String sql = """
                SELECT
                    b.booking_id,
                    b.customer_id,
                    b.customer_name,
                    b.room_number,
                    r.room_type,
                    b.check_in_date,
                    b.check_out_date,
                    b.total_price,
                    b.status
                FROM bookings b
                JOIN rooms r
                    ON b.room_number = r.room_number
                WHERE b.customer_id = ?
                ORDER BY b.check_in_date DESC, b.booking_id DESC
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();

                    LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();

                    String roomName = rs.getString("room_type")
                            + " "
                            + rs.getString("room_number");

                    Booking booking = new Booking(
                            rs.getString("booking_id"),
                            rs.getString("customer_name"),
                            roomName,
                            checkIn + " - " + checkOut,
                            rs.getDouble("total_price"),
                            rs.getString("status"));

                    booking.setCustomerId(
                            rs.getLong("customer_id"));

                    booking.setCheckInDate(checkIn);
                    booking.setCheckOutDate(checkOut);

                    history.add(booking);
                }
            }

            return history;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải lịch sử đặt phòng.", e);
        }
    }
}