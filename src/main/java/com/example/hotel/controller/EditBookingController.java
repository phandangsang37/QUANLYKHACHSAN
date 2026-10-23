package com.example.hotel.controller;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Room;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.RoomRepository;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class EditBookingController {

    @FXML
    private TextField bookingIdField;

    @FXML
    private TextField customerNameField;

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private DatePicker checkInDatePicker;

    @FXML
    private DatePicker checkOutDatePicker;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label messageLabel;

    private Booking booking;
    private boolean saved = false;

    @FXML
    private void initialize() {

        for (Room room : RoomRepository.getRooms()) {
            roomComboBox.getItems().add(
                    room.getRoomType() + " " + room.getRoomNumber());
        }

        roomComboBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> updateTotalPrice());

        checkInDatePicker.valueProperty().addListener(
                (obs, oldValue, newValue) -> updateTotalPrice());

        checkOutDatePicker.valueProperty().addListener(
                (obs, oldValue, newValue) -> updateTotalPrice());
    }

    // Nhận thông tin đơn từ Dashboard
    public void setBooking(Booking booking) {
        this.booking = booking;

        bookingIdField.setText(booking.getBookingId());
        customerNameField.setText(booking.getCustomerName());
        customerNameField.setEditable(
                booking.getCustomerId() == null);

        roomComboBox.setValue(booking.getRoomName());

        checkInDatePicker.setValue(booking.getCheckInDate());
        checkOutDatePicker.setValue(booking.getCheckOutDate());

        updateTotalPrice();
    }

    private void updateTotalPrice() {

        String roomName = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (roomName == null
                || checkIn == null
                || checkOut == null
                || !checkOut.isAfter(checkIn)) {

            totalPriceLabel.setText("Tổng tiền: 0 ₫");
            return;
        }

        double pricePerNight = 0;

        for (Room room : RoomRepository.getRooms()) {

            String currentRoomName = room.getRoomType() + " " + room.getRoomNumber();

            if (currentRoomName.equals(roomName)) {
                pricePerNight = room.getPricePerNight();
                break;
            }
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        double totalPrice = nights * pricePerNight;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        totalPriceLabel.setText(
                "Tổng tiền: " + formatter.format(totalPrice));
    }

    @FXML
    private void handleSaveChanges() {

        if (booking == null) {
            messageLabel.setText("Không có đơn đặt phòng để sửa.");
            return;
        }

        String customerName = customerNameField.getText().trim();
        String roomName = roomComboBox.getValue();

        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (customerName.isEmpty()) {
            messageLabel.setText("Vui lòng nhập tên khách hàng.");
            return;
        }

        if (roomName == null) {
            messageLabel.setText("Vui lòng chọn phòng.");
            return;
        }

        if (checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) {

            messageLabel.setText("Ngày lưu trú không hợp lệ.");
            return;
        }

        try {

            BookingRepository.updateBooking(

                    booking,
                    customerName,
                    roomName,
                    checkIn,
                    checkOut);

            saved = true;

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText(null);
            success.setContentText(
                    "Đã cập nhật đơn " + booking.getBookingId());
            success.showAndWait();

            handleClose();

        } catch (RuntimeException e) {
            e.printStackTrace();
            messageLabel.setText(e.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) bookingIdField
                .getScene()
                .getWindow();

        stage.close();
    }
}