package com.example.formcheck;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressStorage {

    private static final String PREFS = "formcheck_progress";
    private static final String KEY = "workouts";
    private static final int MAX = 100;

    public static void save(Context ctx, WorkoutRequest req) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        JSONArray arr = load(prefs);
        try {
            JSONObject obj = new JSONObject();
            obj.put("exercise", req.exercise);
            obj.put("reps", req.reps);
            obj.put("minAngle", req.minAngle);
            obj.put("avgAngle", req.avgAngle);
            obj.put("timestamp", req.timestamp);
            arr.put(obj);
            // Обрезаем если > MAX
            while (arr.length() > MAX) arr.remove(0);
            prefs.edit().putString(KEY, arr.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    public static List<WorkoutRecord> getAll(Context ctx) {
        JSONArray arr = load(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE));
        List<WorkoutRecord> result = new ArrayList<>();
        for (int i = arr.length() - 1; i >= 0; i--) {
            try {
                JSONObject o = arr.getJSONObject(i);
                result.add(new WorkoutRecord(
                        o.getString("exercise"),
                        o.getInt("reps"),
                        o.getDouble("minAngle"),
                        o.getDouble("avgAngle"),
                        o.getLong("timestamp")
                ));
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    private static JSONArray load(SharedPreferences prefs) {
        try {
            return new JSONArray(prefs.getString(KEY, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static class WorkoutRecord {
        public final String exercise;
        public final int reps;
        public final double minAngle;
        public final double avgAngle;
        public final long timestamp;

        WorkoutRecord(String e, int r, double mn, double av, long ts) {
            exercise = e;
            reps = r;
            minAngle = mn;
            avgAngle = av;
            timestamp = ts;
        }

        public String formattedDate() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
        }

        public String quality(Exercise ex) {
            if (minAngle <= ex.goodAngleMax) return "ХОРОШО";
            if (minAngle <= ex.lowAngleMax) return "СРЕДНЕ";
            return "ПЛОХО";
        }
    }
}
