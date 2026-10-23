package com.example.hotel.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.util.Locale;

import com.example.hotel.model.Booking;
import javafx.stage.Stage;

public class InvoiceController {
    @FXML
    private Label bookingIdLabel;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label roomNameLabel;
    @FXML
    private Label totalPriceLabel;

    public void setBooking(Booking booking) {
        bookingIdLabel.setText(booking.getBookingId());
        customerNameLabel.setText(booking.getCustomerName());
        roomNameLabel.setText(booking.getRoomName());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("vi-VN"));

        totalPriceLabel.setText(formatter.format(booking.getTotalPrice()));
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) bookingIdLabel.getScene().getWindow();
        stage.close();
    }
}