package lib.kalu.pull.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
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
    public boolean canPull(PullRefreshLoadLayout parent) {
        View childAt = getChildAt(1);
        return childAt.getScrollY() == 0;
    }

    @Override
    public void loadGif(@NonNull ImageView imageView, int res) {

    }

    @Override
    public void stopGif(@NonNull ImageView imageView) {

    }
}
