package com.kalu.pull.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.kalu.pull.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * description: IOS 加载
 * created by kalu on 2017/4/24 13:38
 */
public class LoadingView extends View implements ViewTreeObserver.OnScrollChangedListener {

    private final String TAG = LoadingView.class.getSimpleName();

    private float roundRectfHeight = 2;
    private int roundRectCount = 10;
    private int progress = 0;
    private int pointerColor = Color.WHITE;

    private final Paint paint = new Paint();
    private Rect rect;

    private boolean isTreeObserver = false;

    /**********************************************************************************************/

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);

        try {
            pointerColor = array.getColor(R.styleable.LoadingView_lv_bar_color, Color.WHITE);
            roundRectCount = array.getInteger(R.styleable.LoadingView_lv_bar_num, roundRectCount);
            isTreeObserver = array.getBoolean(R.styleable.LoadingView_lv_tree_observer, false);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            array.recycle();
        }
    }

    /**********************************************************************************************/

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getWidth();

        canvas.save();

        //canvas的抗锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Path path = new Path();
        path.addCircle(width / 2, getHeight() / 2, width / 2, Path.Direction.CCW);//CCW 逆时针方向 CW 顺时针方向
        canvas.clipPath(path, Region.Op.REPLACE);

        canvas.translate(width / 2, getHeight() / 2);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(pointerColor);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(1f);
        paint.setFakeBoldText(true);

        if (progress > roundRectCount) {
            progress = 1;
        }
        roundRectfHeight = (float) (2 * Math.PI * width / 4) / (float) (roundRectCount * 3);
        canvas.rotate(360 / roundRectCount * (progress++), 0f, 0f);
        canvas.save();

        for (int i = 0; i < roundRectCount; i++) {

            RectF rectF = new RectF(width / 4, -roundRectfHeight, width / 2, roundRectfHeight);

            ColorMatrix colorMatrix = new ColorMatrix(new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, roundRectCount / 360f * (i + 1), 0,});//透明度过滤矩阵
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawRoundRect(rectF, roundRectfHeight, roundRectfHeight, paint);
            canvas.rotate(360 / roundRectCount, 0f, 0f);
            canvas.save();
        }
    }

    /*************************************************************************/

    @Override
    public void onScrollChanged() {

        if (null != rect) {
            boolean visibility = getGlobalVisibleRect(rect);
            setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {

        if (getVisibility() == View.VISIBLE) {
            startTimer();
        }

        if (isTreeObserver) {

            if (null == rect) {
                rect = new Rect();
            }

            getViewTreeObserver().addOnScrollChangedListener(this);
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {

        stopTimer();
        if (isTreeObserver) {
            getViewTreeObserver().removeOnScrollChangedListener(this);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {

        if (visibility != View.VISIBLE) {
            stopTimer();
        } else {
            startTimer();
        }

        super.onVisibilityChanged(changedView, visibility);
    }

    /**
     * 在Parent需要对此View作出调整的时候会触发onStartTemporaryDetach
     */
    @Override
    public void onStartTemporaryDetach() {

        startTimer();
        super.onStartTemporaryDetach();
    }

    /**
     * 在Parent需要对此View作出调整, 结束后调用onFinishTemporaryDetach
     */
    @Override
    public void onFinishTemporaryDetach() {

        stopTimer();
        super.onFinishTemporaryDetach();
    }

    /**********************************************************************************************/

    private Disposable timer;

    private void startTimer() {

        if (timer == null) {

            timer = Observable.interval(100, 100, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            Log.e(TAG, "startTimer[发送消息] ==> viewId = " + getId());
                            postInvalidate();
                        }
                    });
        } else if (timer.isDisposed()) {

            timer.dispose();
            timer = null;

            timer = Observable.interval(100, 100, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            Log.e(TAG, "startTimer[发送消息] ==> viewId = " + getId());
                            postInvalidate();
                        }
                    });
        }
    }

    private void stopTimer() {
        if (timer == null || timer.isDisposed())
            return;
        timer.dispose();
        timer = null;
        Log.e(TAG, "startTimer[清空消息] ==> viewId = " + getId());
    }

    public void setEnableScrollTreeObserver(boolean isTreeObserver) {
        this.isTreeObserver = isTreeObserver;
    }

    /*************************************************************************/
}