package com.example.fitnessapp.model;

public class SessionSummary {
    private long sessionId;
    private String startTime;
    private String endTime;
    private int exerciseCount;
    private int completedSets;
    private double totalVolume;

    public SessionSummary(long sessionId, String startTime, String endTime, int exerciseCount, int completedSets, double totalVolume) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exerciseCount = exerciseCount;
        this.completedSets = completedSets;
        this.totalVolume = totalVolume;
    }

    public long getSessionId() { return sessionId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getExerciseCount() { return exerciseCount; }
    public int getCompletedSets() { return completedSets; }
    public double getTotalVolume() { return totalVolume; }
}