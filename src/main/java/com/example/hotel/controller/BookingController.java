package com.example.hotel.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.example.hotel.model.Booking;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.model.Room;
import com.example.hotel.repository.RoomRepository;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.Button;
import com.example.hotel.model.Customer;
import com.example.hotel.repository.CustomerRepository;
import javafx.util.StringConverter;

public class BookingController {

    @FXML
    private TextField customerNameField;
    @FXML
    private ComboBox<String> roomComboBox;
    @FXML
    private DatePicker checkInDatePicker;
    @FXML
    private DatePicker checkOutDatePicker;
    @FXML
    private Label messageLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label numberOfNightsLabel;
    @FXML
    private Label roomStatusLabel;
    @FXML
    private Button saveBookingButton;
    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private void initialize() {
        for (Room room : RoomRepository.getRooms()) {
            String roomName = room.getRoomType() + " " + room.getRoomNumber();

            roomComboBox.getItems().add(roomName);
        }
        roomComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalPrice();
                    updateRoomStatus();
                    updateSaveButton();
                });

        checkInDatePicker.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalPrice();
                    updateRoomStatus();
                    updateSaveButton();
                });

        checkOutDatePicker.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalPrice();
                    updateRoomStatus();
                    updateSaveButton();
                });
        customerNameField.textProperty().addListener(
                (observable, oldValue, newValue) -> updateSaveButton());
        updateSaveButton();
        CustomerRepository.loadCustomers();

        customerComboBox.getItems().setAll(
                CustomerRepository.getCustomers());

        customerComboBox.setConverter(new StringConverter<Customer>() {

            @Override
            public String toString(Customer customer) {
                if (customer == null) {
                    return "";
                }

                return customer.getFullName()
                        + " - " + customer.getPhone();
            }

            @Override
            public Customer fromString(String text) {
                return null;
            }
        });

        customerNameField.setEditable(false);

        customerComboBox.valueProperty().addListener(
                (obs, oldValue, selectedCustomer) -> {

                    if (selectedCustomer == null) {
                        customerNameField.clear();
                    } else {
                        customerNameField.setText(
                                selectedCustomer.getFullName());
                    }

                    updateSaveButton();
                });
    }

    @FXML
    private void handleSaveBooking() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        String customerName = customerNameField.getText().trim();
        Customer selectedCustomer = customerComboBox.getValue();

        if (selectedCustomer == null) {
            messageLabel.setText(
                    "Vui lòng chọn khách hàng.");
            return;
        }

        if (customerName.isEmpty()) {
            messageLabel.setText("Vui lòng nhập tên khách hàng.");
            return;
        }
        if (roomComboBox.getValue() == null) {
            messageLabel.setText("Vui lòng chọn phòng.");
            return;
        }
        if (checkIn == null || checkOut == null) {
            messageLabel.setText("Vui lòng chọn ngày nhận phòng và ngày trả phòng.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            messageLabel.setText("Ngày trả phòng phải sau ngày nhận phòng.");
            return;
        }
        String roomName = roomComboBox.getValue();

        boolean available = BookingRepository.isRoomAvailable(
                roomName,
                checkIn,
                checkOut);
        if (!available) {
            messageLabel.getStyleClass().removeAll("message-success", "message-error");
            messageLabel.getStyleClass().add("message-error");
            messageLabel.setText("Phòng đã được đặt trong khoảng thời gian này.");
            return;
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);

        System.out.println("Số đêm lưu trú: " + numberOfNights);
        double pricePerNight = getRoomPrice(roomName);
        if (pricePerNight <= 0) {
            messageLabel.getStyleClass().removeAll(
                    "message-success", "message-error");
            messageLabel.getStyleClass().add("message-error");

            messageLabel.setText("Không tìm thấy thông tin giá phòng.");
            return;
        }
        double totalPrice = pricePerNight * numberOfNights;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        totalPriceLabel.setText(
                "Tổng tiền: " + formatter.format(totalPrice));
        System.out.println("Tổng giá: " + totalPrice);
        String bookingId = BookingRepository.generateBookingId();
        Booking newBooking = new Booking(
                bookingId,
                customerName,
                roomName,
                checkIn + " - " + checkOut,
                totalPrice,
                "Chờ nhận phòng"

        );
        newBooking.setCheckInDate(checkIn);
        newBooking.setCheckOutDate(checkOut);
        newBooking.setCustomerId(
                selectedCustomer.getCustomerId());

        try {
            BookingRepository.addBooking(newBooking);

        } catch (RuntimeException e) {
            e.printStackTrace();

            messageLabel.getStyleClass().removeAll(
                    "message-success", "message-error");

            messageLabel.getStyleClass().add("message-error");

            messageLabel.setText(
                    "Không thể lưu đặt phòng. "
                            + "Vui lòng kiểm tra dữ liệu hoặc kết nối MariaDB.");

            return;
        }
        updateRoomStatus();
        updateSaveButton();
        System.out.println("Mã đặt phòng: " + newBooking.getBookingId());

        System.out.println("Khách hàng: " + newBooking.getCustomerName());

        System.out.println("Tổng tiền: " + newBooking.getTotalPrice());
        System.out.println(
                "Số đơn đặt phòng: " + BookingRepository.getBookings().size());
        messageLabel.getStyleClass().removeAll(
                "message-error", "message-success");

        messageLabel.getStyleClass().add("message-success");

        messageLabel.setText(
                "Đặt phòng thành công! Mã đặt phòng: " + bookingId);

        clearBookingForm();
        System.out.println("Ngày nhận phòng: " + newBooking.getCheckInDate());
        System.out.println("Ngày trả phòng: " + newBooking.getCheckOutDate());
    }

    @FXML
    private void updateTotalPrice() {
        String roomName = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (roomName == null || checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) {
            numberOfNightsLabel.setText("Số đêm lưu trú: 0");
            totalPriceLabel.setText("Tổng tiền dự kiến: 0 ₫");
            return;
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        numberOfNightsLabel.setText(
                "Số đêm lưu trú: " + numberOfNights);
        double pricePerNight = getRoomPrice(roomName);
        if (pricePerNight <= 0) {
            totalPriceLabel.setText("Không tìm thấy thông tin giá phòng.");
            return;
        }
        double totalPrice = pricePerNight * numberOfNights;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        totalPriceLabel.setText(
                "Tổng tiền dự kiến: " + formatter.format(totalPrice));

    }

    private double getRoomPrice(String roomName) {
        for (Room room : RoomRepository.getRooms()) {

            String currentRoomName = room.getRoomType() + " " + room.getRoomNumber();

            if (currentRoomName.equals(roomName)) {
                return room.getPricePerNight();
            }
        }

        return 0;
    }

    private void updateRoomStatus() {
        String roomName = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        roomStatusLabel.getStyleClass().removeAll(
                "room-available",
                "room-unavailable",
                "room-unknown");
        if (roomName == null || checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) {
            roomStatusLabel.setText("Vui lòng chọn phòng và ngày hợp lệ.");
            roomStatusLabel.getStyleClass().add("room-unknown");
            return;
        }

        boolean available = BookingRepository.isRoomAvailable(
                roomName,
                checkIn,
                checkOut);
        if (available) {
            roomStatusLabel.getStyleClass().add("room-available");
            roomStatusLabel.setText("Phòng còn trống.");
        } else {
            roomStatusLabel.getStyleClass().add("room-unavailable");
            roomStatusLabel.setText("Phòng đã có người đặt.");
        }

    }

    private void updateSaveButton() {
        String customerName = customerNameField.getText().trim();
        String roomName = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        boolean validName = !customerName.isEmpty();

        boolean validDate = checkIn != null
                && checkOut != null
                && checkOut.isAfter(checkIn);

        boolean available = false;

        if (roomName != null && validDate) {
            available = BookingRepository.isRoomAvailable(
                    roomName, checkIn, checkOut);
        }
        boolean canSave = validName
                && customerComboBox.getValue() != null
                && roomName != null
                && validDate
                && available
                && getRoomPrice(roomName) > 0;

        saveBookingButton.setDisable(!canSave);

    }

    private void clearBookingForm() {
        customerNameField.clear();
        customerComboBox.getSelectionModel().clearSelection();
        roomComboBox.getSelectionModel().clearSelection();

        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        numberOfNightsLabel.setText("Số đêm lưu trú: 0");

        totalPriceLabel.setText("Tổng tiền dự kiến: 0 ₫");
        updateRoomStatus();
        updateSaveButton();
    }

    @FXML
    private void handleResetForm() {
        clearBookingForm();
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(
                "message-error", "message-success");

    }
}