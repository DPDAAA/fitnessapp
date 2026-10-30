package com.example.fitnessapp.model;

import java.util.List;

public class ExerciseProgressEntry {
    private long sessionId;
    private long startTimeMs;
    private double avgWeight;
    private List<WorkoutSet> completedSets;

    public ExerciseProgressEntry(long sessionId, long startTimeMs, double avgWeight, List<WorkoutSet> completedSets) {
        this.sessionId = sessionId;
        this.startTimeMs = startTimeMs;
        this.avgWeight = avgWeight;
        this.completedSets = completedSets;
    }

    public long getSessionId() { return sessionId; }
    public long getStartTimeMs() { return startTimeMs; }
    public double getAvgWeight() { return avgWeight; }
    public List<WorkoutSet> getCompletedSets() { return completedSets; }
}