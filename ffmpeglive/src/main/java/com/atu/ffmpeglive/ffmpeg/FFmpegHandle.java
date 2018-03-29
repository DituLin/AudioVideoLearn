package com.atu.ffmpeglive.ffmpeg;

/**
 * Created by atu on 2018/3/28.
 */

public class FFmpegHandle {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("postproc-54");
        System.loadLibrary("ffmpeg-handle");
    }


    private static FFmpegHandle instance = null;
     
    private FFmpegHandle(){
    }

    public static FFmpegHandle getInstance() {
        synchronized (FFmpegHandle.class) {
            if (instance == null) {
                instance = new FFmpegHandle();
            }
        }
    
        return instance;
    }


    public native int initVideo(String url);

    public native int onFrameCallback(byte[] buffer);

    public native int close();
}
