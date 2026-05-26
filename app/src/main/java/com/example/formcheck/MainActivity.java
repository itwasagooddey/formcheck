package com.example.formcheck;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Image;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.video.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView tvExercise, tvAngle, tvStatus, tvRecIndicator;
    private MaterialButton btnRecord;

    private PoseDetector poseDetector;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    private boolean isRecording = false;
    private Exercise exercise;

    private int reps = 0;
    private boolean wasDown = false;
    private double minAngle = 180;
    private double angleSum = 0;
    private int angleFrames = 0;

    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        tvExercise = findViewById(R.id.tvExercise);
        tvAngle = findViewById(R.id.tvAngle);
        tvStatus = findViewById(R.id.tvStatus);
        btnRecord = findViewById(R.id.btnRecord);
        MaterialCardView infoCard = findViewById(R.id.infoCard);

        tvRecIndicator = new TextView(this);
        tvRecIndicator.setText("REC");
        tvRecIndicator.setTextColor(0xFFE53935);
        tvRecIndicator.setTextSize(13f);
        tvRecIndicator.setTypeface(null, android.graphics.Typeface.BOLD);
        tvRecIndicator.setVisibility(View.GONE);
        ((android.widget.FrameLayout) findViewById(R.id.root)).addView(tvRecIndicator);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);

        String exName = getIntent().getStringExtra("exercise");
        if (exName == null) exName = Exercise.SQUATS;
        exercise = new Exercise(exName);

        tvExercise.setText(exercise.name);
        overlayView.setExercise(exercise.name);

        ViewCompat.setOnApplyWindowInsetsListener(infoCard, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.widget.FrameLayout.LayoutParams lp =
                    (android.widget.FrameLayout.LayoutParams) v.getLayoutParams();
            lp.bottomMargin = bars.bottom + 40;
            v.setLayoutParams(lp);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(btnRecord, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.widget.FrameLayout.LayoutParams lp =
                    (android.widget.FrameLayout.LayoutParams) v.getLayoutParams();
            lp.bottomMargin = bars.bottom + 220;
            v.setLayoutParams(lp);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(tvRecIndicator, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.widget.FrameLayout.LayoutParams lp =
                    new android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.view.Gravity.TOP | android.view.Gravity.END);
            lp.topMargin = bars.top + 12;
            lp.rightMargin = 24;
            v.setLayoutParams(lp);
            return insets;
        });

        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE).build();
        poseDetector = PoseDetection.getClient(options);

        overlayView.setAngleListener((angle, status) -> {
            tvAngle.setText("Угол: " + (int) angle + "°");
            tvStatus.setText(status);

            if (isRecording) {
                angleSum += angle;
                angleFrames++;
                if (angle < minAngle) minAngle = angle;
                countRep(angle);
            }

            switch (status) {
                case "GOOD":
                    tvStatus.setTextColor(0xFF00C853);
                    break;
                case "LOW":
                    tvStatus.setTextColor(0xFFFFD600);
                    break;
                default:
                    tvStatus.setTextColor(0xFFD50000);
            }
        });

        btnRecord.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
                btnRecord.setText("REC");
                btnRecord.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.accent_green));
                tvRecIndicator.clearAnimation();
                tvRecIndicator.setVisibility(View.GONE);
            } else {
                startRecording();
                btnRecord.setText("СТОП");
                btnRecord.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.accent_red));
                tvRecIndicator.setVisibility(View.VISIBLE);
                AlphaAnimation blink = new AlphaAnimation(1f, 0f);
                blink.setDuration(600);
                blink.setRepeatMode(Animation.REVERSE);
                blink.setRepeatCount(Animation.INFINITE);
                tvRecIndicator.startAnimation(blink);
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE);
    }

    private void countRep(double angle) {
        if (angle < exercise.goodAngleMax && !wasDown) wasDown = true;
        if (angle > exercise.lowAngleMax && wasDown) {
            reps++;
            wasDown = false;
        }
    }

    private void startRecording() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120);
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120), 200);

        reps = 0;
        wasDown = false;
        minAngle = 180;
        angleSum = 0;
        angleFrames = 0;

        if (videoCapture == null) return;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                "formcheck_" + System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        MediaStoreOutputOptions opts = new MediaStoreOutputOptions.Builder(
                getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(values).build();

        recording = videoCapture.getOutput()
                .prepareRecording(this, opts)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), event -> {
                    if (event instanceof VideoRecordEvent.Start) isRecording = true;
                    if (event instanceof VideoRecordEvent.Finalize) isRecording = false;
                });
    }

    private void stopRecording() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 500);
        if (recording != null) {
            recording.stop();
            recording = null;
        }

        double avgAngle = angleFrames > 0 ? angleSum / angleFrames : 0;

        StringBuilder tips = new StringBuilder();
        if (minAngle > exercise.lowAngleMax) {
            tips.append("Недостаточная амплитуда: минимальный угол ")
                    .append((int) minAngle).append("°. Стремитесь к ")
                    .append(exercise.goodAngleMax).append("°.\n");
        } else if (minAngle > exercise.goodAngleMax) {
            tips.append("Неплохо, но можно глубже: угол ")
                    .append((int) minAngle).append("°. Цель: до ")
                    .append(exercise.goodAngleMax).append("°.\n");
        } else {
            tips.append("Отличная глубина: угол ").append((int) minAngle).append("°.\n");
        }

        if (avgAngle > exercise.lowAngleMax + 10) {
            tips.append("Средний угол высокий (").append((int) avgAngle)
                    .append("°) — замедли темп и контролируй каждое повторение.\n");
        }
        if (reps < 3) {
            tips.append("Мало зафиксированных повторений — убедись, что тело полностью в кадре.\n");
        }
        if (reps >= 8 && minAngle <= exercise.goodAngleMax) {
            tips.append("Стабильная техника на протяжении всего подхода.\n");
        }

        WorkoutRequest req = new WorkoutRequest(exercise.name, reps, minAngle, avgAngle);
        ProgressStorage.save(this, req);

        ApiClient.getApi().sendWorkout(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> r) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("reps", reps);
        intent.putExtra("minAngle", minAngle);
        intent.putExtra("avgAngle", avgAngle);
        intent.putExtra("recommendation", tips.toString());
        intent.putExtra("exercise", exercise.name);
        startActivity(intent);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyze);

                Recorder recorder = new Recorder.Builder().build();
                videoCapture = VideoCapture.withOutput(recorder);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, analysis, videoCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyze(ImageProxy proxy) {
        Image image = proxy.getImage();
        if (image == null) {
            proxy.close();
            return;
        }
        InputImage inputImage = InputImage.fromMediaImage(
                image, proxy.getImageInfo().getRotationDegrees());
        poseDetector.process(inputImage)
                .addOnSuccessListener(pose -> {
                    overlayView.setPose(pose, proxy.getWidth(), proxy.getHeight());
                    proxy.close();
                })
                .addOnFailureListener(e -> proxy.close());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) toneGenerator.release();
    }
}
