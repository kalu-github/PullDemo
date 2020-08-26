package lib.kalu.pull.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class PullTextView extends TextView implements Pullable {

    public PullTextView(Context context) {
        super(context);
    }

    public PullTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canPull(PullRefreshLoadLayout parent) {
        return true;
    }

    @Override
    public void loadGif(@NonNull ImageView imageView, int res) {

    }

    @Override
    public void stopGif(@NonNull ImageView imageView) {

    }
}
