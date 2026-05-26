package com.example.formcheck;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutRecord {
    public Long id;
    public String exercise;
    public int reps;
    public double minAngle;
    public double avgAngle;
    public long timestamp;

    public String formattedDate() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }

    public String quality() {
        Exercise ex = new Exercise(exercise != null ? exercise : Exercise.SQUATS);
        if (minAngle <= ex.goodAngleMax) return "ХОРОШО";
        if (minAngle <= ex.lowAngleMax) return "СРЕДНЕ";
        return "ПЛОХО";
    }
}
