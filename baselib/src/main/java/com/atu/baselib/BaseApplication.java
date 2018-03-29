package com.atu.baselib;

import android.app.Application;

import com.atu.baselib.common.Constant;
import com.atu.baselib.utils.FileUtils;

import java.io.File;

/**
 * Created by atu on 2018/3/22.
 */

public class BaseApplication extends Application {

    public static String ROOT;
    public static String DIR_IMAGE;
    public static String DIR_VIDEO;

    @Override
    public void onCreate() {
        super.onCreate();
        createFilePath();
    }

    private void createFilePath() {
        if (null == ROOT) {
            // 获取SD卡或内置存储区的根目录路径
            ROOT = FileUtils.getSDPath();
        }
        if (ROOT != null){
            DIR_IMAGE = ROOT + File.separator + Constant.DIR_IMAGE;
            DIR_VIDEO = ROOT + File.separator + Constant.DIR_VIDEO;

            if (!FileUtils.isFileExists(DIR_IMAGE))
                FileUtils.createDirFile(DIR_IMAGE);
            if (!FileUtils.isFileExists(DIR_VIDEO))
                FileUtils.createDirFile(DIR_VIDEO);
        }
    }
}
