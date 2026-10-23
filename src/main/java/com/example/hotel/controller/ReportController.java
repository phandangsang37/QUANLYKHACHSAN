package com.example.hotel.controller;

import com.example.hotel.repository.ReportRepository;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class ReportController {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label completedBookingsLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private BarChart<String, Number> revenueChart;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN"));

    @FXML
    private void initialize() {

        LocalDate today = LocalDate.now();

        // Mặc định báo cáo từ đầu tháng đến hôm nay
        fromDatePicker.setValue(today.withDayOfMonth(1));
        toDatePicker.setValue(today);

        handleViewReport();
    }

    @FXML
    private void handleViewReport() {

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null || toDate == null) {
            messageLabel.setText(
                    "Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
            return;
        }

        if (toDate.isBefore(fromDate)) {
            messageLabel.setText(
                    "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
            return;
        }

        try {
            double totalRevenue = ReportRepository.getTotalRevenue(
                    fromDate, toDate);

            int completedCount = ReportRepository.getCompletedBookingCount(
                    fromDate, toDate);

            Map<LocalDate, Double> dailyRevenue = ReportRepository.getDailyRevenue(
                    fromDate, toDate);

            // Hiển thị doanh thu
            totalRevenueLabel.setText(
                    currencyFormatter.format(totalRevenue));

            // Hiển thị số đơn hoàn thành
            completedBookingsLabel.setText(
                    String.valueOf(completedCount));

            // Xóa dữ liệu biểu đồ cũ
            revenueChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            series.setName("Doanh thu");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Map.Entry<LocalDate, Double> entry : dailyRevenue.entrySet()) {

                String dateLabel = entry.getKey().format(dateFormatter);

                series.getData().add(
                        new XYChart.Data<>(
                                dateLabel,
                                entry.getValue()));
            }

            revenueChart.getData().add(series);

            if (dailyRevenue.isEmpty()) {
                messageLabel.setText(
                        "Không có đơn đã trả phòng trong khoảng thời gian này.");
            } else {
                messageLabel.setText("");
            }

        } catch (RuntimeException e) {
            e.printStackTrace();

            messageLabel.setText(
                    "Không thể tải báo cáo từ MariaDB.");
        }
    }
}