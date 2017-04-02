package com.example.deonew.car;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by deonew on 17-3-3.
 */

public class AudioActivity extends Activity {
    private static final int RECORDER_BPP = 16;
    //private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    //construct audiorecord
    private static int frequency = 22050;
    private static int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;//单声道
    private static int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;    //音频数据格式：脉冲编码调制（PCM）每个样品16位
    private AudioRecord audioRecord = null;
    private int recBufSize = 0;

    //
    private Thread recordingThread = null;

    private boolean isRecording = false;
    private Button btnStartRecord, btnStopRecord, btnStartPlay, btnStopPlay;
    private TextView txt;


    private int frequence = 8000; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    //--------------task
    private File audioFile;

    //-------------play task
    boolean isPlaying;
    private PlayTask player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        initBtns();


        //if sd card exists -- yes
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
//            return true;
//            txt.append("sd card exists");
//            txt.append(android.os.Environment.getExternalStorageState());
        }


        //file need to be created
        //在这里我们创建一个文件，用于保存录制内容
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");
        fpath.mkdirs();//创建文件夹
        try {
            //创建临时文件,注意这里的格式为.pcm
//            audioFile = File.createTempFile("recording", ".pcm", fpath);
            audioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //查看路径
//        txt.setText(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");
        txt.setText(getCacheDir().getAbsolutePath() + "/data/files/");
//        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/recording.pcm");
        File f = new File(getCacheDir() + "recording.pcm");
        if (f.exists()){
            txt.append("pcm exists");
        }
        else{
            txt.append("\noh no pcm not exists");
        }

    }

//    public void onClick(View v){
//        int btnId = v.getId();
//        switch (btnId){
//         case R.id.startPlay:
//             player = new PlayTask();
//             player.execute();
//             break;
//            case R.id.stopPlay:
//                isPlaying = false;
//                break;
//        }
//    }
    public void initBtns() {
        btnStartRecord = (Button) this.findViewById(R.id.startRecord);
        btnStartRecord.setTag("startAudioReco");
//        btnStartRecord.setOnClickListener(this);
        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordAudio();
            }
        });

        btnStopRecord = (Button) this.findViewById(R.id.stopRecord);
        btnStopRecord.setTag("stopAudioReco");
        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecordAudio();
            }
        });

        btnStartPlay = (Button) this.findViewById(R.id.startPlay);
        btnStartPlay.setTag("startAudioPlay");
        btnStartPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });

        btnStopPlay = (Button) this.findViewById(R.id.stopPlay);
        btnStopPlay.setTag("stopAudioPlay");
        btnStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlay();
            }
        });


        txt = (TextView) this.findViewById(R.id.textView2);

    }

    public void startRecordAudio() {
//        txt.append("start");

        //1 create audio record
        //get the mini buffer size to new an instance
//        recBufSize = AudioRecord.getMinBufferSize(frequency,
//                channelConfiguration, EncodingBitRate);
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
//                channelConfiguration, EncodingBitRate, recBufSize);

        //2 start
//        audioRecord.startRecording();

        //3 start a new thread to write data
//        isRecording = true;
//        recordingThread = new Thread(new Runnable() {
//            public void run() {
//                writeAudioDataToFile();
//            }
//        },"AudioRecorder Thread");
//        recordingThread.start();


        //task to record
        RecordTask recorder = new RecordTask();
        recorder.execute();

    }

    //stop
    public void stopRecordAudio() {
        isRecording = false;
    }

    //start play
    public void startPlay() {
        player = new PlayTask();
        player.execute();
//        isPlaying = true;
    }
    //stop
    public void stopPlay() {
        isPlaying = false;
    }

    public void writeAudioDataToFile() {
        byte data[] = new byte[recBufSize];
//        String filename = getTempFilename();
        String filename = getTempFilename();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int read = 0;
        if (null != os) {
            while (isRecording) {
                read = audioRecord.read(data, 0, recBufSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTempFilename() {
//        String filepath = Environment.getDataDirectory().getPath();
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);
        if (tempFile.exists())
            tempFile.delete();
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }


    //asynctask to record
    class RecordTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;

            try {
//                File audioFile;

                //开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                //实例化AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, frequence, channelConfig, audioEncoding, bufferSize);
                //定义缓冲
                short[] buffer = new short[bufferSize];

                //开始录制
                record.startRecording();

                int r = 0; //存储录制进度
                //定义循环，根据isRecording的值来判断是否继续录制
                while (isRecording) {
                    //从bufferSize中读取字节，返回读取的short个数
                    //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    //循环将buffer中的音频数据写入到OutputStream中
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(new Integer(r)); //向UI线程报告当前进度
                    r++; //自增进度值
                }
                //录制结束
                record.stop();
//                Log.v("The DOS available:", "::"+audioFile.length());
                dos.close();

            } catch (Exception e) {
            }

            return null;
        }
    }

    class PlayTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
            short[] buffer = new short[bufferSize / 4];
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < buffer.length) {
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    //然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);

                }

                //播放结束
                track.stop();
                dis.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }
    }
}