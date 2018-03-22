package com.atu.audiorecord.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import com.atu.audiorecord.enums.AudioRecordStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by atu on 2018/3/22.
 */

public class AudioRecorder {

    public final static String TAG = "AudioRecorder";

    //麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;

    //采样频率
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;

    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;

    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    private AudioRecord audioRecord;

    private String fileName;

    private AudioRecordStatus recordStatus = AudioRecordStatus.STATUS_NO_READY;

    //录音文件
    private List<String> filesName = new ArrayList<>();

    private static AudioRecorder audioRecorder = null;

    private AudioRecorder() {
    }

    public static AudioRecorder getInstance() {
        synchronized (AudioRecorder.class) {
            if (audioRecorder == null) {
                audioRecorder = new AudioRecorder();
            }
        }

        return audioRecorder;
    }

    /**
     * 创建默认的录音文件
     *
     * @param fileName
     */
    public void createDefaultAudio(String fileName) {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        this.fileName = fileName;
        recordStatus = AudioRecordStatus.STATUS_READY;
    }


    /**
     * 创建录音对象
     *
     * @param fileName       文件名
     * @param audioSource    音源
     * @param sampleRateInHz 采样频率
     * @param channelConfig  声道
     * @param audioFormat    编码格式
     */
    public void createAudio(String fileName, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        this.fileName = fileName;
        recordStatus = AudioRecordStatus.STATUS_READY;
    }


    /**
     * 开始录音
     */
    public void startAudioRecord() {
        if (recordStatus == AudioRecordStatus.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (recordStatus == AudioRecordStatus.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }

        audioRecord.startRecording();

        // 开启线程写文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeDataTOFile();
            }


        }).start();


    }


    /**
     * 暂停录音
     */
    public void pauseAudioRecord() {
        if (recordStatus != AudioRecordStatus.STATUS_START) {
            throw new IllegalStateException("未在录音");
        } else {
            audioRecord.stop();
            recordStatus = AudioRecordStatus.STATUS_PAUSE;
        }
    }


    /**
     * 停止录音
     */
    public void stopAudioRecord() {
        if (recordStatus == AudioRecordStatus.STATUS_NO_READY || recordStatus == AudioRecordStatus.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            audioRecord.stop();
            recordStatus = AudioRecordStatus.STATUS_STOP;
            release();
        }
    }

    public void release() {

        if (filesName.size() > 0) {
            List<String> filePaths = new ArrayList<>();
            for (String fileName : filesName) {
                filePaths.add(FileUtils.getPcmFileAbsolutePath(fileName));
            }

            filesName.clear();
            mergePCMFilesToWAVFile(filePaths);
        }



        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        recordStatus = AudioRecordStatus.STATUS_NO_READY;
    }


    private void writeDataTOFile() {
        //缓冲区
        byte[] bytes = new byte[bufferSizeInBytes];

        FileOutputStream fos = null;

        int readsize = 0;

        try {
            String currentFileName = fileName;

            if (recordStatus == AudioRecordStatus.STATUS_PAUSE) {
                currentFileName += filesName.size();
            }

            filesName.add(currentFileName);

            File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName));
            if (file.exists()) {
                file.delete();
            }


            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        recordStatus = AudioRecordStatus.STATUS_START;
        Log.d(TAG,"开始录音...");

        while (recordStatus == AudioRecordStatus.STATUS_START) {
            readsize = audioRecord.read(bytes,0,bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 合并 pcw 为 wav
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.mergePCMFilesToWAVFile(filePaths,  FileUtils.getWavFileAbsolutePath(fileName))) {
                    //成功
                    Log.d(TAG,"录制成功");
                }else {
                    //失败
                    Log.d(TAG,"录制失败");
                }
            }
        }).start();
    }


    /**
     * 获取录音对象的状态
     *
     * @return
     */
    public AudioRecordStatus getStatus() {
        return recordStatus;
    }

    /**
     * 获取本次录音文件的个数
     *
     * @return
     */
    public int getPcmFilesCount() {
        return filesName.size();
    }



}
