package com.atu.videorecord;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atu.baselib.BaseActivity;
import com.atu.baselib.utils.FileUtils;
import com.atu.videorecord.app.VideoApplication;
import com.atu.videorecord.videorecord.OrientationSensorListener;
import com.atu.videorecord.videorecord.VideoProgressView;
import com.atu.videorecord.videorecord.VideoRecorder;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements VideoRecorder.OnRecordListener {

    @BindView(R.id.libVideoRecorder_btn_start)
    Button btnStart;
    @BindView(R.id.libVideoRecorder_fl)
    FrameLayout flVideoRecorder;
    @BindView(R.id.libVideoRecorder_progress)
    VideoProgressView vpProgress;
    @BindView(R.id.libVideoRecorder_tv_tips)
    TextView tvTips;
    @BindView(R.id.vr_surface)
    VideoRecorder videoRecorder;

    private SensorManager sm;
    private Sensor sensor;
    private OrientationSensorListener listener;
    private int iTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.CAMERA}, 5);
        }

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener();
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);

        if (!FileUtils.isFileExists(VideoApplication.DIR_VIDEO)) {
            FileUtils.createDirFile(VideoApplication.DIR_VIDEO);
        }

        btnStart.setOnTouchListener(new View.OnTouchListener() {

            private float moveY;
            private float moveX;
            Rect rect = new Rect();
            boolean isInner = true;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按住事件发生后执行代码的区域
                        tvTips.setVisibility(View.VISIBLE);
                        videoRecorder.startVideoRecord(MainActivity.this,listener.getOrientationHintDegrees());
                        vpProgress.startProgress(videoRecorder.mRecordMaxTime);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //移动事件发生后执行代码的区域
                        if (rect.right == 0 && rect.bottom == 0) {
                            btnStart.getFocusedRect(rect);
                        }
                        moveX = event.getX();
                        moveY = event.getY();
                        if (moveY > 0 && moveX > 0 && moveX <= rect.right && moveY <= rect.bottom) {
                            //内
                            isInner = true;
                            if (!"移开取消".equals(tvTips.getText().toString().trim())) {
                                tvTips.setBackgroundColor(Color.TRANSPARENT);
                                tvTips.setTextColor(Color.GREEN);
                                tvTips.setText("移开取消");
                            }
                            btnStart.setVisibility(View.INVISIBLE);
                        } else {
                            //外
                            isInner = false;
                            if (!"松开取消".equals(tvTips.getText().toString().trim())) {
                                tvTips.setBackgroundColor(Color.RED);//getResources().getColor(android.R.color.holo_red_dark));
                                tvTips.setTextColor(Color.WHITE);
                                tvTips.setText("松开取消");
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //松开事件发生后执行代码的区域
                        tvTips.setVisibility(View.INVISIBLE);
                        vpProgress.stopProgress();
                        if (iTime <= videoRecorder.mRecordMiniTime || (iTime < videoRecorder.mRecordMaxTime && !isInner)) {
                            if (isInner) {
                                Toast.makeText(MainActivity.this, "录制时间太短", Toast.LENGTH_SHORT).show();
                            } else {
                                //
                            }
                            videoRecorder.stopVideoRecord();
//                            videoRecorder.repCamera();
                            btnStart.setVisibility(View.VISIBLE);
                        } else if(iTime < videoRecorder.mRecordMaxTime){
                            videoRecorder.stop();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        sm.unregisterListener(listener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoRecorder.freeCameraResource();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoRecorder.stopVideoRecord();
    }

    @Override
    protected void onResume() {
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onRecordFinish() {
        if (vpProgress != null) {
            vpProgress.stopProgress();
        }
    }

    @Override
    public void onRecordProgress(int progress) {
        iTime = progress;
    }
}
