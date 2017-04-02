package com.example.deonew.car;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.Button;

/**
 * Created by deonew on 17-3-2.
 */

/*
*record audio.
*/
public class carAudioRecord extends Activity {

    //audio source: micphone
    private int aSource = MediaRecorder.AudioSource.MIC;
    //sample rate
    private static int sampleRate = 44100;

    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private Button Start;
    private Button Stop;
    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态
    //AudioName裸音频数据文件
    private static final String AudioName = "/sdcard/love.raw";
    //NewAudioName可播放的音频文件
    private static final String NewAudioName = "/sdcard/new.wav";
}
