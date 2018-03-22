package com.atu.audiotrack;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.atu.baselib.BaseActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private File mAudioFile = null;
    private Thread mCaptureThread = null;
    private boolean mIsRecording, mIsPlaying;
    private int mFrequence = 44100; // 采样频率
    private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;//录制 声道
    private int mPlayChannelConfig = AudioFormat.CHANNEL_IN_STEREO;//播放 声道
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;//编码格式


    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.tv_record_status)
    TextView tvStatus;
    @BindView(R.id.tv_result)
    TextView tvResult;


    private RecordTask mRecorder;

    @OnClick({
            R.id.btn_start,
            R.id.btn_query,
            R.id.btn_play
    })
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (btnStart.getTag() == null) {
                    startAudioRecord();
                }else {
                    stopAudioRecord();
                }
                break;
            case R.id.btn_query:
                break;
            case R.id.btn_play:
                if (btnPlay.getTag() == null) {
                    playAudio();
                }else {
                    stopAudio();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, 5);
        }
    }


    private void startAudioRecord() {
        btnStart.setTag(this);
        File fpath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/AudioTrack");
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        try {
            // 创建临时文件,注意这里的格式为.pcm
            mAudioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mRecorder = new RecordTask();
        mRecorder.execute();
        btnStart.setText("停止录音");
        tvStatus.setText("录音中...");
    }

    private void stopAudioRecord() {
        mIsRecording = false;
        btnStart.setTag(null);
        btnStart.setText("开始");
        tvStatus.setText("停止录音");
    }

    private void playAudio() {
        btnPlay.setTag(this);
        PlayTask playTask = new PlayTask();
        playTask.execute();
        btnPlay.setText("停止播放");
        tvStatus.setText("播放。。。");
    }

    private void stopAudio() {
        mIsPlaying = false;
        btnPlay.setTag(null);
        btnPlay.setText("播放");
        tvStatus.setText("停止播放.");
    }

    /**
     * 录制 task
     */
    class RecordTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mIsRecording = true;

            try {
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mAudioFile)));
                int bufferSize = AudioRecord.getMinBufferSize(mFrequence,
                        mChannelConfig, mAudioEncoding);
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, mFrequence,
                        mChannelConfig, mAudioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];

                record.startRecording();//开始录制

                while (mIsRecording) {
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.write(buffer[i]);
                    }
                }

                record.stop();
                dos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    /**
     * 播放 task
     */
    class PlayTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mIsPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(mFrequence,
                    mPlayChannelConfig, mAudioEncoding);

            short[] buffer = new short[bufferSize];

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mAudioFile)));
                // 实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                        mFrequence,
                        mPlayChannelConfig, mAudioEncoding, bufferSize,
                        AudioTrack.MODE_STREAM);

                track.play();

                while (mIsPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < buffer.length) {
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    track.write(buffer,0,buffer.length);
                }

                track.stop();
                dis.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


}
