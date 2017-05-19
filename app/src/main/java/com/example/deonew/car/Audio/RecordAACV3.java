package com.example.deonew.car.Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.example.deonew.car.Tool.TimeStamp;
import com.example.deonew.car.Video.VideoActivity3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/16/17.
 */

public class RecordAACV3 {
    private String TAG = "RecordAACV3";
    private VideoActivity3 mainAC;

    //audio
    private FileOutputStream audioFos;
    private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";

    private AudioRecord mAudioRecorder;
    //for startSendH264
    private int sampleRate = 44100;
    private int channelCount = 2;
    //stereo
    private int channelConf = AudioFormat.CHANNEL_IN_STEREO;
//    private int channelConf = AudioFormat.CHANNEL_IN_MONO;
    //data format
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int aacProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private int keyBitRate = 64000;
    //generate by config
    //used when read data
    private int miniBuffSize;

    //control record
//    private boolean isAudioRecording = false;

    //audio encode
    private MediaCodec mAudioCodec;
    private TimeStamp mAACTimeStamp;

    public RecordAACV3(VideoActivity3 ac){
        mainAC = ac;
        mAACTimeStamp = new TimeStamp();
    }

    public void initAudioRec(){
        miniBuffSize = AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat);
        //startSendH264 mAudioRecorder according to minibuff and given conf
        mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConf, audioFormat, miniBuffSize);
        //startSendH264 fos
        File file=new File(audioPath);
        if (file.exists()){
//            file.delete();
        }
        try{
            audioFos = new FileOutputStream(audioPath,true);
//            audioFis = new FileInputStream(audioPath);
        }catch(FileNotFoundException e){}



        //create aac type codec
        try{
            mAudioCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");//encoder
        }catch (IOException e){}

        //----------codec config
        MediaFormat f = MediaFormat.createAudioFormat("audio/mp4a-latm",sampleRate,channelCount);
        //"aac-profile"
        f.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //bit rate
        f.setInteger(MediaFormat.KEY_BIT_RATE, keyBitRate);

        f.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
//        f.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);

        mAudioCodec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioCodec.start();
        Log.d(TAG,"audio codec");
    }

    public void startRecord(){
        setRecordAacStatus(true);
        initAudioRec();
        mAudioRecorder.startRecording();
        new RecordAacThread().start();
    }
    public void stopRecord(){
        isRecordAAC = false;
    }
    private boolean isRecordAAC = false;
    public void setRecordAacStatus(boolean value){
        isRecordAAC = value;
    }

    class RecordAacThread extends Thread{
        @Override
        public void run() {
            //socket connect
            while(true){
                if (isRecordAAC){
                    Log.d(TAG,"read data");
                    readAudioData();
                }
            }
        }
        public void readAudioData() {

            int inputBuffIndex = mAudioCodec.dequeueInputBuffer(-1);
            if (inputBuffIndex>=0){
                ByteBuffer bybu = mAudioCodec.getInputBuffer(inputBuffIndex);
                bybu.clear();
                int len = mAudioRecorder.read(bybu,miniBuffSize);
                mAudioCodec.queueInputBuffer(inputBuffIndex,0,len, System.nanoTime()/1000,0);
            }

            //get data out
            MediaCodec.BufferInfo buInfo = new MediaCodec.BufferInfo();
            int outputBuffIndex =mAudioCodec.dequeueOutputBuffer(buInfo,0);

            if (outputBuffIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
            }
            else if(outputBuffIndex == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
            }
            else
            {
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
                    encodedData[0] = (byte)0xff;
                    encodedData[1] = (byte)0xff9;
                    encodedData[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
                    encodedData[3] = (byte)(((chanCfg&3)<<6) + (encodedData.length>>11));
                    encodedData[4] = (byte)((encodedData.length&0x7FF) >> 3);
                    encodedData[5] = (byte)(((encodedData.length&7)<<5) + 0x1F);
                    encodedData[6] = (byte)0xFC;
                    Log.d(TAG,"out put one");
                    //write data
                    try{
                        audioFos.write(encodedData,0,encodedData.length);

                        //add timestamp
//                        byte[] aacts = mAACTimeStamp.getTimeStamp();
//                        byte[] toOffer = new byte[encodedData.length+8];
//                        System.arraycopy(aacts,0,toOffer,0,8);
//                        System.arraycopy(encodedData,0,toOffer,8,encodedData.length);

                        //put data to the queue
//                        mainAC.offerAudioSendQueue(toOffer);
                        mainAC.offerAudioSendQueue(encodedData);

                        Log.d(TAG,""+ mainAC.getAACSendQueue().size());

                    }catch (IOException e){}
                    //continue circle
                    mAudioCodec.releaseOutputBuffer(outputBuffIndex,false);
                    outputBuffIndex = mAudioCodec.dequeueOutputBuffer(buInfo,0);
                }
            }

            try{
                Thread.sleep(10);
            }catch (InterruptedException e){}

        }

    }

}
