package com.atu.baselib;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by atu on 2018/3/22.
 */

public class BaseActivity extends AppCompatActivity {

    private Unbinder binder;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        binder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binder.unbind();
    }
}
