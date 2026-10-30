package com.example.fitnessapp.model;

public class SessionExercise {
    private long id;
    private long sessionId;
    private Long exerciseId; // Can be null
    private String name;
    private String muscleGroup;
    private int orderIndex;

    public SessionExercise(long id, long sessionId, Long exerciseId, String name, String muscleGroup, int orderIndex) {
        this.id = id;
        this.sessionId = sessionId;
        this.exerciseId = exerciseId;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.orderIndex = orderIndex;
    }

    public SessionExercise(long sessionId, Long exerciseId, String name, String muscleGroup, int orderIndex) {
        this.sessionId = sessionId;
        this.exerciseId = exerciseId;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.orderIndex = orderIndex;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}