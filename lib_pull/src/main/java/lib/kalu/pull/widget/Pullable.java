package lib.kalu.pull.widget;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * description: 监听
 * created by kalu on 2017/1/13 18:01
 */
public interface Pullable {

    /**
     * 是否可以上拉刷新
     *
     * @param parent
     * @return
     */
    boolean canPull(@NonNull PullRefreshLoadLayout parent);

    void loadGif(@NonNull ImageView imageView, @DrawableRes int res);

    void stopGif(@NonNull ImageView imageView);
}