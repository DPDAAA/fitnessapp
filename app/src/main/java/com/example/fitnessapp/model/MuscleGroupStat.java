package com.example.fitnessapp.model;

public class MuscleGroupStat {
    private String muscleGroup;
    private int workouts;
    private int sets;
    private double volume;
    private double volumePercentage;

    public MuscleGroupStat(String muscleGroup, int workouts, int sets, double volume) {
        this.muscleGroup = muscleGroup;
        this.workouts = workouts;
        this.sets = sets;
        this.volume = volume;
    }

    public String getMuscleGroup() { return muscleGroup; }
    public int getWorkouts() { return workouts; }
    public int getSets() { return sets; }
    public double getVolume() { return volume; }
    public double getVolumePercentage() { return volumePercentage; }
    public void setVolumePercentage(double volumePercentage) { this.volumePercentage = volumePercentage; }
}