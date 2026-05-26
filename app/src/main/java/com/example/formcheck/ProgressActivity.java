package com.example.formcheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProgressActivity extends AppCompatActivity {

    private RecyclerView rvProgress;
    private TextView tvEmpty, tvSummary, tvLoading;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_progress);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.progressRoot), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), bars.top + 16,
                            v.getPaddingRight(), bars.bottom + 16);
                    return WindowInsetsCompat.CONSUMED;
                });

        tvEmpty = findViewById(R.id.tvEmpty);
        tvSummary = findViewById(R.id.tvSummary);
        tvLoading = findViewById(R.id.tvLoading);
        rvProgress = findViewById(R.id.rvProgress);

        rvProgress.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(new ArrayList<>());
        rvProgress.setAdapter(adapter);

        loadFromServer();
    }

    private void loadFromServer() {
        tvLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getApi().getWorkouts().enqueue(new Callback<List<WorkoutRecord>>() {
            @Override
            public void onResponse(Call<List<WorkoutRecord>> call,
                                   Response<List<WorkoutRecord>> response) {
                tvLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<WorkoutRecord> records = response.body();
                    java.util.Collections.reverse(records);
                    showRecords(records);
                } else {
                    fallbackToLocal();
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutRecord>> call, Throwable t) {
                tvLoading.setVisibility(View.GONE);
                fallbackToLocal();
            }
        });
    }

    private void fallbackToLocal() {
        List<ProgressStorage.WorkoutRecord> local = ProgressStorage.getAll(this);
        List<WorkoutRecord> converted = new ArrayList<>();
        for (ProgressStorage.WorkoutRecord r : local) {
            WorkoutRecord wr = new WorkoutRecord();
            wr.exercise = r.exercise;
            wr.reps = r.reps;
            wr.minAngle = r.minAngle;
            wr.avgAngle = r.avgAngle;
            wr.timestamp = r.timestamp;
            converted.add(wr);
        }
        showRecords(converted);
    }

    private void showRecords(List<WorkoutRecord> records) {
        if (records.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProgress.setVisibility(View.GONE);
            return;
        }

        rvProgress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        int totalReps = 0, goodCount = 0;
        for (WorkoutRecord r : records) {
            totalReps += r.reps;
            Exercise ex = new Exercise(r.exercise != null ? r.exercise : Exercise.SQUATS);
            if (r.minAngle <= ex.goodAngleMax) goodCount++;
        }
        tvSummary.setText(String.format(Locale.getDefault(),
                "Тренировок: %d   Повторений: %d   Хороших: %d",
                records.size(), totalReps, goodCount));
        tvSummary.setVisibility(View.VISIBLE);

        adapter.setData(records);
    }

    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

        private List<WorkoutRecord> items;

        HistoryAdapter(List<WorkoutRecord> items) {
            this.items = items;
        }

        void setData(List<WorkoutRecord> data) {
            this.items = data;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            WorkoutRecord r = items.get(pos);
            h.tvDate.setText(r.formattedDate());
            h.tvExercise.setText(r.exercise != null ? r.exercise : "");
            h.tvReps.setText("Повторений: " + r.reps);
            h.tvAngle.setText(String.format(Locale.getDefault(),
                    "Мин. угол: %d°   Средний: %d°",
                    (int) r.minAngle, (int) r.avgAngle));

            String q = r.quality();
            h.tvQuality.setText(q);
            switch (q) {
                case "ХОРОШО":
                    h.tvQuality.setTextColor(0xFF00C853);
                    break;
                case "СРЕДНЕ":
                    h.tvQuality.setTextColor(0xFFFFD600);
                    break;
                default:
                    h.tvQuality.setTextColor(0xFFD50000);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvDate, tvExercise, tvReps, tvAngle, tvQuality;

            VH(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvHistDate);
                tvExercise = v.findViewById(R.id.tvHistExercise);
                tvReps = v.findViewById(R.id.tvHistReps);
                tvAngle = v.findViewById(R.id.tvHistAngle);
                tvQuality = v.findViewById(R.id.tvHistQuality);
            }
        }
    }
}
