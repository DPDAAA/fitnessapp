package com.example.fitnessapp.model;


public class WorkoutSet {
    private long id;
    private long exerciseId;
    private long sessionId;
    private double weight;
    private int reps;

    public WorkoutSet(long exerciseId, long sessionId, double weight, int reps) {
        this.exerciseId = exerciseId;
        this.sessionId = sessionId;
        this.weight = weight;
        this.reps = reps;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExerciseId() {
        return this.exerciseId;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return this.reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }
}
