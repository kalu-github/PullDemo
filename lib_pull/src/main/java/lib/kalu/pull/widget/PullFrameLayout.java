package lib.kalu.pull.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PullFrameLayout extends FrameLayout implements Pullable {

    public PullFrameLayout(Context context) {
        super(context);
    }

    public PullFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
