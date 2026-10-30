package com.example.fitnessapp.model;

public class TrainedExercise {
    private Long exerciseId;
    private String name;

    public TrainedExercise(Long exerciseId, String name) {
        this.exerciseId = exerciseId;
        this.name = name;
    }

    public Long getExerciseId() { return exerciseId; }
    public String getName() { return name; }
}