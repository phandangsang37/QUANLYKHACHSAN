package com.example.hotel.model;

public class UserSession {

    private static String username;
    private static String role;

    public static void login(String username, String role) {
        UserSession.username = username;
        UserSession.role = role;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdmin() {
        return "admin".equals(role);
    }

    public static boolean isLoggedIn() {
        return username != null;
    }

    public static void logout() {
        username = null;
        role = null;
    }
}