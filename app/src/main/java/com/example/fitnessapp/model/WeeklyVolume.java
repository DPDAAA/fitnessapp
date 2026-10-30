package com.example.fitnessapp.model;

public class WeeklyVolume {
    private String weekLabel;
    private double volume;
    private int workouts;
    private int sets;

    public WeeklyVolume(String weekLabel, double volume, int workouts, int sets) {
        this.weekLabel = weekLabel;
        this.volume = volume;
        this.workouts = workouts;
        this.sets = sets;
    }

    public String getWeekLabel() { return weekLabel; }
    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
    public void addVolume(double addedVolume) { this.volume += addedVolume; }
    public int getWorkouts() { return workouts; }
    public void addWorkout() { this.workouts++; }
    public int getSets() { return sets; }
    public void addSets(int addedSets) { this.sets += addedSets; }
}