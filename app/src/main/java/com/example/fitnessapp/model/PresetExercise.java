package com.example.fitnessapp.model;

public class PresetExercise {
    private long id;
    private long presetId;
    private long exerciseId;
    private int orderIndex;
    
    // Additional info fetched from Exercise table for convenience
    private String name;
    private String muscleGroup;

    public PresetExercise(long id, long presetId, long exerciseId, int orderIndex, String name, String muscleGroup) {
        this.id = id;
        this.presetId = presetId;
        this.exerciseId = exerciseId;
        this.orderIndex = orderIndex;
        this.name = name;
        this.muscleGroup = muscleGroup;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getPresetId() { return presetId; }
    public void setPresetId(long presetId) { this.presetId = presetId; }
    public long getExerciseId() { return exerciseId; }
    public void setExerciseId(long exerciseId) { this.exerciseId = exerciseId; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
}