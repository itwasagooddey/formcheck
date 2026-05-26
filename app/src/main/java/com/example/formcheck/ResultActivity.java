package com.example.formcheck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_result);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.resultRoot), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), bars.top + 16,
                            v.getPaddingRight(), bars.bottom + 16);
                    return WindowInsetsCompat.CONSUMED;
                });

        TextView tvExerciseName = findViewById(R.id.tvExerciseName);
        TextView tvReps = findViewById(R.id.tvReps);
        TextView tvMinAngle = findViewById(R.id.tvMinAngle);
        TextView tvAvgAngle = findViewById(R.id.tvAvgAngle);
        TextView tvRecommendation = findViewById(R.id.tvAdvice);
        TextView tvQuality = findViewById(R.id.tvQuality);
        Button btnAgain = findViewById(R.id.btnAgain);
        Button btnProgress = findViewById(R.id.btnProgress);

        String exerciseName = getIntent().getStringExtra("exercise");
        int reps = getIntent().getIntExtra("reps", 0);
        double minAngle = getIntent().getDoubleExtra("minAngle", 0);
        double avgAngle = getIntent().getDoubleExtra("avgAngle", 0);
        String recommendation = getIntent().getStringExtra("recommendation");

        tvExerciseName.setText(exerciseName != null ? exerciseName : "");
        tvReps.setText("Повторений: " + reps);
        tvMinAngle.setText("Минимальный угол: " + (int) minAngle + "°");
        tvAvgAngle.setText("Средний угол: " + (int) avgAngle + "°");
        tvRecommendation.setText(recommendation);

        // Итоговая оценка
        Exercise ex = new Exercise(exerciseName != null ? exerciseName : Exercise.SQUATS);
        if (minAngle <= ex.goodAngleMax) {
            tvQuality.setText("ХОРОШО ✓");
            tvQuality.setTextColor(0xFF00C853);
        } else if (minAngle <= ex.lowAngleMax) {
            tvQuality.setText("СРЕДНЕ");
            tvQuality.setTextColor(0xFFFFD600);
        } else {
            tvQuality.setText("НУЖНА РАБОТА");
            tvQuality.setTextColor(0xFFD50000);
        }

        btnAgain.setOnClickListener(v -> finish());

        btnProgress.setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressActivity.class));
        });
    }
}
