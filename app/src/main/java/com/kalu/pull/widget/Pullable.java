package com.kalu.pull.widget;

import android.view.View;

/**
 * description: 监听
 * created by kalu on 2017/1/13 18:01
 */
public interface Pullable {

    int PULL_NORMAL = 1;    // 默认状态
    int PULL_DOWN_START = 2; // 下拉中
    int PULL_DOWN_RELEASEABLE = 3; // 可释放下拉
    int PULL_DOWN_RELEASE = 4;     // 已释放下拉

    /**
     * 是否可以上拉刷新
     *
     * @param parent
     * @param child
     * @return
     */
    boolean canRefresh(PullRefreshLoadLayout parent, View child);
}