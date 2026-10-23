package com.example.hotel.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import com.example.hotel.model.Booking;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import com.example.hotel.repository.BookingRepository;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import com.example.hotel.model.DashboardStats;
import com.example.hotel.repository.DashboardRepository;
import com.example.hotel.model.UserSession;

public class DashboardController {
        @FXML
        private TableView<Booking> bookingTable;
        @FXML
        private TableColumn<Booking, String> bookingIdColumn;
        @FXML
        private TableColumn<Booking, String> customerNameColumn;
        @FXML
        private TableColumn<Booking, String> roomNameColumn;
        @FXML
        private TableColumn<Booking, String> stayDateColumn;
        @FXML
        private TableColumn<Booking, Double> totalPriceColumn;
        @FXML
        private TableColumn<Booking, String> statusColumn;
        @FXML
        private TableColumn<Booking, String> actionColumn;
        @FXML
        private BorderPane mainLayout;
        private Node overviewContent;
        private FilteredList<Booking> filteredBookings;
        @FXML
        private TextField searchBookingField;
        @FXML
        private ComboBox<String> statusFilterComboBox;
        @FXML
        private Label bookingCountLabel;
        @FXML
        private Label occupancyLabel;
        @FXML
        private Label occupancyDetailLabel;
        @FXML
        private Label checkInTodayLabel;
        @FXML
        private Label checkOutTodayLabel;
        @FXML
        private Label revenueTodayLabel;
        @FXML
        private Button reportButton;
        @FXML
        private Button accountButton;

        private void showDatabaseError(String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Lỗi cơ sở dữ liệu");
                alert.setHeaderText("Không thể thực hiện thao tác");
                alert.setContentText(message);

                alert.showAndWait();
        }

        @FXML
        private void initialize() {
                bookingIdColumn.setCellValueFactory(
                                new PropertyValueFactory<>("bookingId"));
                customerNameColumn.setCellValueFactory(
                                new PropertyValueFactory<>("customerName"));
                roomNameColumn.setCellValueFactory(
                                new PropertyValueFactory<>("roomName"));
                stayDateColumn.setCellValueFactory(
                                new PropertyValueFactory<>("stayDate"));
                totalPriceColumn.setCellValueFactory(
                                new PropertyValueFactory<>("totalPrice"));
                statusColumn.setCellValueFactory(
                                new PropertyValueFactory<>("status"));
                NumberFormat formatter = NumberFormat.getCurrencyInstance(
                                Locale.forLanguageTag("vi-VN"));

                totalPriceColumn.setCellFactory(column -> new TableCell<Booking, Double>() {

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
                statusColumn.setCellFactory(column -> new TableCell<Booking, String>() {

                        @Override
                        protected void updateItem(String status, boolean empty) {
                                super.updateItem(status, empty);

                                setText(null);
                                setGraphic(null);

                                if (!empty && status != null) {
                                        Label statusLabel = new Label(status);

                                        if (status.equals("Chờ nhận phòng")) {
                                                statusLabel.getStyleClass().add("status-pending");
                                        } else if (status.equals("Đang ở")) {
                                                statusLabel.getStyleClass().add("status-occupied");
                                        } else if (status.equals("Đã trả phòng")) {
                                                statusLabel.getStyleClass().add("status-completed");
                                        } else if (status.equals("Đã hủy")) {
                                                statusLabel.getStyleClass().add("status-cancelled");
                                        }

                                        setGraphic(statusLabel);
                                }
                        }
                });
                actionColumn.setCellFactory(column -> new TableCell<Booking, String>() {

                        @Override
                        protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);

                                setText(null);
                                setGraphic(null);

                                if (!empty) {
                                        Booking booking = getTableRow().getItem();

                                        if (booking != null) {
                                                Button actionButton = new Button();

                                                if (booking.getStatus().equals("Chờ nhận phòng")) {

                                                        Button checkInButton = new Button("Nhận phòng");
                                                        Button cancelButton = new Button("Hủy đơn");
                                                        Button editButton = new Button("Sửa");

                                                        editButton.setOnAction(event -> {
                                                                openEditBookingWindow(booking);
                                                        });
                                                        checkInButton.setOnAction(event -> {
                                                                try {
                                                                        BookingRepository.updateBookingStatus(
                                                                                        booking, "Đang ở");

                                                                        applyBookingFilters();
                                                                        bookingTable.refresh();
                                                                        // Cập nhật thống kê
                                                                        updateDashboardStats();

                                                                } catch (RuntimeException e) {
                                                                        showDatabaseError(e.getMessage());
                                                                }
                                                        });

                                                        cancelButton.setOnAction(event -> {

                                                                Alert confirm = new Alert(
                                                                                Alert.AlertType.CONFIRMATION,
                                                                                "Bạn có chắc muốn hủy đơn "
                                                                                                + booking.getBookingId()
                                                                                                + "?",
                                                                                ButtonType.YES,
                                                                                ButtonType.NO);

                                                                if (confirm.showAndWait().orElse(
                                                                                ButtonType.NO) != ButtonType.YES) {
                                                                        return;
                                                                }

                                                                try {
                                                                        BookingRepository.cancelBooking(booking);

                                                                        applyBookingFilters();
                                                                        bookingTable.refresh();

                                                                        // Cập nhật thống kê
                                                                        updateDashboardStats();

                                                                } catch (RuntimeException e) {
                                                                        showDatabaseError(e.getMessage());
                                                                }
                                                        });

                                                        HBox actions = new HBox(8, checkInButton, editButton,
                                                                        cancelButton);
                                                        setGraphic(actions);

                                                        return;
                                                } else if (booking.getStatus().equals("Đang ở")) {
                                                        actionButton.setText("Trả phòng");
                                                        actionButton.setOnAction(event -> {
                                                                try {
                                                                        BookingRepository.updateBookingStatus(
                                                                                        booking, "Đã trả phòng");

                                                                        applyBookingFilters();
                                                                        bookingTable.refresh();

                                                                        // Cập nhật thống kê
                                                                        updateDashboardStats();

                                                                } catch (RuntimeException e) {
                                                                        showDatabaseError(e.getMessage());
                                                                }
                                                        });
                                                } else if (booking.getStatus().equals("Đã trả phòng")) {
                                                        actionButton.setText("Xem hóa đơn");
                                                        actionButton.setOnAction(event -> {
                                                                try {
                                                                        FXMLLoader loader = new FXMLLoader(getClass()
                                                                                        .getResource("/com/example/hotel/fxml/invoice.fxml"));
                                                                        Scene scene = new Scene(loader.load());
                                                                        InvoiceController invoiceController = loader
                                                                                        .getController();
                                                                        invoiceController.setBooking(booking);

                                                                        Stage stage = new Stage();
                                                                        stage.setTitle("Hóa đơn thanh toán");
                                                                        stage.setScene(scene);
                                                                        stage.show();
                                                                } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                }
                                                        });
                                                } else if (booking.getStatus().equals("Đã hủy")) {
                                                        actionButton.setText("Đã hủy");
                                                        actionButton.setDisable(true);
                                                }

                                                setGraphic(actionButton);
                                        }
                                }
                        }
                });

                filteredBookings = new FilteredList<>(
                                BookingRepository.getBookings(),
                                booking -> true);

                bookingTable.setItems(filteredBookings);
                filteredBookings.addListener(
                                (javafx.collections.ListChangeListener<Booking>) change -> updateBookingCount());
                statusFilterComboBox.getItems().addAll(
                                "Tất cả",
                                "Chờ nhận phòng",
                                "Đang ở",
                                "Đã trả phòng",
                                "Đã hủy");

                statusFilterComboBox.setValue("Tất cả");
                // Theo dõi khi người dùng nhập từ khóa
                searchBookingField.textProperty().addListener(
                                (observable, oldValue, newValue) -> applyBookingFilters());

                // Theo dõi khi người dùng thay đổi trạng thái
                statusFilterComboBox.valueProperty().addListener(
                                (observable, oldValue, newValue) -> applyBookingFilters());

                // Áp dụng bộ lọc ban đầu
                applyBookingFilters();
                updateBookingCount();

                // Lưu nội dung trang Tổng quan
                overviewContent = mainLayout.getCenter();
                // Tải thống kê từ MariaDB
                updateDashboardStats();
                reportButton.setVisible(UserSession.isAdmin());
                reportButton.setManaged(UserSession.isAdmin());
                boolean isAdmin = UserSession.isAdmin();

                accountButton.setVisible(isAdmin);
                accountButton.setManaged(isAdmin);
        }

        private void updateBookingCount() {
                int displayedCount = filteredBookings.size();

                int totalCount = BookingRepository.getBookings().size();

                bookingCountLabel.setText(
                                "Hiển thị: " + displayedCount
                                                + " / " + totalCount
                                                + " đơn đặt phòng");
        }

        private void applyBookingFilters() {
                String keyword = searchBookingField.getText() == null
                                ? ""
                                : searchBookingField.getText().trim().toLowerCase(Locale.ROOT);

                String selectedStatus = statusFilterComboBox.getValue();

                filteredBookings.setPredicate(booking -> {

                        String bookingId = booking.getBookingId()
                                        .toLowerCase(Locale.ROOT);

                        String customerName = booking.getCustomerName()
                                        .toLowerCase(Locale.ROOT);

                        boolean matchesSearch = keyword.isEmpty()
                                        || bookingId.contains(keyword)
                                        || customerName.contains(keyword);

                        boolean matchesStatus = selectedStatus == null
                                        || selectedStatus.equals("Tất cả")
                                        || booking.getStatus().equals(selectedStatus);

                        return matchesSearch && matchesStatus;
                });
        }

        @FXML
        private void handleOverview() {
                mainLayout.setCenter(overviewContent);
                updateDashboardStats();
        }

        @FXML
        private void handleBooking() {
                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource("/com/example/hotel/fxml/booking.fxml"));

                        mainLayout.setCenter(loader.load());

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @FXML
        private void handleRoom() {
                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource("/com/example/hotel/fxml/room.fxml"));

                        mainLayout.setCenter(loader.load());

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @FXML
        private void handleResetBookingFilters() {
                searchBookingField.clear();

                statusFilterComboBox.setValue("Tất cả");
        }

        private void openEditBookingWindow(Booking booking) {

                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(
                                                        "/com/example/hotel/fxml/edit_booking.fxml"));

                        Scene scene = new Scene(loader.load());

                        EditBookingController controller = loader.getController();

                        controller.setBooking(booking);

                        Stage stage = new Stage();
                        stage.setTitle(
                                        "Chỉnh sửa đặt phòng - " + booking.getBookingId());

                        stage.initOwner(bookingTable.getScene().getWindow());
                        stage.initModality(Modality.WINDOW_MODAL);

                        stage.setScene(scene);
                        stage.setResizable(false);

                        stage.showAndWait();

                        if (controller.isSaved()) {
                                applyBookingFilters();
                                bookingTable.refresh();
                                updateDashboardStats();
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        showDatabaseError(
                                        "Không thể mở cửa sổ sửa đặt phòng: "
                                                        + e.getMessage());
                }
        }

        @FXML
        private void handleCustomer() {
                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(
                                                        "/com/example/hotel/fxml/customer.fxml"));

                        mainLayout.setCenter(loader.load());

                } catch (Exception e) {
                        e.printStackTrace();
                        showDatabaseError(
                                        "Không thể mở trang quản lý khách hàng: "
                                                        + e.getMessage());
                }
        }

        private void updateDashboardStats() {
                DashboardStats stats = DashboardRepository.getDashboardStats();

                occupancyLabel.setText(
                                String.format("%.0f%%", stats.getOccupancyRate()));

                occupancyDetailLabel.setText(
                                stats.getOccupiedRooms()
                                                + " / "
                                                + stats.getTotalRooms()
                                                + " phòng đang có khách");

                checkInTodayLabel.setText(
                                String.valueOf(stats.getTodayCheckIns()));

                checkOutTodayLabel.setText(
                                String.valueOf(stats.getTodayCheckOuts()));

                NumberFormat formatter = NumberFormat.getCurrencyInstance(
                                Locale.forLanguageTag("vi-VN"));

                revenueTodayLabel.setText(
                                formatter.format(stats.getTodayRevenue()));
        }

        @FXML
        private void handleReport() {
                if (!UserSession.isAdmin()) {
                        showDatabaseError(
                                        "Bạn không có quyền truy cập báo cáo.");
                        return;
                }

                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(
                                                        "/com/example/hotel/fxml/report.fxml"));

                        mainLayout.setCenter(loader.load());

                } catch (Exception e) {
                        e.printStackTrace();

                        showDatabaseError(
                                        "Không thể mở trang báo cáo: " + e.getMessage());
                }
        }

        @FXML
        private void handleLogout() {

                Alert confirm = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "Bạn có chắc chắn muốn đăng xuất?",
                                ButtonType.YES,
                                ButtonType.NO);

                if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                        return;
                }

                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(
                                                        "/com/example/hotel/fxml/login.fxml"));

                        Scene scene = new Scene(loader.load(), 800, 500);

                        Stage stage = (Stage) mainLayout.getScene().getWindow();

                        stage.setTitle("SDZ Hotel - Đăng nhập");
                        UserSession.logout();
                        stage.setScene(scene);
                        stage.show();

                } catch (Exception e) {
                        e.printStackTrace();
                        showDatabaseError("Không thể quay về trang đăng nhập.");
                }
        }

        @FXML
        private void handleAccount() {

                if (!UserSession.isAdmin()) {
                        showDatabaseError(
                                        "Bạn không có quyền quản lý tài khoản.");
                        return;
                }

                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(
                                                        "/com/example/hotel/fxml/account.fxml"));

                        mainLayout.setCenter(loader.load());

                } catch (Exception e) {
                        e.printStackTrace();

                        showDatabaseError(
                                        "Không thể mở trang quản lý tài khoản: "
                                                        + e.getMessage());
                }
        }
}