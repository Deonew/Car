package com.example.deonew.car.Audio;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.example.deonew.car.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/11/17.
 */

public class AudioActivity2 extends Activity{
    //audio
    private FileOutputStream audioFos;
    private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";

    private AudioRecord mAudioRec;
    //for init
    private int sampleRate = 44100;
    private int channelCount = 2;
    //stereo
    private int channelConf = AudioFormat.CHANNEL_IN_STEREO;
    //data format
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //generate by config
    //used when read data
    private int miniBuffSize;

    //control record
    private boolean isAudioRecording = false;

    //audio encode
    private MediaCodec mAudioCodec;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio3);

//        Log.d(TAG,"oncreate");


        //new
        initAudioRec();

        //thread
    }


    private Thread msendTH = null;
    public void sendAudio(View v){
        //record
        isSend = true;
//        if ()
        if (isSend && msendTH == null){
            mAudioRec.startRecording();
            msendTH = new Thread(new SendRun());
            msendTH.start();
        }
    }

    public void playAudio(View v){
        Log.d("aaaaaaaaaaaaaaaaaa","play aac");
        //play
        String fielPath = (Environment.getExternalStorageDirectory().getPath() + "/carTemp.aac");
        AudioDecoder audioDecoder = new AudioDecoder(fielPath);
        audioDecoder.start();
    }

    private boolean isSend = false;
    class SendRun implements Runnable{
        @Override
        public void run() {
            //socket connect

            while(isSend){
                readAudioData();
            }

        }
    }

    //encoder and file output
    private InputStream audioFis = null;
    public void initAudioRec(){
        miniBuffSize = AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat)*2;
        //init mAudioRec according to minibuff and given conf
        mAudioRec = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConf, audioFormat, miniBuffSize);

        //init fos
        File file=new File(audioPath);
        if (file.exists()){
//            file.delete();
        }
        try{
            audioFos = new FileOutputStream(audioPath,true);
            audioFis = new FileInputStream(audioPath);
        }catch(FileNotFoundException e){}

        //codec config
        MediaFormat f = MediaFormat.createAudioFormat("audio/mp4a-latm",sampleRate,channelCount);
        //"aac-profile"
        f.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //bit rate
        f.setInteger(MediaFormat.KEY_BIT_RATE, 25600);
        //create aac type codec
        try{
            mAudioCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");//encoder
        }catch (IOException e){}
        mAudioCodec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioCodec.start();

    }

    //read raw data
    //encode the data
    //write it to a file according to the given string
    public void readAudioData() {

            int inputBuffIndex = mAudioCodec.dequeueInputBuffer(-1);
            if (inputBuffIndex>=0){
                //available input buffer
                ByteBuffer bybu = mAudioCodec.getInputBuffer(inputBuffIndex);
                bybu.clear();
//            bybu
                int len = mAudioRec.read(bybu,miniBuffSize);

//                byte[] b =new byte[miniBuffSize];
//                int len = audioFis.read(b);
                //put it back

                mAudioCodec.queueInputBuffer(inputBuffIndex,0,len, System.nanoTime()/1000,0);
            }
            //get data out
            MediaCodec.BufferInfo buInfo = new MediaCodec.BufferInfo();
            int outputBuffIndex =mAudioCodec.dequeueOutputBuffer(buInfo,0);

            if (outputBuffIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            }else if(outputBuffIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

            }else{
                while(outputBuffIndex>=0){
                    ByteBuffer bybuOut = mAudioCodec.getOutputBuffer(outputBuffIndex);

                    //encodedData receive data from buffer
                    byte[] encodedData = new byte[buInfo.size+7];

                    //get raw data
                    bybuOut.get(encodedData,7,buInfo.size);

                    //add adts
                    // to save as aac format
                    int profile = 2;  //AAC LC
                    int freqIdx = 4;  //44.1KHz
                    int chanCfg = 2;  //CPE
                    encodedData[0] = (byte)0xFF;
                    encodedData[1] = (byte)0xF9;
                    encodedData[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
                    encodedData[3] = (byte)(((chanCfg&3)<<6) + (encodedData.length>>11));
                    encodedData[4] = (byte)((encodedData.length&0x7FF) >> 3);
                    encodedData[5] = (byte)(((encodedData.length&7)<<5) + 0x1F);
                    encodedData[6] = (byte)0xFC;
                    //write data
                    try{
                        audioFos.write(encodedData,0,encodedData.length);
                        //send
                    }catch (IOException e){}
                    //continue circle
                    mAudioCodec.releaseOutputBuffer(outputBuffIndex,false);
                    outputBuffIndex = mAudioCodec.dequeueOutputBuffer(buInfo,0);
                }
            }
    }
}


