package com.kalu.pull;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lib.kalu.pull.widget.OnPullRefreshChangeListener;
import lib.kalu.pull.widget.PullRefreshLoadLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PullRefreshLoadLayout pullRefreshLoadLayout = (PullRefreshLoadLayout) findViewById(R.id.main_pull);
        pullRefreshLoadLayout.setOnPullRefreshChangeListener(new OnPullRefreshChangeListener() {
            @Override
            public void onPull(boolean refresh, float scrollY) {

                if (!refresh)
                    return;

                PullRefreshLoadLayout pullRefreshLoadLayout = (PullRefreshLoadLayout) findViewById(R.id.main_pull);
                pullRefreshLoadLayout.stopPull();
            }
        });
    }
}
