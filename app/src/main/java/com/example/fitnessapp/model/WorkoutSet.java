package com.example.fitnessapp.model;

public class WorkoutSet {
    private long id;
    private long sessionExerciseId;
    private int setNumber;
    private Integer reps; // Nullable
    private Double weight; // Nullable
    private boolean isCompleted;

    public WorkoutSet(long id, long sessionExerciseId, int setNumber, Integer reps, Double weight, boolean isCompleted) {
        this.id = id;
        this.sessionExerciseId = sessionExerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.isCompleted = isCompleted;
    }

    public WorkoutSet(long sessionExerciseId, int setNumber, Integer reps, Double weight, boolean isCompleted) {
        this.sessionExerciseId = sessionExerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.isCompleted = isCompleted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSessionExerciseId() { return sessionExerciseId; }
    public void setSessionExerciseId(long sessionExerciseId) { this.sessionExerciseId = sessionExerciseId; }
    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }
    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}