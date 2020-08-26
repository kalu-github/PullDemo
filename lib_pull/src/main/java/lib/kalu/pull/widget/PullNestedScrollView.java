package lib.kalu.pull.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ScrollView;

public class PullNestedScrollView extends ScrollView implements Pullable {

    public PullNestedScrollView(Context context) {
        super(context);
    }

    public PullNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canPull(PullRefreshLoadLayout parent) {
        return getScrollY() == 0;
    }

    @Override
    public void loadGif(@NonNull ImageView imageView, int res) {

    }

    @Override
    public void stopGif(@NonNull ImageView imageView) {

    }
}
