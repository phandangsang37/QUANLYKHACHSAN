package com.example.hotel.repository;

import com.example.hotel.database.Database;
import com.example.hotel.model.Room;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomRepository {

    private static final ObservableList<Room> rooms = FXCollections.observableArrayList();

    static {
        loadRooms();
    }

    public static void loadRooms() {
        String sql = """
                SELECT room_number, room_type, price_per_night
                FROM rooms
                ORDER BY room_number
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            rooms.clear();

            while (rs.next()) {
                Room room = new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price_per_night"));

                rooms.add(room);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải phòng từ MariaDB", e);
        }
    }

    public static ObservableList<Room> getRooms() {
        return rooms;
    }

    public static void addRoom(Room room) {
        String sql = """
                INSERT INTO rooms
                (room_number, room_type, price_per_night)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType());
            stmt.setDouble(3, room.getPricePerNight());

            stmt.executeUpdate();

            rooms.add(room);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể thêm phòng vào MariaDB", e);
        }
    }

    public static void updateRoom(Room room) {

        String sql = """
                UPDATE rooms
                SET room_type = ?, price_per_night = ?
                WHERE room_number = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomType());
            stmt.setDouble(2, room.getPricePerNight());
            stmt.setString(3, room.getRoomNumber());

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "Không tìm thấy phòng cần cập nhật.");
            }

            loadRooms();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật phòng trong MariaDB", e);
        }
    }

    public static void deleteRoom(String roomNumber) {

        String sql = """
                DELETE FROM rooms
                WHERE room_number = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomNumber);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "Không tìm thấy phòng cần xóa.");
            }

            loadRooms();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa phòng. Phòng có thể đã có đơn đặt.", e);
        }
    }
}