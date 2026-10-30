package com.example.fitnessapp.model;

public class Session {
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_COMPLETED = 1;

    private long id;
    private Long presetId; // Can be null
    private String startTime;
    private String endTime; // Can be null while active
    private int status;

    public Session(long id, Long presetId, String startTime, String endTime, int status) {
        this.id = id;
        this.presetId = presetId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Session(Long presetId, String startTime, String endTime, int status) {
        this.presetId = presetId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getPresetId() { return presetId; }
    public void setPresetId(Long presetId) { this.presetId = presetId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}