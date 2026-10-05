package com.example.studentattendance.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.studentattendance.R;

/**
 * Draws a ring that completes, then a checkmark — attendance recorded, ready.
 */
public class SplashAttendanceMarkView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ringBounds = new RectF();
    private final Path checkPath = new Path();
    private final Path checkSegment = new Path();
    private final PathMeasure checkMeasure = new PathMeasure();

    private float ringSweep;
    private float checkFraction;
    private AnimatorSet animator;

    public SplashAttendanceMarkView(Context context) {
        super(context);
        init();
    }

    public SplashAttendanceMarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SplashAttendanceMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float stroke = getResources().getDisplayMetrics().density * 3.5f;
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(stroke);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setColor(ContextCompat.getColor(getContext(), R.color.brand_primary));

        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(stroke);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);
        checkPaint.setColor(ContextCompat.getColor(getContext(), R.color.brand_secondary));
    }

    public float getRingSweep() {
        return ringSweep;
    }

    public void setRingSweep(float ringSweep) {
        this.ringSweep = ringSweep;
        invalidate();
    }

    public float getCheckFraction() {
        return checkFraction;
    }

    public void setCheckFraction(float checkFraction) {
        this.checkFraction = checkFraction;
        invalidate();
    }

    public void play() {
        cancel();
        ringSweep = 0f;
        checkFraction = 0f;
        invalidate();

        ObjectAnimator ring = ObjectAnimator.ofFloat(this, "ringSweep", 0f, 360f);
        ring.setDuration(520);
        ring.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator check = ObjectAnimator.ofFloat(this, "checkFraction", 0f, 1f);
        check.setDuration(280);
        check.setInterpolator(new DecelerateInterpolator());

        animator = new AnimatorSet();
        animator.playSequentially(ring, check);
        animator.start();
    }

    public void showComplete() {
        cancel();
        ringSweep = 360f;
        checkFraction = 1f;
        invalidate();
    }

    public void cancel() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = ringPaint.getStrokeWidth();
        ringBounds.set(inset, inset, w - inset, h - inset);
        checkPath.reset();
        checkPath.moveTo(w * 0.30f, h * 0.52f);
        checkPath.lineTo(w * 0.44f, h * 0.66f);
        checkPath.lineTo(w * 0.72f, h * 0.36f);
        checkMeasure.setPath(checkPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(ringBounds, -90f, ringSweep, false, ringPaint);
        if (checkFraction <= 0f || checkMeasure.getLength() == 0f) {
            return;
        }
        checkSegment.reset();
        checkMeasure.getSegment(0f, checkMeasure.getLength() * checkFraction, checkSegment, true);
        canvas.drawPath(checkSegment, checkPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancel();
        super.onDetachedFromWindow();
    }
}
