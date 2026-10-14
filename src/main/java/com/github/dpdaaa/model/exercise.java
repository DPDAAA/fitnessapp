package com.github.dpdaaa.model;

public class exercise {
    
    private static long counter = 0;
    private long id;
    private String name;
    private String muscleGroup; // Enum? 

    public exercise(String name, String muscleGroup) {
        this.id = counter++;        
        this.name = name;
        this.muscleGroup = muscleGroup;
    }


    public long getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }

    public String getMuscleGroup() {
        return this.muscleGroup;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }


} 
