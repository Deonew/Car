package com.example.deonew.car;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by deonew on 17-3-24.
 */

public class AudioRecManager {
    //instance
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

    //for save file
    //fos
    private FileOutputStream fos;
    private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";

    //thread to read data from audio buffer in mAudioRec
    private dataCatch mDataCatch;
    private Thread mDataCatchThread;

    //control record
    private boolean isAudioRecording = false;

    //audio encode
    private MediaCodec mAudioCodec;

    //get main activity
    private VideoActivity2 mVideoActivity2;
    public void initAudioRecManager(VideoActivity2 vac2){
        //init
        // get minibuff according to given conf
        miniBuffSize = AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat)*2;
        //init mAudioRec according to minibuff and given conf
        mAudioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConf,audioFormat,miniBuffSize);
        mVideoActivity2 = vac2;
    }
    public void initAudioFos(){
        //init fos
        File file=new File(audioPath);
        if (file.exists()){
            file.delete();
        }
        try{
            fos = new FileOutputStream(audioPath,true);
        }catch(FileNotFoundException e){}
    }
    //init codec
    public void initAudioCodec(){
        //codec config
        MediaFormat f = MediaFormat.createAudioFormat("audio/mp4a-latm",sampleRate,channelCount);
        //"aac-profile"
        f.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //bit rate
        f.setInteger(MediaFormat.KEY_BIT_RATE, 25600);
        //create aac type codec
        try{
            mAudioCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        }catch (IOException e){}
        mAudioCodec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioCodec.start();
    }
    public void startAudioRec(){
        mAudioRec.startRecording();

        //start thread
        mDataCatchThread = new Thread(new dataCatch());
        mDataCatchThread.start();

        isAudioRecording = true;
    }
    public void stopRec(){
        isAudioRecording = false;
        mAudioRec.stop();
    }
    public void saveFile(){
//        fos.write(miniBuffSize,0,);
    }

    class dataCatch implements Runnable{
        @Override
        public void run() {
            while(isAudioRecording){
                readAudioData();
                //TODO sleep 10ms
            }
        }
    }

    //read raw data
    //encode the data
    //write it to a file according to the given string
    public void readAudioData() {
        //buff contains raw data
//        byte[] buff = new byte[miniBuffSize];
//        mAudioRec.read(buff,0,miniBuffSize);

        if (mVideoActivity2.getIsMuxering()){
        //read raw data
        int inputBuffIndex = mAudioCodec.dequeueInputBuffer(-1);
        if (inputBuffIndex>=0){
            //available input buffer
            ByteBuffer bybu = mAudioCodec.getInputBuffer(inputBuffIndex);
            bybu.clear();
//            bybu
            int len = mAudioRec.read(bybu,miniBuffSize);
            //put it back
            mAudioCodec.queueInputBuffer(inputBuffIndex,0,len, System.nanoTime()/1000,0);
        }
        //get data out
        MediaCodec.BufferInfo buInfo = new MediaCodec.BufferInfo();
        int outputBuffIndex =mAudioCodec.dequeueOutputBuffer(buInfo,0);

        if (outputBuffIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            //add audio track
            mVideoActivity2.addAudioTrack();
        }else if(outputBuffIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

        }else{
        while(outputBuffIndex>=0){
            ByteBuffer bybuOut = mAudioCodec.getOutputBuffer(outputBuffIndex);

//            if (mVideoActivity2.getIsMuxering()){
//                buInfo.presentationTimeUs = System.nanoTime() / 1000L;
//                mVideoActivity2.getMuxer().writeSampleData(mVideoActivity2.getmAudioTrackIndex(),bybuOut,buInfo);
//            }

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
                fos.write(encodedData,0,encodedData.length);
            }catch (IOException e){}

            //continue circle
            mAudioCodec.releaseOutputBuffer(outputBuffIndex,false);
            outputBuffIndex = mAudioCodec.dequeueOutputBuffer(buInfo,0);
        }
        }

        }
    }
//    public MediaFormat getAudioCodecOutputFormat(){
//        return mAudioCodec.getOutputFormat();
//    }
}
