package com.example.deonew.car.Video;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.deonew.car.Audio.AudioFragmentV3;
import com.example.deonew.car.Audio.AudioSocketWrapper;
import com.example.deonew.car.R;
import com.example.deonew.car.Video.camera.Camera2BasicFragment;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VideoActivity3 extends FragmentActivity {
    private final String TAG = "VideoActivity3";


    private ShowFragment showFragment;
    private AudioFragmentV3 audioFragmentV3;
    private Camera2BasicFragment camera2BasicFragment;
    private AudioSocketWrapper audioSocketWrapper;
    private VideoSocketWrapper videoSocketWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video3);

        //camera preview fragment
        camera2BasicFragment = Camera2BasicFragment.newInstance();
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.cameraPreview, camera2BasicFragment)
                    .commit();
        }

        //show video fragment
        FragmentManager fragmentManager =getSupportFragmentManager();
        showFragment = new ShowFragment();
        fragmentManager.beginTransaction().add(R.id.showView,showFragment).commit();

        audioFragmentV3 = new AudioFragmentV3();
        fragmentManager.beginTransaction().add(R.id.audioControlFragment,audioFragmentV3).commit();

//        SurfaceView sv = (SurfaceView) findViewById(R.id.videoPlay);
//        showFragment.initMediaCodec();
//        showFragment.initMediaCodec(sv);
        Log.d(TAG,"init codec");
//        sendH264 = new SendH264(this);
        sendH264V3 = new SendH264V3(this);
//
        recvH264V3= new RecvH264V3(this);


        audioSocketWrapper = new AudioSocketWrapper(this,getAACSendQueue(),getAACRecvQueue());
        videoSocketWrapper = new VideoSocketWrapper(this,getAACSendQueue(),getAACRecvQueue());


        initBtn();


        editText = (EditText)findViewById(R.id.sendEditText);
    }



    public void initBtn(){

        Button recordBtn = (Button)findViewById(R.id.record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"recordBtn clicked");
                startRecordH264();
            }
        });

        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"sendBtn clicked");
                startSendH264();
            }
        });

        Button button = (Button)findViewById(R.id.recvH264V3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"recv clicked");
                startRecvH264();
            }
        });
        Button playBtn = (Button)findViewById(R.id.playH264V3);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"playBtn clicked");
                startH264Play();
            }
        });





        //-------set
        Button setSendBtn = (Button)findViewById(R.id.setSendBtn);
        setSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"set sendBtn clicked");
                setSendIP();
            }
        });
    }


    private EditText editText;
    private String sendIP = "10.1.1.1";
    private String recvIP = "10.1.1.1";
    public void setSendIP(){
        //get editbox string
        String s = editText.getText().toString();
        sendIP = s;
        Log.d(TAG,"send ip set" + s);
    }
    public String getSendIP(){
        return sendIP;
    }
    public void setRecvIP(String s){
        recvIP = s;
    }
    public String getRecvIP(){
        return recvIP;
    }


    //h264 data queue
    private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264SendQueue(){
        return H264SendQueue;
    }
    int totalSendcnt = 0;
    public void offerSendH264Queue(byte[] b){
        int n = b.length/1000;
        int m = b.length%1000;
        if (m !=0){
            n++;
        }
        for(int i = 0;i< n;i++){
            int len = 1000;
            if (i == n-1){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            H264SendQueue.offer(tmp);
            totalSendcnt++;
        }
//        Log.d(TAG,"offer one h264 and send size:"+getH264SendQueue().size()+"         "+ totalSendcnt);
    }


    //send h264
    private SendH264V3 sendH264V3;
    private boolean isSendH264 = false;

    public void startSendH264(){
//        Log.d(TAG,"ac3 send start");
//        isSendH264 = true;
//        sendH264V3.startSendH264();

        Log.d(TAG,"h264 send start");
        if (!isSendH264){
            videoSocketWrapper.startSendH264();
            isSendH264 = true;
        }else{

        }
    }
    private RecvH264V3 recvH264V3 = null;
    private boolean isRecvH264 = false;
    public void startRecvH264(){
        isRecvH264 = true;
        if (isRecvH264){
            recvH264V3.startRecvH264();
        }

//        if (!isRecvH264){
//            videoSocketWrapper.startRecvH264();
//            isRecvH264 = true;
//        }
    }

    //
    public void startH264Play(){
        showFragment.startPlay();
    }

    //h264 recv queue
    private BlockingQueue<byte[]> H264RecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264RecvQueue(){
        return H264RecvQueue;
    }
    private byte[] currentBuff = new byte[102400];
    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;
//    int cnt = 0;

    public byte[] getOneNalu(){
        int n = getNextIndex();
        if (n <= 0){
            Log.d(TAG,"nulllll"+"   "+n);
//            Log.d(TAG,n+"");
            return null;
        }
//        Log.d(TAG,"get one"+n);
        byte[] naluu = new byte[n-currentBuffStart];
        Log.d(TAG,n+"--n");
        Log.d(TAG,currentBuffStart+"");
        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);

        //handle currentBuff
        System.arraycopy(currentBuff, n , currentBuff, 0, currentBuff.length - n);

        //set index
        currentBuffStart = 0;
        currentBuffEnd = currentBuffEnd - naluu.length;
        return naluu;
    }
    //added by deonew
//    private int nextNaluHead = -1;
    public int getNextIndex(){
        int nextNaluHead;
        nextNaluHead = getNextIndexOnce();

        //currentBuff don't contain a nalu
        //poll data
        while(nextNaluHead == -1) {
            if (getH264RecvQueue().isEmpty()){
                Log.d(TAG,"queue empty");
                break;
            }else{
                byte[] tmp = (byte[])getH264RecvQueue().poll();
                System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
                currentBuffEnd = currentBuffEnd + tmp.length;
                nextNaluHead = getNextIndexOnce();
            }
//            cnt++;
//            Log.d(TAG,"poll"+cnt);
        }
        nextNaluHead = nextNaluHead - 3;
        // currentBuffStart = nextNaluHead;
        return nextNaluHead;
    }

    //get next 000000[01]
    public int getNextIndexOnce(){
        int nextIndex = -1;
        byte[] naluHead = {0,0,0,1};
        byte[] correctBuff = {0,1,2,0};
        int i;
        int index = 0;
        for(i = currentBuffStart+1; i < currentBuffEnd;i++){
            while (index > 0 && currentBuff[i] != naluHead[index]) {
                index = correctBuff[index - 1];
            }
            if (currentBuff[i] == naluHead[index]) {
                index++;
                if (index == 4){
                    nextIndex = i;//i = 00000001中的01
                    break;
                }
            }
        }
        return nextIndex;
    }

    //record
    private boolean isRecordH264 = false;
    public void startRecordH264() {
//        Log.d(TAG,"stop record");
        isRecordH264 = !isRecordH264;
        if (isRecordH264) {
            camera2BasicFragment.startRecordH264();
//            isRecordH264 = true;
        }else{
            Log.d(TAG,"stop record");
            camera2BasicFragment.stopRecordH264();
//            isRecordH264 = false;
        }
    }


    //--------------------audio record
    public void startRecordAAC(){
//        audioFragmentV3.sta
    }


    //------------------------------------aac send
    public void startSendAAC(){
        audioSocketWrapper.startSend();
    }
    public void startRecvAAC(){
        audioSocketWrapper.startRecv();
    }


    private BlockingQueue<byte[]> AACSendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getAACSendQueue(){
        return AACSendQueue;
    }

    public void offerAudioSendQueue(byte[] b){
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
//    LinkedBlockingQueue
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


    //audio socket
    private Socket audioSocket = null;
    public Socket getAudioSocket(){
        return audioSocket;
    }
    public void setAudioSocket(Socket s){
        audioSocket = s;
    }



}
