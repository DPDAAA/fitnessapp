package com.github.dpdaaa.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class session {
    
    private long id = 0;
    private LocalDate date;
    private LocalTime startzeit;
    private LocalTime endzeit;

    public session() {

        this.id = id++;
        this.date = LocalDate.now();
        this.startzeit = LocalTime.now();
        this.endzeit = LocalTime.now(); // am ende noch überschreiben
    }

    public long getId() {
        return this.id;
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
