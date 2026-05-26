package com.example.formcheck;

public class WorkoutRequest {
    public String exercise;
    public int reps;
    public double minAngle;
    public double avgAngle;
    public long timestamp;

    public WorkoutRequest(String exercise, int reps, double minAngle, double avgAngle) {
        this.exercise = exercise;
        this.reps = reps;
        this.minAngle = minAngle;
        this.avgAngle = avgAngle;
        this.timestamp = System.currentTimeMillis();
    }
}
