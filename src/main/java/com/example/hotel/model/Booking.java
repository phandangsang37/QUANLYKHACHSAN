package com.example.hotel.model;

import java.time.LocalDate;

public class Booking {

    private String bookingId;
    private String customerName;
    private String roomName;
    private String stayDate;
    private double totalPrice;
    private String status;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long customerId;

    public Booking(String bookingId, String customerName,
            String roomName, String stayDate,
            double totalPrice, String status) {

        this.bookingId = bookingId;
        this.customerName = customerName;
        this.roomName = roomName;
        this.stayDate = stayDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;

    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getStayDate() {
        return stayDate;
    }

    public void setStayDate(String stayDate) {
        this.stayDate = stayDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
