package com.example.hotel.database;

import com.example.hotel.database.Database;
import java.sql.Connection;

public class TestDatabase {

    public static void main(String[] args) {

        try (Connection conn = Database.getConnection()) {

            System.out.println("Kết nối MariaDB thành công!");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
