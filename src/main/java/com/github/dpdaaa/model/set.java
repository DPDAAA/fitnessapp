package com.github.dpdaaa.model;

public class set {

    private static long count = 0;
    private long id;
    String name;
    private int weight;
    private int reps;

    public set(String name, int weight, int reps) {

        this.id = count++;
        this.name = name;
        this.weight = weight;
        this.reps = reps;
        
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.id;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getReps() {
        return this.reps;
    }


    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setName(String name) {
        this.name = name;
    }


}
