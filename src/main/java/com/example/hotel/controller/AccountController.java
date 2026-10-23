package com.example.hotel.controller;

import com.example.hotel.model.UserAccount;
import com.example.hotel.model.UserSession;
import com.example.hotel.repository.AccountRepository;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AccountController {

    @FXML
    private TableView<UserAccount> accountTable;

    @FXML
    private TableColumn<UserAccount, Long> userIdColumn;
    @FXML
    private TableColumn<UserAccount, String> usernameColumn;
    @FXML
    private TableColumn<UserAccount, String> roleColumn;
    @FXML
    private TableColumn<UserAccount, String> statusColumn;

    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private ComboBox<String> newRoleComboBox;

    @FXML
    private Label selectedAccountLabel;
    @FXML
    private ComboBox<String> selectedRoleComboBox;
    @FXML
    private PasswordField resetPasswordField;

    @FXML
    private Button changeRoleButton;
    @FXML
    private Button lockButton;
    @FXML
    private Button resetPasswordButton;

    @FXML
    private Label messageLabel;

    private final ObservableList<UserAccount> accounts = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        if (!UserSession.isAdmin()) {
            throw new SecurityException(
                    "Bạn không có quyền quản lý tài khoản.");
        }

        userIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(
                data.getValue().getUserId()));

        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUsername()));

        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRole()));

        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatusText()));

        accountTable.setItems(accounts);

        newRoleComboBox.getItems().addAll("staff", "admin");
        newRoleComboBox.setValue("staff");

        selectedRoleComboBox.getItems().addAll("staff", "admin");

        accountTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> updateSelection(selected));

        updateSelection(null);
        refreshAccounts();
    }

    private void refreshAccounts() {
        accounts.setAll(AccountRepository.getAccounts());

        accountTable.getSelectionModel().clearSelection();
        updateSelection(null);
    }

    private void updateSelection(UserAccount account) {

        boolean selected = account != null;

        changeRoleButton.setDisable(!selected);
        resetPasswordButton.setDisable(!selected);

        boolean isSelf = selected
                && account.getUsername().equals(
                        UserSession.getUsername());

        lockButton.setDisable(!selected || isSelf);

        if (!selected) {
            selectedAccountLabel.setText("Chưa chọn tài khoản");
            selectedRoleComboBox.setValue(null);
            lockButton.setText("Khóa tài khoản");
            return;
        }

        selectedAccountLabel.setText(
                "Đang chọn: " + account.getUsername());

        selectedRoleComboBox.setValue(account.getRole());

        lockButton.setText(
                account.isEnabled()
                        ? "Khóa tài khoản"
                        : "Mở khóa tài khoản");
    }

    private UserAccount getSelectedAccount() {
        UserAccount account = accountTable.getSelectionModel().getSelectedItem();

        if (account == null) {
            throw new IllegalStateException(
                    "Vui lòng chọn tài khoản trong bảng.");
        }

        return account;
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);

        messageLabel.setStyle(
                success
                        ? "-fx-text-fill: #198754;"
                        : "-fx-text-fill: #dc3545;");
    }

    @FXML
    private void handleCreateAccount() {

        try {
            AccountRepository.createAccount(
                    newUsernameField.getText(),
                    newPasswordField.getText(),
                    newRoleComboBox.getValue());

            newUsernameField.clear();
            newPasswordField.clear();
            newRoleComboBox.setValue("staff");

            refreshAccounts();

            showMessage("Tạo tài khoản thành công!", true);

        } catch (RuntimeException e) {
            e.printStackTrace();
            showMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void handleChangeRole() {

        try {
            UserAccount account = getSelectedAccount();
            String newRole = selectedRoleComboBox.getValue();

            if (newRole == null) {
                showMessage("Vui lòng chọn vai trò.", false);
                return;
            }

            if (newRole.equals(account.getRole())) {
                showMessage("Vai trò không thay đổi.", false);
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Đổi quyền của " + account.getUsername()
                            + " thành " + newRole + "?",
                    ButtonType.YES,
                    ButtonType.NO);

            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }

            AccountRepository.updateRole(
                    account.getUserId(), newRole);

            refreshAccounts();

            showMessage("Cập nhật quyền thành công!", true);

        } catch (RuntimeException e) {
            e.printStackTrace();
            showMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void handleToggleLock() {

        try {
            UserAccount account = getSelectedAccount();

            boolean newEnabled = !account.isEnabled();

            String action = newEnabled ? "mở khóa" : "khóa";

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Bạn có chắc muốn " + action
                            + " tài khoản "
                            + account.getUsername() + "?",
                    ButtonType.YES,
                    ButtonType.NO);

            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }

            AccountRepository.setEnabled(
                    account.getUserId(), newEnabled);

            refreshAccounts();

            showMessage(
                    newEnabled
                            ? "Mở khóa tài khoản thành công!"
                            : "Khóa tài khoản thành công!",
                    true);

        } catch (RuntimeException e) {
            e.printStackTrace();
            showMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void handleResetPassword() {

        try {
            UserAccount account = getSelectedAccount();

            AccountRepository.resetPassword(
                    account.getUserId(),
                    resetPasswordField.getText());

            resetPasswordField.clear();

            showMessage(
                    "Đã đặt lại mật khẩu cho "
                            + account.getUsername(),
                    true);

        } catch (RuntimeException e) {
            e.printStackTrace();
            showMessage(e.getMessage(), false);
        }
    }
}