package com.atu.ffmpeglive;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.atu.baselib.BaseActivity;
import com.atu.ffmpeglive.ffmpeg.FFmpegHandle;
import com.atu.ffmpeglive.view.MySurfaceView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "VIDEO_LIVE";
    @BindView(R.id.sv)
    MySurfaceView mSurfaceView;
    @BindView(R.id.btn_start)
    Button btnStart;

    private Camera mCamera;
    private int screenWidth = 320;
    private int screenHeight = 240;
    private SurfaceHolder mHolder;
    boolean isPreview = false; // 是否在浏览中
    public final static String RTMP_URL = "rtmp://192.168.13.127:1935/live/";
    private boolean isPlay;


    @OnClick(R.id.btn_start)
    void onClick(View view) {
        if (isPlay) {
            isPlay = false;
            btnStart.setText("开始");
        }else {
            isPlay = true;
            btnStart.setText("暂停");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 5);
        }
        initView();
    }

    private void initView() {
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        FFmpegHandle.getInstance().initVideo(RTMP_URL);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = openCamera();
            if (mHolder != null) {
                setStartPreview(mCamera,mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     *
     *@author : Atu
     *@method : openCamera()
     *@param :
     *@return : Camera
     *@description : 打开摄像头，配置参数
     *create at: 2018/3/28 上午10:14
     */
    private Camera openCamera() {
        Camera camera;
        try {
            camera = Camera.open(1);
            if (camera != null && !isPreview) {
                // 参数配置
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(screenWidth,screenHeight);
                parameters.setPreviewFpsRange(30000,30000);
                parameters.setPictureSize(screenWidth,screenHeight);
                parameters.setPictureFormat(ImageFormat.NV21);
                camera.setParameters(parameters);
                // 指定预览 surfaceView
                camera.setPreviewDisplay(mSurfaceView.getHolder());
                camera.setPreviewCallback(new StreamCallback());
                camera.startPreview();
                isPreview = true;
            }
        } catch (Exception e) {
            camera = null;
            Log.d(TAG,"无法打开摄像头");
        }

        return camera;
    }

    private void setStartPreview(Camera camera ,SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            followScreenOrientation(this,camera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = openCamera();
        }
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    public static void followScreenOrientation(Context context, Camera camera) {
        final int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(270);
        }
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private class StreamCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            if (isPlay) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"length:" + data.length);
                        FFmpegHandle.getInstance().onFrameCallback(data);
                    }
                });
            }


        }
    }
}
