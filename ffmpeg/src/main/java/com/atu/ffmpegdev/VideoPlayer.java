package com.atu.ffmpegdev;

/**
 * Created by atu on 2018/3/26.
 */

public class VideoPlayer {

    static {
        System.loadLibrary("VideoPlayer");
    }

    public static native int play(Object surface);
}
