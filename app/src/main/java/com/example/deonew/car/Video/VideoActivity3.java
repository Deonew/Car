package com.example.deonew.car.Video;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.deonew.car.R;
import com.example.deonew.car.Video.camera.Camera2BasicFragment;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VideoActivity3 extends FragmentActivity {
    private final String TAG = "VideoActivity3";

    private ShowFragment showFragment;
    private Camera2BasicFragment camera2BasicFragment;
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

//        SurfaceView sv = (SurfaceView) findViewById(R.id.videoPlay);
//        showFragment.initMediaCodec();
//        showFragment.initMediaCodec(sv);
        Log.d(TAG,"init codec");
//        sendH264 = new SendH264(this);
        sendH264V3 = new SendH264V3(this);

        recvH264V3= new RecvH264V3(this);


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
                startPlay();
            }
        });
        Button recordBtn = (Button)findViewById(R.id.record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"recordBtn clicked");
                startRecord();
            }
        });

        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"sendBtn clicked");
                startSend();
            }
        });
    }

    //h264 data queue
    private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264SendQueue(){
        return H264SendQueue;
    }
    public void offerSendH264Queue(byte[] b){
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
            getH264SendQueue().offer(tmp);
            i++;

        }
    }

    //send h264
    private SendH264V3 sendH264V3;
    private boolean isSendH264 = false;
    public void startSend(){
        Log.d(TAG,"ac3 send start");
        isSendH264 = true;
        sendH264V3.startSendH264();
    }


    private RecvH264V3 recvH264V3 = null;
    private boolean isRecvH264 = false;
    public void startRecvH264(){
        isRecvH264 = true;
        if (isRecvH264){
            recvH264V3.startRecvH264();
        }
    }

    //
    public void startPlay(){
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
    public void startRecord(){
        camera2BasicFragment.startRecord();
    }
    //send
//    public void startSend(){
//        startSend();
//    }


}
