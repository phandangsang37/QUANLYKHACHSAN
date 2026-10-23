package com.example.hotel.model;

public class DashboardStats {

    private final int totalRooms;
    private final int occupiedRooms;
    private final int todayCheckIns;
    private final int todayCheckOuts;
    private final double todayRevenue;

    public DashboardStats(
            int totalRooms,
            int occupiedRooms,
            int todayCheckIns,
            int todayCheckOuts,
            double todayRevenue) {

        this.totalRooms = totalRooms;
        this.occupiedRooms = occupiedRooms;
        this.todayCheckIns = todayCheckIns;
        this.todayCheckOuts = todayCheckOuts;
        this.todayRevenue = todayRevenue;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public int getTodayCheckIns() {
        return todayCheckIns;
    }

    public int getTodayCheckOuts() {
        return todayCheckOuts;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public double getOccupancyRate() {
        if (totalRooms == 0) {
            return 0;
        }

        return occupiedRooms * 100.0 / totalRooms;
    }
}