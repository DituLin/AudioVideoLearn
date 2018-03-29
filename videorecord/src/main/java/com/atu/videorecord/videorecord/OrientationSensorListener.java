package com.atu.videorecord.videorecord;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * 重力感应
 * Created by atu on 2018/3/23.
 */

public class OrientationSensorListener implements SensorEventListener {

    private static final int _DATA_X = 0;
    private static final int _DATA_Y = 1;
    private static final int _DATA_Z = 2;
    public static final int ORIENTATION_UNKNOWN = -1;
    private int orientationHintDegrees;


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        int orientation = ORIENTATION_UNKNOWN;
        float X = -values[_DATA_X];
        float Y = -values[_DATA_Y];
        float Z = -values[_DATA_Z];

        float magnitude = X * X + Y * Y;
        if (magnitude * 4 > Z * Z) {
            float OneEightyOverPi = 57.29577957855f;
            float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
            orientation = 90 - (int) Math.round(angle);
            while (orientation >= 360) {
                orientation -= 360;
            }
            while (orientation < 0) {
                orientation += 360;
            }
        }

        if (orientation > 45 && orientation < 135) {
            // 右平行--180
            orientationHintDegrees = 180;
        } else if (orientation > 135 && orientation < 225) {
            // 倒屏--90
            orientationHintDegrees = 90;
        } else if (orientation > 225 && orientation < 315) {
            // 左水平-->0
            orientationHintDegrees = 0;
        } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
            //--竖屏--->90
            orientationHintDegrees = 90;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getOrientationHintDegrees() {
        return orientationHintDegrees;
    }


}