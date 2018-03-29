package com.atu.ffmpeglive.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by atu on 2018/3/28.
 */

public class MySurfaceView extends SurfaceView {

    private SurfaceHolder mHolder;

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHolder = getHolder();
    }

}
