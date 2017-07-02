package com.kalu.pull.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * description: 字体加粗
 * created by kalu on 2017/5/17 10:30
 */
@SuppressLint("AppCompatCustomView")
public class TextBoldView extends TextView {

    public TextBoldView(Context context) {
        this(context, null, 0);
    }

    public TextBoldView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextBoldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 解决TextView高度和textSize大小不一致
        // setIncludeFontPadding(false);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        getPaint().setFakeBoldText(true);
        super.setText(text, type);
    }
}
