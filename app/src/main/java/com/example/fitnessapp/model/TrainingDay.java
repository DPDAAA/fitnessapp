package com.example.fitnessapp.model;

public class TrainingDay {
    private long id;
    private String dayName;
    private boolean isRest;

    public TrainingDay(long id, String dayName, boolean isRest) {
        this.id = id;
        this.dayName = dayName;
        this.isRest = isRest;
    }

    public long getId() { return id; }
    public String getDayName() { return dayName; }
    public boolean isRest() { return isRest; }
    public void setRest(boolean rest) { isRest = rest; }
}