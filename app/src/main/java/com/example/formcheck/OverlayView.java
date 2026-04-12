package com.example.formcheck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.HashMap;
import java.util.Map;

public class OverlayView extends View {

    private Pose pose;
    private Paint paint;

    private int imageWidth;
    private int imageHeight;

    private Map<Integer, float[]> smoothedPoints = new HashMap<>();

    private final float ALPHA = 0.7f;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8f);
    }

    public void setPose(Pose pose, int width, int height) {
        this.pose = pose;
        this.imageWidth = width;
        this.imageHeight = height;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pose == null) return;

        drawLine(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW);
        drawLine(canvas, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST);

        drawLine(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE);
        drawLine(canvas, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE);

        drawLine(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW);
        drawLine(canvas, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST);

        drawLine(canvas, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE);
        drawLine(canvas, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE);

        drawLine(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        drawLine(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);

        drawLine(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP);
        drawLine(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP);
    }

    private void drawLine(Canvas canvas, int startType, int endType) {

        PoseLandmark start = pose.getPoseLandmark(startType);
        PoseLandmark end = pose.getPoseLandmark(endType);

        if (start == null || end == null) return;

        float[] startPoint = getSmoothedPoint(startType, start);
        float[] endPoint = getSmoothedPoint(endType, end);

        canvas.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1], paint);

        canvas.drawCircle(startPoint[0], startPoint[1], 10, paint);
        canvas.drawCircle(endPoint[0], endPoint[1], 10, paint);
    }

    private float[] getSmoothedPoint(int type, PoseLandmark landmark) {

        float scaleX = (float) getWidth() / imageHeight;
        float scaleY = (float) getHeight() / imageWidth;

        float rawX = landmark.getPosition().x * scaleX;
        float rawY = landmark.getPosition().y * scaleY;

        if (!smoothedPoints.containsKey(type)) {
            smoothedPoints.put(type, new float[]{rawX, rawY});
            return new float[]{rawX, rawY};
        }

        float[] last = smoothedPoints.get(type);

        float smoothX = ALPHA * last[0] + (1 - ALPHA) * rawX;
        float smoothY = ALPHA * last[1] + (1 - ALPHA) * rawY;

        smoothedPoints.put(type, new float[]{smoothX, smoothY});

        return new float[]{smoothX, smoothY};
    }
}