package lib.kalu.pull.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import lib.kalu.pull.R;
import lib.kalu.pull.util.PullUtil;

/**
 * description: 通用下拉刷新, 并触发相应事件的ViewGroup。
 * created by kalu on 2017/1/17 11:30
 */
public class PullRefreshLoadLayout extends ViewGroup {

    private final int PULL_STATUS_DEFAULT = 1;    // 默认状态
    private final int PULL_STATUS_READY = 2; // 开始下拉
    private final int PULL_STATUS_START = 3; // 下拉触发有效滑动距离
    private final int PULL_STATUS_REFRESH = 4;     // 释放刷新

    private int mPullCount = 1;
    private int mPullStatus = PULL_STATUS_DEFAULT;
    private final Scroller mScroller = new Scroller(getContext().getApplicationContext());

    private OnPullRefreshChangeListener onPullRefreshChangeListener; // 滑动回调监听

    /**********************************************************************************************/

    public PullRefreshLoadLayout(Context context) {
        super(context);
    }

    public PullRefreshLoadLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullRefreshLoadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PullRefreshLoadLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**********************************************************************************************/

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

        // 画子控件
        boolean drawChild = super.drawChild(canvas, child, drawingTime);

        Paint paint = new Paint();

        // 画文字
        drawText(canvas, paint);

        // 画时间
        drawTime(canvas, paint);

        // 画箭头
        drawArrow(canvas, paint);

        // 画转圈
        drawLoading(canvas, paint);
        startGif();

        return drawChild;
    }

    private void drawText(Canvas canvas, Paint paint) {

        paint.setFakeBoldText(false);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        // 文字大小
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);
        paint.setTextSize(size);

        // 最小滑动距离
        float min = generateMin();

        float x1 = getWidth() * 0.5f;
        float y1 = -min * 0.4f;

        // 正在刷新数据
        if (mPullStatus == PULL_STATUS_REFRESH) {
            canvas.drawText("正在刷新中...", x1, y1, paint);
        }
        // 松开立即刷新
        else if (mPullStatus == PULL_STATUS_START) {
            canvas.drawText("松开立即刷新", x1, y1, paint);
        }
        // 下拉开始刷新
        else {
            canvas.drawText("下拉可以刷新", x1, y1, paint);
        }
    }

    private void drawTime(Canvas canvas, Paint paint) {

        paint.setFakeBoldText(false);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        // 文字大小
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);
        paint.setTextSize(size);

        // 最小滑动距离
        float min = generateMin();

        long timeMillis;
        Object tag = getTag(getId());
        if (null == tag) {
            timeMillis = System.currentTimeMillis();
            setTag(getId(), timeMillis);
        } else {
            timeMillis = (long) tag;
        }
        float x2 = getWidth() * 0.5f;
        float y2 = -min * 0.15f;
        canvas.drawText("最后更新: " + PullUtil.foamatTimestampDisplay(timeMillis), x2, y2, paint);
    }

    private void drawArrow(Canvas canvas, Paint paint) {

        // 当前状态
        if (mPullStatus == PULL_STATUS_REFRESH || mPullStatus == PULL_STATUS_DEFAULT)
            return;

        // 子控件个数
        int count = getChildCount();
        if (count >= 2)
            return;

        // 是否图片
        boolean hasImage = false;

        // 图片
        if (hasImage) {

            // 图片1
            float radius = generateRadius();
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) radius, (int) radius);
            ImageView imageView1 = new ImageView(getContext().getApplicationContext());
            imageView1.setLayoutParams(layoutParams);
            imageView1.setImageResource(R.drawable.ic_pull_default);

            // 添加子控件
            addView(imageView1);

        }
        // 箭头
        else {

            paint.setFakeBoldText(true);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);

            // 最小滑动距离
            float min = generateMin();

            float startX = getWidth() * 0.26f;
            float startY = -min * 0.5f;
            float stopY = -min * 0.15f;
            float density = getResources().getDisplayMetrics().density;
            float stroke = 2 * density;
            float angle = 4 * density;
            paint.setStrokeWidth(stroke);
            canvas.drawLine(startX, startY, startX, stopY, paint);
            Path path = new Path();
            // 松开立即刷新
            if (mPullStatus == PULL_STATUS_START) {
                path.moveTo(startX - angle, startY + angle);
                path.lineTo(startX, startY - stroke / 2);
                path.lineTo(startX + angle, startY + angle);
            }
            // 下拉开始刷新
            else {
                path.moveTo(startX - angle, stopY - angle);
                path.lineTo(startX, stopY + stroke / 2);
                path.lineTo(startX + angle, stopY - angle);
            }
            canvas.drawPath(path, paint);
            canvas.save();
            canvas.restore();
        }
    }

    private void drawLoading(Canvas canvas, Paint paint) {

        // 当前状态
        if (mPullStatus != PULL_STATUS_REFRESH)
            return;

        // 是否图片
        boolean hasGif = false;

        // 动图
        if (hasGif) {
            startGif();
        }
        // 转圈
        else {

            // 循环次数
            if (mPullCount > 10) {
                mPullCount = 1;
            }
            ++mPullCount;

            // 画笔
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(0f);
            paint.setFakeBoldText(true);

            float x = getWidth() * 0.26f;
            float y = -generateMin() * 0.32f;
            float density = getResources().getDisplayMetrics().density;
            float radius = 10 * density;
            float v = 1.2f * density;
            float left = x - v;
            float right = x + v;
            float top = y - radius;
            float bottom = y - radius * 0.4f;
            float rx = radius * 0.4f;
            // LogUtil.e("lala", "x = " + x + ", y = " + y + ", radius = " + radius);

            // 椭圆
            for (int i = 0; i < 10; i++) {
                paint.setColor(i == mPullCount ? Color.RED : Color.BLACK);

//            ColorMatrix colorMatrix = new ColorMatrix(new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 10 / 360f * (i + count), 0,});//透明度过滤矩阵
//            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

                // RectF rectF = new RectF(left, top, right, bottom);
                // canvas.drawOval(rectF, paint);
                canvas.drawRoundRect(left, top, right, bottom, rx, rx, paint);
                canvas.save();
                canvas.rotate(36, x, y);
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int count = getChildCount();
        if (count > 3)
            throw new RuntimeException("下拉刷新控件, 子控件最多有3个");

        boolean hasPull = false;
        for (int i = 0; i < 3; i++) {

            View childAt = getChildAt(i);
            if (childAt instanceof Pullable) {
                hasPull = true;
            }

            // Gif
            if (childAt instanceof ImageView) {
                float radius = generateRadius();
                int measureSpecWidth = MeasureSpec.makeMeasureSpec((int) radius, MeasureSpec.EXACTLY);
                int measureSpecHeight = MeasureSpec.makeMeasureSpec((int) radius, MeasureSpec.EXACTLY);
                childAt.measure(measureSpecWidth, measureSpecHeight);
            }
            // Pullable
            else if (childAt instanceof Pullable) {
                int left = getPaddingLeft();
                int right = getPaddingRight();
                int top = getPaddingTop();
                int bottom = getPaddingBottom();
                int width = getMeasuredWidth();
                int height = getMeasuredHeight();
                int measureSpecWidth = MeasureSpec.makeMeasureSpec(width - left - right, MeasureSpec.EXACTLY);
                int measureSpecHeight = MeasureSpec.makeMeasureSpec(height - top - bottom, MeasureSpec.EXACTLY);
                childAt.measure(measureSpecWidth, measureSpecHeight);
            }
        }

        if (!hasPull)
            throw new RuntimeException("下拉刷新控件, 子控件必须实现Pullable接口");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int count = getChildCount();
        if (count > 3)
            throw new RuntimeException("下拉刷新控件, 子控件最多有3个");

        boolean hasPull = false;
        for (int i = 0; i < 3; i++) {

            View childAt = getChildAt(i);
            if (childAt instanceof Pullable) {
                hasPull = true;
            }

            // Gif
            if (childAt instanceof ImageView) {
                float x = getWidth() * 0.26f;
                float y = -generateMin() * 0.32f;
                float radius = generateRadius();
                float left = x - radius * 0.5f;
                float right = x + radius * 0.5f;
                float top = y - radius * 0.5f;
                float bottom = y + radius * 0.5f;
                childAt.layout((int) left, (int) top, (int) right, (int) bottom);
            }
            // Pullable
            else if (childAt instanceof Pullable) {
                int left = getPaddingLeft();
                int right = getPaddingRight();
                int top = getPaddingTop();
                int width = childAt.getMeasuredWidth();
                int height = childAt.getMeasuredHeight();
                childAt.layout(left, top, width - left - right, height + top);
            }
        }

        if (!hasPull)
            throw new RuntimeException("下拉刷新控件, 子控件必须实现Pullable接口");
    }

    /**********************************************************************************************/

    private float mLastMoveY;

    private float downX;
    private float downY;
    private float rangeX;
    private float rangeY;

    private float evX;
    private float evY;

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (null == mScroller)
            return;

        postInvalidate();
        if (!mScroller.computeScrollOffset()) {
            return;
        }

        int currY = mScroller.getCurrY();
        scrollTo(0, currY);

        if (null != onPullRefreshChangeListener) {
            onPullRefreshChangeListener.onPull(false, Math.abs(currY));
        }
    }

    /**********************************************************************************************/

    /**
     * 下拉刷新监听
     */
    public void setOnPullRefreshChangeListener(OnPullRefreshChangeListener onPullRefreshChangeListener) {
        this.onPullRefreshChangeListener = onPullRefreshChangeListener;
    }

    /***************************************************************************************/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        boolean enabled = isEnabled();
        if (!enabled)
            return false;

        // 当前ViewGroup是否拦截触摸事件
        boolean intercept = false;
        int y = (int) event.getY();
        // 判断触摸事件类型
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                // LogUtil.e("pullrefreshloadlayout", "onInterceptTouchEvent => action_down");

                downX = event.getX();
                downY = event.getY();

                evX = event.getX();
                evY = event.getY();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // LogUtil.e("pullrefreshloadlayout", "onInterceptTouchEvent => action_up");
                evX = 0f;
                evY = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                // LogUtil.e("pullrefreshloadlayout", "onInterceptTouchEvent => action_move");

                rangeX = event.getX() - downX;
                rangeY = event.getY() - downY;

                if (y == mLastMoveY || rangeY < rangeX)
                    break;

                boolean left = (evX - event.getX() > 10);
                boolean right = (event.getX() - evX > 10);
                boolean top = (evY - event.getY() > 10);
                boolean down = (event.getY() - evY > 10);

                float absY = Math.abs(event.getY() - evY);
                float absX = Math.abs(event.getX() - evX);

                // 有效滑动, 竖直滑动距离>水平滑动距离
                if (absX > absY)
                    break;

                intercept = canPull();
                break;
        }

        mLastMoveY = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                float y = event.getY();
                float scrollRange = mLastMoveY - y;
                mLastMoveY = y;
                onStartPull(scrollRange);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onStopPull();
                break;
        }
        return true;
    }

    private void onStartPull(float scroll) {

        // 触摸事件
        getParent().requestDisallowInterceptTouchEvent(true);

        // 滑动位移
        int scrollY = getScrollY();

        // 允许滑动到顶部继续向上滑动, scrollY = -getScrollY();
        // 容错：滑动顶部
        if (scrollY > 0) {
            onStopPull();
            return;
        }

        // 最小滑动距离
        float min = generateMin();

        // 已触发滑动有效距离
        if (Math.abs(getScrollY()) >= min) {
            mPullStatus = PULL_STATUS_START;
        }
        // 未触发有效滑动距离
        else {
            mPullStatus = PULL_STATUS_READY;
        }

        scroll /= 2f;
        scrollBy(0, (int) scroll);

        // 回调事件
        if (null != onPullRefreshChangeListener) {
            onPullRefreshChangeListener.onPull(false, Math.abs(getScrollY()));
        }
    }

    private void onStopPull() {

        // 刷新时间
        long currentTimeMillis = System.currentTimeMillis();
        setTag(getId(), currentTimeMillis);

        // 触摸事件
        getParent().requestDisallowInterceptTouchEvent(false);

        // 滑动位移
        int scrollY = getScrollY();

        // 最小滑动距离
        float min = generateMin();

        // 有效拉动行为, 已触发滑动有效距离
        boolean ok = (Math.abs(scrollY) >= min);
        if (ok) {
            setEnabled(false);
            mPullStatus = PULL_STATUS_REFRESH;
            mScroller.startScroll(0, scrollY, 0, (int) (-scrollY - min));
        }
        // 无效拉动行为, 未触发有效滑动距离
        else {
            setEnabled(true);
            mPullStatus = PULL_STATUS_DEFAULT;
            mScroller.startScroll(0, scrollY, 0, (int) -scrollY);
        }

        // 回调事件
        if (null != onPullRefreshChangeListener) {
            onPullRefreshChangeListener.onPull(ok, Math.abs(getScrollY()));
        }
    }

    public void stopPull() {

        // 延迟400ms
        postDelayed(new Runnable() {
            @Override
            public void run() {

                setEnabled(true);

                // 默认状态
                mPullStatus = PULL_STATUS_DEFAULT;

                // 清除Gif
                clearGif();

                // 位移复位
                int scrollY = getScrollY();
                mScroller.startScroll(0, scrollY, 0, -scrollY);

            }
        }, 800);
    }

    private float generateMin() {
        float density = getResources().getDisplayMetrics().density;
        float min = 80 * density;
        return min;
    }

    private float generateRadius() {
        float density = getResources().getDisplayMetrics().density;
        float radius = 24 * density;
        return radius;
    }

    public void autoRefresh() {

        final float v = -generateMin();
        ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(0, -v);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float temp = (float) animation.getAnimatedValue();
                if (temp == -v) {
                    onStopPull();
                    clearAnimation();
                } else {
                    onStartPull(temp - getScrollY());
                }
            }
        });
        animator.start();
    }

    private void startGif() {

        // 当前状态
        if (mPullStatus != PULL_STATUS_REFRESH)
            return;

        // 子控件个数
        int count = getChildCount();
        if (count >= 3)
            return;

        ImageView imageView1 = count == 1 ? null : (ImageView) getChildAt(1);
        ImageView imageView2 = new ImageView(getContext().getApplicationContext());

        // 图片1
        if (null != imageView1) {
            imageView1.setImageBitmap(null);
            imageView1.setImageDrawable(null);
            imageView1.setBackgroundColor(Color.TRANSPARENT);
            imageView1.setVisibility(View.INVISIBLE);
        }

        // 图片2
        if (null != imageView2) {
            float radius = generateRadius();
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) radius, (int) radius);
            imageView2.setLayoutParams(layoutParams);

            loadGif(imageView2, R.drawable.ic_pull_gif);
            // 添加子控件
            addView(imageView2);
        }
    }

    private void clearGif() {

        // 子控件个数
        int count = getChildCount();
        if (count != 3)
            return;

        ImageView imageView1 = count == 1 ? null : (ImageView) getChildAt(1);
        ImageView imageView2 = (ImageView) getChildAt(count == 1 ? 1 : 2);

        // 图片1
        if (null != imageView1) {
            imageView1.setImageBitmap(null);
            imageView1.setImageDrawable(null);
            removeView(imageView1);
        }

        // 图片2
        if (null != imageView2) {
            stopGif(imageView2);
            imageView2.setImageDrawable(null);
            removeView(imageView2);
        }
    }

    public boolean canPull() {

        View childAt = getChildAt(0);
        boolean ok = (null != childAt && (childAt instanceof Pullable) && ((Pullable) childAt).canPull(this));
        return ok;
    }

    private void loadGif(@NonNull ImageView imageView, @DrawableRes int res) {

        if (null == imageView)
            return;

        View childAt = getChildAt(0);
        if (null == childAt || !(childAt instanceof Pullable))
            return;

        Pullable pullable = (Pullable) childAt;
        pullable.loadGif(imageView, res);

    }

    private void stopGif(@NonNull ImageView imageView) {

        if (null == imageView)
            return;

        View childAt = getChildAt(0);
        if (null == childAt || !(childAt instanceof Pullable))
            return;

        Pullable pullable = (Pullable) childAt;
        pullable.stopGif(imageView);
    }
}