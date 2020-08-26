package lib.kalu.pull.widget;

/**
 * description: 监听
 * created by kalu on 2017/1/13 18:01
 */
public interface OnPullRefreshChangeListener {

    void onPull(boolean refresh, float scrollY);
}
