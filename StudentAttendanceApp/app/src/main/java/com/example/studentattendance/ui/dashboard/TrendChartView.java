package com.example.studentattendance.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.example.studentattendance.R;
import com.example.studentattendance.model.DailyTrend;
import com.example.studentattendance.utils.AttendancePolicy;
import com.example.studentattendance.utils.DateUtils;

/** Aqua line chart with a Royal Green threshold baseline and real date labels. */
public class TrendChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final List<DailyTrend> points = new ArrayList<>();
    private double threshold = AttendancePolicy.DEFAULT_LOW_THRESHOLD;

    public TrendChartView(Context context) {
        super(context);
        init();
    }

    public TrendChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrendChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int line = getResources().getColor(R.color.aqua, null);
        int label = getResources().getColor(R.color.text_primary, null);
        int secondary = getResources().getColor(R.color.text_secondary, null);
        int axis = getResources().getColor(R.color.divider, null);
        int thresholdColor = getResources().getColor(R.color.brand_secondary, null);
        linePaint.setColor(line);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        fillPaint.setColor(line);
        fillPaint.setAlpha(48);
        fillPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(line);
        pointPaint.setStyle(Paint.Style.FILL);
        axisPaint.setColor(axis);
        axisPaint.setStrokeWidth(2f);
        thresholdPaint.setColor(thresholdColor);
        thresholdPaint.setStrokeWidth(2.5f);
        labelPaint.setColor(label);
        labelPaint.setTextSize(24f);
        datePaint.setColor(secondary);
        datePaint.setTextSize(22f);
    }

    public void setTrend(List<DailyTrend> trend) {
        setTrend(trend, AttendancePolicy.DEFAULT_LOW_THRESHOLD);
    }

    public void setTrend(List<DailyTrend> trend, double thresholdPercent) {
        points.clear();
        if (trend != null) {
            points.addAll(trend);
        }
        this.threshold = thresholdPercent <= 0 ? AttendancePolicy.DEFAULT_LOW_THRESHOLD : thresholdPercent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }
        float padL = 48f;
        float padR = 20f;
        float padT = 28f;
        float padB = 44f;
        float usableH = h - padT - padB;
        canvas.drawLine(padL, padT, padL, h - padB, axisPaint);
        canvas.drawLine(padL, h - padB, w - padR, h - padB, axisPaint);
        canvas.drawText("100", 4f, padT + 12f, labelPaint);
        canvas.drawText("0", 8f, h - padB, labelPaint);

        float thresholdY = padT + usableH * (1f - (float) (threshold / 100.0));
        canvas.drawLine(padL, thresholdY, w - padR, thresholdY, thresholdPaint);
        canvas.drawText(String.format("%.0f%%", threshold), 4f, thresholdY - 6f, labelPaint);

        if (points.isEmpty()) {
            return;
        }
        float usableW = w - padL - padR;
        linePath.reset();
        Path fill = new Path();
        int last = Math.max(1, points.size() - 1);
        float[] xs = new float[points.size()];
        float[] ys = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            float x = points.size() == 1 ? padL + usableW / 2f : padL + (usableW * i / last);
            float y = padT + usableH * (1f - (float) (points.get(i).getAttendanceRate() / 100.0));
            xs[i] = x;
            ys[i] = y;
            if (i == 0) {
                linePath.moveTo(x, y);
                fill.moveTo(x, h - padB);
                fill.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fill.lineTo(x, y);
            }
        }
        fill.lineTo(xs[xs.length - 1], h - padB);
        fill.close();
        if (points.size() > 1) {
            canvas.drawPath(fill, fillPaint);
            canvas.drawPath(linePath, linePaint);
        }
        for (int i = 0; i < points.size(); i++) {
            canvas.drawCircle(xs[i], ys[i], 6f, pointPaint);
            if (shouldDrawValue(i)) {
                String value = String.format("%.0f%%", points.get(i).getAttendanceRate());
                float valueWidth = labelPaint.measureText(value);
                canvas.drawText(value, xs[i] - valueWidth / 2f, ys[i] - 10f, labelPaint);
            }
            if (shouldDrawDate(i)) {
                String label = shortDate(points.get(i).getDate());
                float labelWidth = datePaint.measureText(label);
                float labelX = Math.max(padL, Math.min(xs[i] - labelWidth / 2f, w - padR - labelWidth));
                canvas.drawText(label, labelX, h - 10f, datePaint);
            }
        }
    }

    private boolean shouldDrawValue(int index) {
        int size = points.size();
        if (size <= 6) {
            return true;
        }
        return index == 0 || index == size - 1 || index == size / 2;
    }

    private boolean shouldDrawDate(int index) {
        int size = points.size();
        if (size <= 4) {
            return true;
        }
        return index == 0 || index == size - 1 || index == size / 2;
    }

    private static String shortDate(String storageDate) {
        if (storageDate == null || storageDate.isEmpty()) {
            return "";
        }
        return DateUtils.format(DateUtils.toCalendar(storageDate).getTime(), "dd MMM");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (180 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(width, height);
    }
}
