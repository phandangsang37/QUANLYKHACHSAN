package com.example.hotel.controller;

import com.example.hotel.repository.UserRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import com.example.hotel.model.UserSession;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText(
                    "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        try {
            String role = UserRepository.authenticateAndGetRole(
                    username, password);

            boolean valid = "admin".equals(role)
                    || "staff".equals(role);

            if (!valid) {
                messageLabel.setText(
                        "Tên đăng nhập hoặc mật khẩu không đúng.");
                return;
            }
            UserSession.login(username, role);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/hotel/fxml/dashboard.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 750);

            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setTitle("SDZ Hotel - Dashboard");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();

            messageLabel.setText(
                    "Không thể đăng nhập. Vui lòng kiểm tra kết nối.");
        }
    }
}