package com.kalu.pull.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class PullLinearLayout extends LinearLayout implements Pullable {

    public PullLinearLayout(Context context) {
        super(context);
    }

    public PullLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canRefresh(PullRefreshLoadLayout parent, View child) {
        return true;
    }
}
