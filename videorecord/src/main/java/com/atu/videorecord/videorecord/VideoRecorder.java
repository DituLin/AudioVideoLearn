package com.atu.videorecord.videorecord;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.atu.baselib.utils.FileUtils;
import com.atu.videorecord.app.VideoApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频录制
 * Created by atu on 2018/3/23.
 */

public class VideoRecorder extends SurfaceView implements SurfaceHolder.Callback, MediaRecorder.OnErrorListener {

    private Camera mCamera;
    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    private File mRecordFile;//存储的路径
    private MediaRecorder mMediaRecorder;
    private int mTimeCount = 1;//开启时间
    public final int mRecordMaxTime = 6;//最大时间
    public final int mRecordMiniTime = 2;//最小时间
    private Timer mTimer;
    private OnRecordListener mOnRecordListener;
    private String basePath = VideoApplication.DIR_VIDEO;//默认路径
    private Camera.Size size;


    public VideoRecorder(Context context) {
        this(context, null);
    }

    public VideoRecorder(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoRecorder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        initCamera();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio = 1f * size.height / size.width;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / ratio);
        int wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(wms, hms);
    }

    private void initCamera() {
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open();
            if (mCamera == null) {
                return;
            }
            initCameraParams();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
//            mCamera.unlock();
        } catch (IOException e) {
            e.printStackTrace();
            freeCameraResource();
        }

    }

    /**
     * 初始化 camera 参数
     */
    private void initCameraParams() {
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        parameters.setPictureSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        parameters.setPictureFormat(ImageFormat.NV21);
        size = parameters.getPreviewSize();
        List<String> foucsModes = parameters.getSupportedFocusModes();
        if (foucsModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);
    }


    /**
     * 释放 camera 资源
     */
    public void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mSurfaceHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }


    /**
     * @param :
     * @return :
     * @author : Atu
     * @method :
     * @description :
     * create at: 2018/3/23 下午2:44
     */
    public void stop() {
        stopVideoRecord();
        releaseVideoRecord();
//        freeCameraResource();
    }

    public void repCamera() {
        initCamera();
    }


    /**
     *
     *@author : Atu
     *@method :
     *@param :
     *@return :
     *@description :
     *create at: 2018/3/23 下午3:15
     */
    private void initVideoRecord(int orientationHintDegrees) {
        try {
            if (mMediaRecorder == null) {
                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setOnErrorListener(this);
            } else {
                mMediaRecorder.reset();
            }
            mCamera.unlock();
            //1.设置摄像头解锁，和MediaRecorder
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            //2.设置视频源
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置视频输出的格式和编码
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

            mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mMediaRecorder.setAudioEncodingBitRate(44100);
            if (mProfile.videoBitRate > 2 * 1024 * 1024) {
                mMediaRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
            } else {
                mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            }
            mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (orientationHintDegrees != 0 &&
                    orientationHintDegrees != 90 &&
                    orientationHintDegrees != 180 &&
                    orientationHintDegrees != 270) {
                orientationHintDegrees = 90;
            }
            mMediaRecorder.setOrientationHint(orientationHintDegrees);
            mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        if (mr != null) {
            mr.reset();
        }
    }


    public void startVideoRecord(OnRecordListener listener ,int orientationHintDegrees) {
        this.mOnRecordListener =listener;
        createRecordDir();
        initVideoRecord(orientationHintDegrees);
        mTimeCount = 1;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 时间计数器重新赋值
                mOnRecordListener.onRecordProgress(mTimeCount);
                if (mTimeCount >= mRecordMaxTime) {
                    stop();
                }
                mTimeCount++;

            }
        },0,1000);

    }

    public void stopVideoRecord() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mCamera.lock();
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
            }
        }
    }

    public void releaseVideoRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    private void createRecordDir() {
        File sampleDir = new File(basePath);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        File videoFile = sampleDir;
        // 创建文件
        try {
            //mp4格式
            mRecordFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".mp4", videoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 录制完成回调接口
     *
     * @author liuyinjun
     * @date 2015-2-5
     */
    public interface OnRecordListener {
        void onRecordFinish();

        /**
         * 录制进度
         */
        void onRecordProgress(int progress);
    }



}
