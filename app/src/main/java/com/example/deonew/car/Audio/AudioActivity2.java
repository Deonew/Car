package com.example.deonew.car.Audio;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.deonew.car.R;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 4/11/17.
 */

public class AudioActivity2 extends Activity{

    private SendAAC sendAAC;
    private RecvAAC recvAAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio3);

        recvAAC = new RecvAAC(this);

        sendAAC = new SendAAC(this);

        //
        recordAAC = new RecordAAC(this);
        recordAAC.startRecord();

    }
    //--------------------------------------------click event
    public void sendAudio(View v){
        setSendAacStatus(true);
        sendAAC.startSendAAC();
    }

    public void setSendAacStatus(boolean value){
        isSendingAac = value;
        sendAAC.setSendAacStatus(value);
    }

    private boolean isRecvAAC = false;
    public void recvAacClick(View v){
        //
        isRecvAAC = true;

    }

    //
    public void playAac(View v){
        PlayAAC audioDecoder = new PlayAAC(this);
        audioDecoder.start();
    }
    private RecordAAC recordAAC;
    private boolean isRecordingAac = false;
    public void setRecordAacStatus(boolean value){
        isRecordingAac = value;
        recordAAC.setRecordAacStatus(value);
    }
    public void recordAac(View v){
        setRecordAacStatus(true);
    }

    private boolean isSendingAac = false;


    //------------------------------------aac send
    private BlockingQueue<byte[]> AACSendQueue = new ArrayBlockingQueue<byte[]>(10000);

    public BlockingQueue getAACSendQueue(){
        return AACSendQueue;
    }
    public void offerAudioQueue(byte[] b){
        int n = b.length/1000;
        for(int i = 0;i< n+1;i++){
            int len = 1000;
            if (i == n){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            getAACSendQueue().offer(tmp);
            i++;
        }
    }
    //---------------------------------------------------aac recv
    private BlockingQueue<byte[]> AACRecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue<byte[]> getAACRecvQueue() {
        return AACRecvQueue;
    }
    public void offerAudioRecvQueue(byte[] b){
        int n = b.length/1000;
        for(int i = 0;i< n+1;i++){
            int len = 1000;
            if (i == n){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            getAACRecvQueue().offer(tmp);
            i++;
        }
    }
}