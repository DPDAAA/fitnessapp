package com.example.fitnessapp.model;

import java.util.List;

public class CalendarDay {
    private int day;
    private boolean isCurrentMonth;
    private List<SessionSummary> sessions;

    public CalendarDay(int day, boolean isCurrentMonth, List<SessionSummary> sessions) {
        this.day = day;
        this.isCurrentMonth = isCurrentMonth;
        this.sessions = sessions;
    }

    public int getDay() {
        return day;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public List<SessionSummary> getSessions() {
        return sessions;
    }
}