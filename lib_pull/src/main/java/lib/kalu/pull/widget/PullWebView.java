package lib.kalu.pull.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ImageView;

public class PullWebView extends WebView implements Pullable {

    public PullWebView(Context context) {
        super(context);
    }

    public PullWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullWebView(Context context, AttributeSet attrs, int defStyleAttr) {
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
