package com.kalu.pull.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

import com.kalu.pull.DateUtil;
import com.kalu.pull.R;

/**
 * description: 通用下拉刷新, 并触发相应事件的ViewGroup。
 * created by kalu on 2017/1/17 11:30
 */
public class PullRefreshLoadLayout extends ViewGroup {

    private final String TAG = PullRefreshLoadLayout.class.getSimpleName();

    private final float DECELERATE_INTERPOLATION_FACTOR = 2f; // 滑动阻力因子

    private int currentState = Pullable.PULL_NORMAL; // 视图当前状态
    private OnPullRefreshChangeListener onPullRefreshChangeListener; // 滑动回调监听
    private OnPullRefreshListener onPullRefreshListener; // 滑动回调监听

    private View sub;  // 触发滑动手势的目标View
    private View pullHead; // 滑动头部

    private ArrowView pullArrowDown;   // 下拉状态指示器(箭头)
    private LoadingView pullProgressDown;// 下拉加载进度条(圆形)
    private TextView pullHeadText; // 下拉状态文本指示
    private TextView pullTimeText; // 下拉最后更新时间

    private Scroller scroller;  // 用于平滑滑动的Scroller对象
    private int pullColor; // 拉动部分背景(color|

    private int effectiveRange;    // 使拉动回调生效(触发)的滑动距离

    private String pullTime;

    private final ObjectAnimator arrowAnimator = new ObjectAnimator();

    /**********************************************************************************************/

    public PullRefreshLoadLayout(Context context) {
        this(context, null, 0);
    }

    public PullRefreshLoadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshLoadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float scale = context.getResources().getDisplayMetrics().density;
        // int temp = (int) (20 * scale + 0.5f);
        effectiveRange = (int) (60 * scale + 0.5f);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshLoadLayout);
        try {
            pullColor = array.getColor(R.styleable.PullRefreshLoadLayout_prll_pull_color, getResources().getColor(R.color.white));
        } finally {
            array.recycle();
        }

        scroller = new Scroller(context);

        pullTime = "最后更新: " + DateUtil.foamatTimestampDisplay(System.currentTimeMillis());
    }

    /**********************************************************************************************/

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // 下拉刷新
        pullHead = LayoutInflater.from(getContext()).inflate(R.layout.layout_custom_pull_header, null);
        pullHead.setBackgroundColor(pullColor);
        pullHeadText = (TextView) pullHead.findViewById(R.id.custom_pull_header_hint);
        pullTimeText = (TextView) pullHead.findViewById(R.id.custom_pull_header_time);
        pullArrowDown = (ArrowView) pullHead.findViewById(R.id.custom_pull_header_arrow);
        pullProgressDown = (LoadingView) pullHead.findViewById(R.id.custom_pull_header_loading);
        this.addView(pullHead, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (sub == null) {
            sub = getChildAt(1);
        }

        if (sub == null) return;

        for (int i = 0; i < getChildCount(); i++) {

            int widthTemp = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int widthMeasureSpecTemp = MeasureSpec.makeMeasureSpec(widthTemp, MeasureSpec.EXACTLY);

            int heightTemp = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            int heightMeasureSpecTemp = MeasureSpec.makeMeasureSpec(heightTemp, MeasureSpec.EXACTLY);

            getChildAt(i).measure(widthMeasureSpecTemp, heightMeasureSpecTemp);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == pullHead) { // 头视图隐藏在顶端
                child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else {
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }

    /**********************************************************************************************/

    private void rotateArrow() {

        if (null == pullArrowDown) return;

        float rotation = pullArrowDown.getRotation();
        // LogUtil.e(TAG, "rotateArrow ==> rotation = " + rotation);

        boolean isReset = (rotation == 0.0f);

        if (arrowAnimator.isRunning()) {
            arrowAnimator.cancel();
            arrowAnimator.end();
            pullArrowDown.setRotation(isReset ? 0f : -180f);
        }

        arrowAnimator.setPropertyName("rotation");
        arrowAnimator.setFloatValues(isReset ? 0f : -180f, isReset ? -180f : 0f);
        arrowAnimator.setDuration(250);
        arrowAnimator.setTarget(pullArrowDown);
        arrowAnimator.start();
    }

    private float mLastMoveY;

    private float downX;
    private float downY;
    private float rangeX;
    private float rangeY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        // 当前ViewGroup是否拦截触摸事件
        boolean intercept = false;
        int y = (int) event.getY();
        // 判断触摸事件类型
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                intercept = false;

                downX = event.getX();
                downY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                rangeX = event.getX() - downX;
                rangeY = event.getY() - downY;

                if (sub == null || y == mLastMoveY || rangeY < rangeX) break;

                intercept = canPullDown();
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }

        mLastMoveY = y;
        return intercept;
    }

    /**
     * onInterceptTouchEvent return ture
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float y = event.getY();
                float scrollRange = mLastMoveY - y;
                mLastMoveY = y;
                updataScroll(scrollRange);
                getParent().requestDisallowInterceptTouchEvent(true);

                if (null != onPullRefreshChangeListener && !isAutoRefresh) {
                    onPullRefreshChangeListener.onPullChange(true, Math.abs(getScrollY()));
                }

                break;
            case MotionEvent.ACTION_UP:
                onStopScroll();
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (null == scroller) return;

        postInvalidate();
        if (!scroller.computeScrollOffset()) {
            return;
        }

        int currY = scroller.getCurrY();
        scrollTo(0, currY);

        if (null != onPullRefreshChangeListener && !isAutoRefresh) {
            onPullRefreshChangeListener.onPullChange(false, Math.abs(currY));
        }
    }

    private void updataScroll(float scrollY) {
        // LogUtil.e(TAG, "updataScroll ==> scrollY = " + scrollY);

        // 下拉刷新
        if (scrollY < 0) {

            if (getScrollY() > 0) {

                // 此判断意味是在进行上拉操作的过程中，进行的下拉操作(可能代表用户视图取消此次上拉)
                if (getScrollY() < effectiveRange && Math.abs(scrollY) > getScrollY()) {
                    scrollY = -getScrollY();
                }
            } else {
                if (currentState > Pullable.PULL_DOWN_RELEASEABLE)
                    return;

                if (Math.abs(getScrollY()) >= effectiveRange) { // 当下拉已经达到有效距离，则为滑动添加阻力
                    scrollY /= DECELERATE_INTERPOLATION_FACTOR;
                    if (currentState != Pullable.PULL_DOWN_RELEASEABLE)
                        updateState(Pullable.PULL_DOWN_RELEASEABLE);
                } else {

                    if (currentState != Pullable.PULL_DOWN_START) {
                        updateState(Pullable.PULL_DOWN_START);
                    }
                }
            }
        }

        scrollY /= DECELERATE_INTERPOLATION_FACTOR;
        scrollBy(0, (int) scrollY);
    }

    private void onStopScroll() {

        final int scrollY = getScrollY();

        // 有效拉动行为
        if (Math.abs(scrollY) >= effectiveRange) {

            // 有效下拉行为
            if (scrollY < 0) {

                scroller.startScroll(0, scrollY, 0, -(scrollY + effectiveRange));
                updateState(Pullable.PULL_DOWN_RELEASE);
            }
        }
        // 无效拉动行为
        else {
            updateState(Pullable.PULL_NORMAL);
        }
    }

    private void updateState(int state) {
        // LogUtil.e(TAG, "updateState ==> state = " + state);

        switch (state) {
            case Pullable.PULL_NORMAL:

                if (null != pullProgressDown) {
                    pullProgressDown.setVisibility(View.GONE);
                }

                final int scrollY = getScrollY();
                scroller.startScroll(0, scrollY, 0, -scrollY);
                break;
            case Pullable.PULL_DOWN_START:

                if (null != pullHeadText) {
                    pullHeadText.setText(R.string.txt_pull_refresh_normal);
                }

                if (null != pullTimeText) {
                    pullTimeText.setText(pullTime);
                }

                if (null != pullArrowDown) {
                    pullArrowDown.setVisibility(View.VISIBLE);

                    if (currentState != Pullable.PULL_NORMAL) {
                        rotateArrow();
                    }
                }
                break;
            case Pullable.PULL_DOWN_RELEASEABLE:

                if (null != pullArrowDown) {
                    rotateArrow();
                }

                if (null != pullHeadText) {
                    pullHeadText.setText(R.string.txt_pull_refresh_release);
                }
                break;
            case Pullable.PULL_DOWN_RELEASE:

                if (null != pullHeadText) {
                    pullHeadText.setText(R.string.txt_pull_refresh_loading);
                }

                if (null != pullProgressDown) {
                    pullProgressDown.setVisibility(View.VISIBLE);
                }

                if (null != pullArrowDown) {
                    pullArrowDown.setVisibility(View.GONE);
                }

                if (onPullRefreshChangeListener != null && !isAutoRefresh) {
                    onPullRefreshChangeListener.onPullDown();
                }

                if (onPullRefreshListener != null && !isAutoRefresh) {
                    onPullRefreshListener.onPullDown();
                }

                break;
        }

        currentState = state;
    }

    /**********************************************************************************************/

    /**
     * 下拉刷新监听
     */
    public void setOnPullRefreshChangeListener(OnPullRefreshChangeListener onPullRefreshChangeListener) {
        this.onPullRefreshChangeListener = onPullRefreshChangeListener;
    }

    /**
     * 下拉刷新监听
     */
    public void setOnPullRefreshListener(OnPullRefreshListener onPullRefreshListener) {
        this.onPullRefreshListener = onPullRefreshListener;
    }

    /**********************************************************************************************/

    public void stopPull() {

        if (null != pullHeadText) {
            pullHeadText.setText(R.string.txt_pull_refresh_success);

            pullTime = "最后更新: " + DateUtil.foamatTimestampDisplay(System.currentTimeMillis());
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateState(Pullable.PULL_NORMAL);
            }
        }, 1000);
    }

    public void setHeadBackgroundColor(int color) {

        if (null == pullHead) return;
        pullHead.setBackgroundColor(color);
    }

    public boolean canPullDown() {

        if (sub == null || !(sub instanceof Pullable)) return false;
        return ((Pullable) sub).canRefresh(this, sub);
    }

    private final ValueAnimator animator = new ValueAnimator();
    private final int end = -50;

    private boolean isAutoRefresh = false;

    public void autoRefresh() {

        if (null == animator || animator.isRunning() || animator.isStarted()) return;

        isAutoRefresh = true;
        animator.setFloatValues(0, end);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float temp = (float) animation.getAnimatedValue();
                if (temp == end) {
                    onStopScroll();
                    getParent().requestDisallowInterceptTouchEvent(false);
                    clearAnimation();
                } else {
                    updataScroll(temp - getScrollY());
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
        });
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                isAutoRefresh = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAutoRefresh = false;
            }

            @Override
            public void onAnimationPause(Animator animation) {
                isAutoRefresh = false;
            }
        });
    }
}