package com.example.fitnessapp.model;

public class PlanExercise {
    private long id;
    private long dayId;
    private String name;
    private String muscleGroup;
    private int sets;
    private int reps;
    private double weight;

    public PlanExercise(long id, long dayId, String name, String muscleGroup, int sets, int reps, double weight) {
        this.id = id;
        this.dayId = dayId;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    public long getId() { return id; }
    public long getDayId() { return dayId; }
    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeight() { return weight; }
}