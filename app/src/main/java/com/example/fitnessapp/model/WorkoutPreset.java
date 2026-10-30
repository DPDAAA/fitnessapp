package com.example.fitnessapp.model;

public class WorkoutPreset {
    private long id;
    private String name;
    private String createdAt;

    public WorkoutPreset(long id, String name, String createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public WorkoutPreset(String name, String createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}