package com.example.formcheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ExerciseActivity extends AppCompatActivity {

    private static final String PREFS_AGREEMENT = "formcheck_agreement";
    private static final String KEY_AGREED = "agreed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_exercise);

        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button btnProgress = findViewById(R.id.btnProgress);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top + 16,
                    v.getPaddingRight(), bars.bottom + 16);
            return WindowInsetsCompat.CONSUMED;
        });

        List<Exercise> list = Arrays.asList(
                new Exercise(Exercise.SQUATS),
                new Exercise(Exercise.PUSHUPS),
                new Exercise(Exercise.PULLUPS)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExerciseAdapter(this, list));

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));

        SharedPreferences prefs = getSharedPreferences(PREFS_AGREEMENT, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_AGREED, false)) {
            showAgreementDialog(prefs);
        }
    }

    private void showAgreementDialog(SharedPreferences prefs) {
        new AlertDialog.Builder(this)
                .setTitle("Пользовательское соглашение")
                .setMessage(
                        "FormCheck — инструмент для самоконтроля техники упражнений.\n\n" +
                                "Приложение НЕ заменяет персонального тренера, врача или " +
                                "специалиста по реабилитации.\n\n" +
                                "Анализ основан на компьютерном зрении и угловых измерениях. " +
                                "Рекомендации носят информационный характер.\n\n" +
                                "При наличии травм или хронических заболеваний проконсультируйтесь " +
                                "с врачом перед началом тренировок.")
                .setCancelable(false)
                .setPositiveButton("Принимаю", (d, w) ->
                        prefs.edit().putBoolean(KEY_AGREED, true).apply())
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }
}
