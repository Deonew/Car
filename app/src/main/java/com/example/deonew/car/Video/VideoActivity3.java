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
import com.example.deonew.car.Tool.TimeStamp;
import com.example.deonew.car.Video.camera.Camera2BasicFragment;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VideoActivity3 extends FragmentActivity {
    private final String TAG = "VideoActivity3";


    private PlayH264Fragment playH264Fragment;
    private AudioFragmentV3 audioFragmentV3;
    private Camera2BasicFragment camera2BasicFragment;
    private AudioSocketWrapper audioSocketWrapper;
    private VideoSocketWrapper videoSocketWrapper;


    private boolean isSendH264 = false;
    private boolean isRecordH264 = false;
    private boolean isRecvAAC = false;

    private Button sendH264Btn = null;
    private Button recordH264Btn = null;

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
        playH264Fragment = new PlayH264Fragment();
        fragmentManager.beginTransaction().add(R.id.showView, playH264Fragment).commit();

        audioFragmentV3 = new AudioFragmentV3();
        fragmentManager.beginTransaction().add(R.id.audioControlFragment,audioFragmentV3).commit();

        audioSocketWrapper = new AudioSocketWrapper(this,getAACSendQueue(),getAACRecvQueue());
        videoSocketWrapper = new VideoSocketWrapper(this,getAACSendQueue(),getAACRecvQueue());




        initBtn();


        editText = (EditText)findViewById(R.id.sendEditText);

    }




    public void initBtn(){

        recordH264Btn = (Button)findViewById(R.id.record);
        recordH264Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordH264Click();
            }
        });

        sendH264Btn = (Button)findViewById(R.id.sendBtn);
        sendH264Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"sendBtn clicked");
                sendH264Click();
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
        int last = 0;
        for(int i = 0;i< n;i++){
            int len = 1000;
            if (i == n-1){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
//            last = len + last;

            //not use timestamp
//            H264SendQueue.offer(tmp);
//            Log.d(TAG,tmp.length+"");

            //add time stamp
            byte[] toOfferWithTS = new byte[tmp.length+8];
            long t = System.currentTimeMillis();
            ByteBuffer bf = ByteBuffer.allocate(8);
            bf.putLong(0,t);
            byte [] timeArr = bf.array();
//            Log.d(TAG,t+" time");
            System.arraycopy(timeArr,0,toOfferWithTS,0,8);
            System.arraycopy(tmp,0,toOfferWithTS,8,tmp.length);
            H264SendQueue.offer(toOfferWithTS);
//            Log.d(TAG,toOfferWithTS.length+"");
//            totalSendcnt++;
        }
//        Log.d(TAG,"offer one h264 and send size:"+getH264SendQueue().size()+"         "+ totalSendcnt);
    }

    public void sendH264Click(){
        isSendH264 = !isSendH264;
        if (isSendH264){
            startSendH264();
        }else {
            stopSendH264();
        }
    }
    public void startSendH264(){
        videoSocketWrapper.startSendH264();
        sendH264Btn.setText("Stop");
    }
    public void stopSendH264(){
        videoSocketWrapper.stopSendH264();
        sendH264Btn.setText("Start");
    }

    private RecvH264V3 recvH264V3 = null;
    private boolean isRecvH264 = false;
    public void startRecvH264(){
        if (!isRecvH264){
            videoSocketWrapper.startRecvH264();
            isRecvH264 = true;
        }
    }

    //
    public void startH264Play(){
        playH264Fragment.startPlay();
    }

    public void videoSleep(long l){
        playH264Fragment.VideoSleep(l);
    }
    public void audioSleep(long l){
        audioFragmentV3.audioSleep(l);
    }


    //h264 recv queue
    private BlockingQueue<byte[]> H264RecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264RecvQueue(){
        return H264RecvQueue;
    }
    private byte[] currentBuff = new byte[102400];
    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;

    public byte[] getOneNalu(){
        int n = getNextIndex();
        if (n <= 0){
            return null;
        }
        byte[] naluu = new byte[n-currentBuffStart];
        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);

        //handle currentBuff
        System.arraycopy(currentBuff, n , currentBuff, 0, currentBuff.length - n);

        //set index
        currentBuffStart = 0;
        currentBuffEnd = currentBuffEnd - naluu.length;

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(0x00000001);
        byte[] naluHead = b.array();
        if (naluu[0]!=naluHead[0] || naluu[1]!=naluHead[1] || naluu[2]!=naluHead[2] || naluu[3]!=naluHead[3]){
            Log.d(TAG,"head wrong");
            return null;
        }
        return naluu;
    }
    private boolean isSynchronized = false;
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

                int len = tmp.length;
                //set timestamp
                byte[] t = new byte[8];
                System.arraycopy(tmp,0,t,0,8);
                ByteBuffer bf = ByteBuffer.allocate(8);
                bf.put(t);
                bf.flip();
                long ts = bf.getLong();
                TimeStamp.setVideoStamp(ts);
                Log.d(TAG,"video time "+ts);

                long t1 = TimeStamp.getAudioStamp();
                long t2 = TimeStamp.getVideoStamp();
                Log.d(TAG,"isSynchronized"+isSynchronized+" time delta"+Math.abs(t1-t2));

                if (!isSynchronized){
                    long au = TimeStamp.getAudioStamp();
                    long vi = TimeStamp.getVideoStamp();
                    long delta = au - vi;
                    long deltaABS = Math.abs(delta);
                    if ( deltaABS > 50){

                        if (delta<0){
                            //audio behind
                            videoSleep(deltaABS);
                            isSynchronized = true;
                        }else {
                            //video behind
                            audioSleep(deltaABS);
                            isSynchronized = true;
                        }
                    }else {
                        isSynchronized = true;
                    }
                }

                byte[] validData = new byte[len-8];
                System.arraycopy(tmp,8,validData,0,len-8);
                System.arraycopy(validData,0,currentBuff,currentBuffEnd,validData.length);
                currentBuffEnd = currentBuffEnd + validData.length;

//                System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
//                currentBuffEnd = currentBuffEnd + tmp.length;
                nextNaluHead = getNextIndexOnce();
            }
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
    public void recordH264Click(){
        isRecordH264 = !isRecordH264;
        if (isRecordH264){
            startRecordH264();
        }else {
            stopRecordH264();
        }
    }
    public void startRecordH264() {
        camera2BasicFragment.startRecordH264();
        recordH264Btn.setText("Stop Reco");
    }
    public void stopRecordH264() {
        camera2BasicFragment.stopRecordH264();
        recordH264Btn.setText("RecordH264");
    }





    //------------------------------------aac send
    private boolean isSendAAC = false;
    public void startSendAAC(){
        audioSocketWrapper.startSend();
        Button b = (Button)findViewById(R.id.sendAACV3);
        b.setText("stopSend");
    }
    public void sendAACClick(){
        isSendAAC = !isSendAAC;
        if (isSendAAC){
            startSendAAC();
        }else {
            stopSendAAC();
        }
    }
    public void stopSendAAC(){
        audioSocketWrapper.stopSend();
        Button b = (Button)findViewById(R.id.sendAACV3);
        b.setText("startSend");
    }


    public void recvAACClick(){
        isRecvAAC = !isRecvAAC;
        if (isRecvAAC){
            startRecvAAC();
        }else {
            stopRecvAAC();
        }
    }
    public void startRecvAAC(){
        audioSocketWrapper.startRecv();
        audioFragmentV3.startRecvAACV3();
    }
    public void stopRecvAAC(){
        audioSocketWrapper.stopRecv();
        audioFragmentV3.stopRecvAACV3();
    }


    private BlockingQueue<byte[]> AACSendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getAACSendQueue(){
        return AACSendQueue;
    }

    public void offerAudioSendQueue(byte[] b){
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
//            AACSendQueue.offer(tmp);

            //add time stamp
            byte[] toOfferWithTS = new byte[tmp.length+8];
            long t = System.currentTimeMillis();
            ByteBuffer bf = ByteBuffer.allocate(8);
            bf.putLong(0,t);
            byte [] timeArr = bf.array();
            System.arraycopy(timeArr,0,toOfferWithTS,0,8);
            System.arraycopy(tmp,0,toOfferWithTS,8,tmp.length);
            AACSendQueue.offer(toOfferWithTS);
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
