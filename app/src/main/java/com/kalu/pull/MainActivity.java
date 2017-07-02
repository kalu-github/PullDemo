package com.kalu.pull;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kalu.pull.adapter.BaseCommonAdapter;
import com.kalu.pull.adapter.holder.BaseViewHolder;
import com.kalu.pull.widget.OnPullRefreshListener;
import com.kalu.pull.widget.PullRefreshLoadLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private PullRefreshLoadLayout pull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pull = (PullRefreshLoadLayout) findViewById(R.id.main_tab_pull);
        recycler = (RecyclerView) findViewById(R.id.main_tab_recycler);

        pull.setOnPullRefreshListener(new OnPullRefreshListener() {
            @Override
            public void onPullDown() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pull.stopPull();
                    }
                }, 2000);
            }
        });

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i + "");
        }

        BaseCommonAdapter baseQuickAdapter = new BaseCommonAdapter<String, BaseViewHolder>(R.layout.activity_main_sub_item, list) {

            @Override
            protected void convert(final BaseViewHolder helper, final String result, int position) {
                helper.setText(R.id.main_tab_text, result);
            }
        };

        baseQuickAdapter.bindToRecyclerView(recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler.setAdapter(baseQuickAdapter);
    }
}
