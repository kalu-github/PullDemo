package com.kalu.pull.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kalu.pull.R;

/**
 * description: 箭头
 * created by kalu on 2017/4/10 17:25
 */
public class ArrowView extends View {

    private final String TAG = ArrowView.class.getSimpleName();

    private int arrowColor; // 箭头颜色
    private int lineWidth; // 箭头宽度
    private int angleWidth; // 箭头宽度
    private int angleHeight; // 箭头宽度

    private boolean isTop;
    private boolean isBottom;
    private boolean isLeft;
    private boolean isRight;

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ArrowView);

        try {
            arrowColor = array.getColor(R.styleable.ArrowView_av_color, Color.BLACK);
            lineWidth = array.getDimensionPixelSize(R.styleable.ArrowView_av_line_width, 1);
            angleWidth = array.getDimensionPixelSize(R.styleable.ArrowView_av_angle_width, 1);
            angleHeight = array.getDimensionPixelSize(R.styleable.ArrowView_av_angle_height, 1);
            isTop = array.getBoolean(R.styleable.ArrowView_av_top, false);
            isBottom = array.getBoolean(R.styleable.ArrowView_av_bottom, false);
            isLeft = array.getBoolean(R.styleable.ArrowView_av_left, false);
            isRight = array.getBoolean(R.styleable.ArrowView_av_right, false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(arrowColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        float centerX = measuredWidth / 2f;

        // 1.画直线
        canvas.drawLine(centerX, lineWidth / 2, centerX, measuredHeight - lineWidth / 2, paint);

        // 2.画箭头
        Path path = new Path();

        if (isTop) {
            path.moveTo(centerX - angleWidth, angleHeight);
            path.lineTo(centerX, lineWidth / 2);
            path.lineTo(centerX + angleWidth, angleHeight);
        } else if (isBottom) {
            path.moveTo(centerX - angleWidth, measuredHeight - angleHeight);
            path.lineTo(centerX, measuredHeight - lineWidth / 2);
            path.lineTo(centerX + angleWidth, measuredHeight - angleHeight);
        }
        canvas.drawPath(path, paint);
        canvas.save();
        canvas.restore();
    }
}
