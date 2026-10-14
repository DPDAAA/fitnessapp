package com.example.fitnessapp.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Session {
    private long id;
    private LocalDate date;
    private LocalTime startzeit;
    private LocalTime endzeit;

    public Session() {
        this.date = LocalDate.now();
        this.startzeit = LocalTime.now();
        this.endzeit = LocalTime.now();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public LocalTime getStartZeit() {
        return this.startzeit;
    }

    public LocalTime getEndZeit() {
        return this.endzeit;
    }

    public void setEndZeit() {
        this.endzeit = LocalTime.now();
    }
}
