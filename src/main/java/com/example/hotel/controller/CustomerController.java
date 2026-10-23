package com.example.hotel.controller;

import com.example.hotel.model.Customer;
import com.example.hotel.repository.CustomerRepository;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Locale;
import com.example.hotel.model.Booking;
import com.example.hotel.repository.BookingRepository;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerController {

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Long> customerIdColumn;
    @FXML
    private TableColumn<Customer, String> fullNameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private TableColumn<Customer, String> addressColumn;

    @FXML
    private TextField searchField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private Label messageLabel;

    private Customer selectedCustomer;
    private FilteredList<Customer> filteredCustomers;

    @FXML
    private void initialize() {

        customerIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getValue().getCustomerId()));

        fullNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFullName()));

        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPhone()));

        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEmail()));

        addressColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAddress()));

        filteredCustomers = new FilteredList<>(
                CustomerRepository.getCustomers(),
                customer -> true);

        customerTable.setItems(filteredCustomers);

        // Tìm kiếm trực tiếp
        searchField.textProperty().addListener(
                (obs, oldValue, newValue) -> {

                    String keyword = newValue == null
                            ? ""
                            : newValue.trim()
                                    .toLowerCase(Locale.ROOT);

                    filteredCustomers.setPredicate(customer -> {
                        if (keyword.isEmpty()) {
                            return true;
                        }

                        return containsKeyword(
                                customer.getFullName(), keyword)
                                || containsKeyword(
                                        customer.getPhone(), keyword)
                                || containsKeyword(
                                        customer.getEmail(), keyword);
                    });
                });

        // Chọn khách hàng từ bảng
        customerTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    selectedCustomer = newValue;

                    if (newValue != null) {
                        fullNameField.setText(newValue.getFullName());
                        phoneField.setText(newValue.getPhone());
                        emailField.setText(newValue.getEmail());
                        addressField.setText(newValue.getAddress());
                    }
                });
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    @FXML
    private void handleAddCustomer() {

        String fullName = fullNameField.getText().trim();

        if (fullName.isEmpty()) {
            messageLabel.setText("Vui lòng nhập họ tên khách hàng.");
            return;
        }

        Customer customer = new Customer(
                0,
                fullName,
                phoneField.getText().trim(),
                emailField.getText().trim(),
                addressField.getText().trim());

        try {
            CustomerRepository.addCustomer(customer);

            clearForm();
            messageLabel.setText("Thêm khách hàng thành công!");

        } catch (RuntimeException e) {
            e.printStackTrace();
            messageLabel.setText("Không thể thêm khách hàng.");
        }
    }

    @FXML
    private void handleUpdateCustomer() {

        if (selectedCustomer == null) {
            messageLabel.setText("Vui lòng chọn khách hàng cần sửa.");
            return;
        }

        String fullName = fullNameField.getText().trim();

        if (fullName.isEmpty()) {
            messageLabel.setText("Họ tên không được để trống.");
            return;
        }

        // Tạo đối tượng mới để không thay đổi dữ liệu
        // trên bảng trước khi MariaDB lưu thành công
        Customer updated = new Customer(
                selectedCustomer.getCustomerId(),
                fullName,
                phoneField.getText().trim(),
                emailField.getText().trim(),
                addressField.getText().trim());

        try {
            CustomerRepository.updateCustomer(updated);

            clearForm();
            messageLabel.setText("Cập nhật khách hàng thành công!");

        } catch (RuntimeException e) {
            e.printStackTrace();
            messageLabel.setText("Không thể cập nhật khách hàng.");
        }
    }

    @FXML
    private void handleDeleteCustomer() {

        if (selectedCustomer == null) {
            messageLabel.setText("Vui lòng chọn khách hàng cần xóa.");
            return;
        }

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa khách hàng "
                        + selectedCustomer.getFullName() + "?",
                ButtonType.YES,
                ButtonType.NO);

        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            CustomerRepository.deleteCustomer(
                    selectedCustomer.getCustomerId());

            clearForm();
            messageLabel.setText("Xóa khách hàng thành công!");

        } catch (RuntimeException e) {
            e.printStackTrace();
            messageLabel.setText("Không thể xóa khách hàng.");
        }
    }

    @FXML
    private void handleResetForm() {
        clearForm();
        messageLabel.setText("");
    }

    private void clearForm() {

        customerTable.getSelectionModel().clearSelection();
        selectedCustomer = null;

        fullNameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
    }

    @FXML
    private void handleBookingHistory() {

        if (selectedCustomer == null) {
            messageLabel.setText(
                    "Vui lòng chọn khách hàng cần xem lịch sử.");
            return;
        }

        try {
            TableView<Booking> historyTable = new TableView<>();

            TableColumn<Booking, String> idColumn = new TableColumn<>("Mã đơn");

            idColumn.setCellValueFactory(
                    new PropertyValueFactory<>("bookingId"));

            TableColumn<Booking, String> roomColumn = new TableColumn<>("Phòng");

            roomColumn.setCellValueFactory(
                    new PropertyValueFactory<>("roomName"));

            TableColumn<Booking, String> dateColumn = new TableColumn<>("Ngày lưu trú");

            dateColumn.setCellValueFactory(
                    new PropertyValueFactory<>("stayDate"));

            TableColumn<Booking, Double> priceColumn = new TableColumn<>("Tổng tiền");

            priceColumn.setCellValueFactory(
                    new PropertyValueFactory<>("totalPrice"));

            TableColumn<Booking, String> statusColumn = new TableColumn<>("Trạng thái");

            statusColumn.setCellValueFactory(
                    new PropertyValueFactory<>("status"));

            historyTable.getColumns().addAll(
                    idColumn,
                    roomColumn,
                    dateColumn,
                    priceColumn,
                    statusColumn);

            historyTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY);

            historyTable.setItems(
                    BookingRepository.getBookingsByCustomerId(
                            selectedCustomer.getCustomerId()));

            historyTable.setPlaceholder(
                    new Label("Khách hàng chưa có đơn đặt phòng."));

            Dialog<Void> dialog = new Dialog<>();

            dialog.setTitle("Lịch sử đặt phòng");
            dialog.setHeaderText(
                    "Khách hàng: " + selectedCustomer.getFullName());

            dialog.getDialogPane().getButtonTypes().add(
                    ButtonType.CLOSE);

            dialog.getDialogPane().setContent(historyTable);
            dialog.getDialogPane().setPrefSize(900, 450);

            dialog.showAndWait();

        } catch (RuntimeException e) {
            e.printStackTrace();
            messageLabel.setText(
                    "Không thể tải lịch sử đặt phòng.");
        }
    }
}