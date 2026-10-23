package com.example.hotel.model;

public class UserAccount {

    private final long userId;
    private final String username;
    private final String role;
    private final boolean enabled;

    public UserAccount(long userId, String username,
            String role, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStatusText() {
        return enabled ? "Hoạt động" : "Đã khóa";
    }
}