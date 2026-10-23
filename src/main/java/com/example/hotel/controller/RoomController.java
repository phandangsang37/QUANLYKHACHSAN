package com.example.hotel.controller;

import com.example.hotel.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.hotel.repository.RoomRepository;
import java.text.NumberFormat;
import java.util.Locale;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import com.example.hotel.model.UserSession;

public class RoomController {

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, String> roomNumberColumn;

    @FXML
    private TableColumn<Room, String> roomTypeColumn;

    @FXML
    private TableColumn<Room, Double> priceColumn;
    @FXML
    private TextField roomNumberField;
    @FXML
    private ComboBox<String> roomTypeComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private Button addRoomButton;
    @FXML
    private Button updateRoomButton;
    @FXML
    private Button deleteRoomButton;
    @FXML
    private Label messageLabel;

    @FXML
    private void handleAddRoom() {
        if (!UserSession.isAdmin()) {
            messageLabel.setText("Bạn không có quyền thêm phòng.");
            return;
        }
        String roomNumber = roomNumberField.getText().trim();
        String roomType = roomTypeComboBox.getValue();
        String priceText = priceField.getText().trim();

        if (roomNumber.isEmpty() || roomType == null || priceText.isEmpty()) {
            messageLabel.setText("Vui lòng điền đầy đủ thông tin phòng.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            messageLabel.setText("Giá phòng phải là một số hợp lệ.");
            return;
        }
        if (!Double.isFinite(price) || price <= 0) {
            messageLabel.setText("Giá phòng phải lớn hơn 0 và là số hữu hạn.");
            return;
        }
        for (Room room : RoomRepository.getRooms()) {
            if (room.getRoomNumber().equals(roomNumber)) {
                messageLabel.setText("Số phòng đã tồn tại.");
                return;
            }
        }

        Room newRoom = new Room(roomNumber, roomType, price);
        RoomRepository.addRoom(newRoom);
        messageLabel.setText("Thêm phòng thành công!");

        // Clear input fields
        roomNumberField.clear();
        roomTypeComboBox.getSelectionModel().clearSelection();
        priceField.clear();
    }

    @FXML
    private void initialize() {
        roomNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("roomNumber"));

        roomTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("roomType"));

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("pricePerNight"));
        roomTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldRoom, newRoom) -> {
                    if (newRoom != null) {
                        handleSelectRoom();
                    }
                });
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));
        priceColumn.setCellFactory(column -> new TableCell<Room, Double>() {

            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatter.format(price));
                }
            }
        });
        roomTable.setItems(RoomRepository.getRooms());
        roomTypeComboBox.getItems().addAll(
                "Standard",
                "Deluxe",
                "Suite");
        boolean isAdmin = UserSession.isAdmin();

        addRoomButton.setVisible(isAdmin);
        addRoomButton.setManaged(isAdmin);

        updateRoomButton.setVisible(isAdmin);
        updateRoomButton.setManaged(isAdmin);

        deleteRoomButton.setVisible(isAdmin);
        deleteRoomButton.setManaged(isAdmin);
    }

    @FXML
    private void handleSelectRoom() {

        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            return;
        }

        roomNumberField.setText(
                selectedRoom.getRoomNumber());

        roomTypeComboBox.setValue(
                selectedRoom.getRoomType());

        priceField.setText(
                String.valueOf(selectedRoom.getPricePerNight()));

        roomNumberField.setDisable(true);
    }

    @FXML
    private void handleUpdateRoom() {
        if (!UserSession.isAdmin()) {
            messageLabel.setText("Bạn không có quyền sửa phòng.");
            return;
        }

        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            messageLabel.setText("Vui lòng chọn phòng cần sửa.");
            return;
        }

        try {
            String roomType = roomTypeComboBox.getValue();
            double price = Double.parseDouble(
                    priceField.getText().trim());

            if (roomType == null
                    || !Double.isFinite(price)
                    || price <= 0) {
                messageLabel.setText(
                        "Loại phòng hoặc giá phòng không hợp lệ.");
                return;
            }

            Room updatedRoom = new Room(
                    selectedRoom.getRoomNumber(),
                    roomType,
                    price);

            RoomRepository.updateRoom(updatedRoom);

            roomTable.refresh();

            messageLabel.setText("Cập nhật phòng thành công!");

        } catch (NumberFormatException e) {
            messageLabel.setText("Giá phòng phải là số hợp lệ.");

        } catch (RuntimeException e) {
            messageLabel.setText(
                    "Không thể cập nhật phòng: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (!UserSession.isAdmin()) {
            messageLabel.setText("Bạn không có quyền xóa phòng.");
            return;
        }

        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            messageLabel.setText("Vui lòng chọn phòng cần xóa.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Xác nhận xóa phòng");
        confirm.setHeaderText("Xóa phòng "
                + selectedRoom.getRoomNumber() + "?");

        confirm.setContentText(
                "Phòng đã có lịch đặt sẽ không thể xóa.");

        if (confirm.showAndWait().orElse(
                ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            RoomRepository.deleteRoom(
                    selectedRoom.getRoomNumber());

            roomNumberField.clear();
            roomNumberField.setDisable(false);
            roomTypeComboBox.setValue(null);
            priceField.clear();

            messageLabel.setText("Xóa phòng thành công!");

        } catch (RuntimeException e) {
            messageLabel.setText(
                    "Không thể xóa phòng đã có lịch đặt.");
        }
    }
}