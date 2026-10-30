package com.example.fitnessapp.model;

public class StatsOverview {
    private int totalWorkouts;
    private int workoutsLast7Days;
    private double totalVolume;
    private double avgVolume;
    private double currentWeekVolume;
    private double avgSets;
    private long avgDurationMs;

    public StatsOverview(int totalWorkouts, int workoutsLast7Days, double totalVolume, double avgVolume, double currentWeekVolume, double avgSets, long avgDurationMs) {
        this.totalWorkouts = totalWorkouts;
        this.workoutsLast7Days = workoutsLast7Days;
        this.totalVolume = totalVolume;
        this.avgVolume = avgVolume;
        this.currentWeekVolume = currentWeekVolume;
        this.avgSets = avgSets;
        this.avgDurationMs = avgDurationMs;
    }

    public int getTotalWorkouts() { return totalWorkouts; }
    public int getWorkoutsLast7Days() { return workoutsLast7Days; }
    public double getTotalVolume() { return totalVolume; }
    public double getAvgVolume() { return avgVolume; }
    public double getCurrentWeekVolume() { return currentWeekVolume; }
    public double getAvgSets() { return avgSets; }
    public long getAvgDurationMs() { return avgDurationMs; }
}