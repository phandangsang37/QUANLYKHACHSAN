package com.example.hotel.repository;

import com.example.hotel.database.Database;
import com.example.hotel.model.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CustomerRepository {

    private static final ObservableList<Customer> customers = FXCollections.observableArrayList();

    static {
        loadCustomers();
    }

    public static ObservableList<Customer> getCustomers() {
        return customers;
    }

    public static void loadCustomers() {
        String sql = """
                SELECT customer_id, full_name, phone, email, address
                FROM customers
                ORDER BY customer_id DESC
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            customers.clear();

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getLong("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"));

                customers.add(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách khách hàng", e);
        }
    }

    public static void addCustomer(Customer customer) {
        String sql = """
                INSERT INTO customers(full_name, phone, email, address)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setCustomerId(keys.getLong(1));
                }
            }

            customers.add(0, customer);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể thêm khách hàng", e);
        }
    }

    public static void updateCustomer(Customer customer) {
        String sql = """
                UPDATE customers
                SET full_name = ?, phone = ?, email = ?, address = ?
                WHERE customer_id = ?
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setLong(5, customer.getCustomerId());

            if (stmt.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Không tìm thấy khách hàng cần sửa.");
            }

            loadCustomers();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật khách hàng", e);
        }
    }

    public static void deleteCustomer(long customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, customerId);

            if (stmt.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Không tìm thấy khách hàng cần xóa.");
            }

            loadCustomers();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa khách hàng", e);
        }
    }
}