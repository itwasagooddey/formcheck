package com.example.formcheck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class OverlayView extends View {

    private Pose pose;
    private Paint paint;

    private int imageWidth;
    private int imageHeight;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10f);
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

        drawLeg(canvas,
                pose.getPoseLandmark(PoseLandmark.LEFT_HIP),
                pose.getPoseLandmark(PoseLandmark.LEFT_KNEE),
                pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE));

        drawLeg(canvas,
                pose.getPoseLandmark(PoseLandmark.RIGHT_HIP),
                pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE),
                pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE));
    }

    private void drawLeg(Canvas canvas,
                         PoseLandmark hip,
                         PoseLandmark knee,
                         PoseLandmark ankle) {

        if (hip == null || knee == null || ankle == null) return;

        float scaleX = (float) getWidth() / imageHeight;
        float scaleY = (float) getHeight() / imageWidth;

        float hx = hip.getPosition().x * scaleX;
        float hy = hip.getPosition().y * scaleY;

        float kx = knee.getPosition().x * scaleX;
        float ky = knee.getPosition().y * scaleY;

        float ax = ankle.getPosition().x * scaleX;
        float ay = ankle.getPosition().y * scaleY;

        canvas.drawCircle(hx, hy, 20, paint);
        canvas.drawCircle(kx, ky, 20, paint);
        canvas.drawCircle(ax, ay, 20, paint);

        canvas.drawLine(hx, hy, kx, ky, paint);
        canvas.drawLine(kx, ky, ax, ay, paint);
    }
}