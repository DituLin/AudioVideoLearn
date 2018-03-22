package com.atu.audiorecord;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.atu.audiorecord.enums.AudioRecordStatus;
import com.atu.audiorecord.record.AudioRecorder;
import com.atu.audiorecord.record.FileUtils;
import com.atu.baselib.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.tv_record_status)
    TextView tvStatus;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.btn_pause)
    Button btnPause;

    private AudioRecorder audioRecord;

    @OnClick({
            R.id.btn_start,
            R.id.btn_pause,
            R.id.btn_stop,
            R.id.btn_query
    })
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                startRecord();
                break;
            case R.id.btn_pause:
                pauseRecord();
                break;
            case R.id.btn_stop:
                stopRecord();
                break;
            case R.id.btn_query:
                queryAudio();
                break;
        }
    }

    private void queryAudio() {
        tvResult.setText("pam size:"+ FileUtils.getPcmFiles().size() + "\n" + "wav size:" + FileUtils.getWavFiles().size());
    }

    private void stopRecord() {
        if (audioRecord.getStatus() == AudioRecordStatus.STATUS_START) {
            audioRecord.stopAudioRecord();
            tvStatus.setText("停止");
        }
    }

    private void pauseRecord() {
        if (audioRecord.getStatus() == AudioRecordStatus.STATUS_START) {
            audioRecord.pauseAudioRecord();
            btnPause.setText("继续");
        }else {
            audioRecord.startAudioRecord();
            btnPause.setText("暂停");
        }

    }

    private void startRecord() {
        if (audioRecord.getStatus() == AudioRecordStatus.STATUS_NO_READY) {
            //初始化录音
            String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            audioRecord.createDefaultAudio(fileName);
            audioRecord.startAudioRecord();
            tvStatus.setText("录制中");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, 5);
        }
        audioRecord = AudioRecorder.getInstance();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (audioRecord.getStatus() == AudioRecordStatus.STATUS_START) {
            audioRecord.pauseAudioRecord();
            tvStatus.setText("暂停");
        }
    }

    @Override
    protected void onDestroy() {
        audioRecord.release();
        super.onDestroy();
    }
}
