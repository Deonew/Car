package com.example.deonew.car.Video;

import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 4/27/17.
 */

public class VideoSocketWrapper {
    private String TAG = "VideoSocketWrapper";

    private BlockingQueue videoSendQueue;
    private BlockingQueue videoRecvQueue;
    public VideoSocketWrapper(VideoActivity3 ac, BlockingQueue sendq, BlockingQueue recvq){
        this.mainAC = ac;
        videoSendQueue = sendq;
        videoRecvQueue = recvq;

        connectVideoSocket();

        initFile();
    }

    private Socket videoSocket;
    private boolean isConnected = false;
    public void connectVideoSocket(){
        if (!isConnected){
            new ConnectVideoSocket().start();
            isConnected = true;
        }
    }
    class ConnectVideoSocket extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                videoSocket = new Socket("10.105.36.224",18888);
                sendStream = videoSocket.getOutputStream();
                recvSream = videoSocket.getInputStream();
            }catch (IOException e ){}

        }
    }


    private boolean isSendH264 = false;
    public void startSendH264(){
        if (!isSendH264){
            new SendThread().start();
            isSendH264 = true;
        }
    }

    private VideoActivity3 mainAC;
    private Socket send;
    private OutputStream sendStream;
    private InputStream recvSream;


    int totalPollcnt = 0;
    //get data frome queue
    class SendThread extends Thread{

        @Override
        public void run() {
            super.run();

//            try{
////                send = new Socket("10.1.1.1",8888);
//                send = new Socket("10.105.36.224",18888);
////                send = new Socket(mainAC.getSendIP(),18888);
//                Log.d(TAG,"success");
////                send = new Socket("10.1.1.1",8888);//obu
//                sendStream = send.getOutputStream();
//            }catch (IOException e){
//                Log.d(TAG,"worong");
//            }

            while(true){
//                if (isSendH264){
//                    if (!mainAC.getH264SendQueue().isEmpty()){
//                        byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
////                        Log.d(TAG,"type name"+mainAC.getH264SendQueue().poll().getClass().getName());
//                        Log.d(TAG,"type name"+tmp.getClass().getName());
//                        totalPollcnt++;
//                        try {
//                            H264fos.write(tmp, 0, tmp.length);
//                            Log.d(TAG,"size: "+mainAC.getH264SendQueue().size());
//                        } catch (IOException e) {}
//                    }else {
//                        Log.d(TAG,"queue empty");
//                    }
//                    Log.d(TAG,"   "+totalPollcnt);
//                    try {
//                        Thread.sleep(5);
//                    }catch (InterruptedException e){}
//                }

                if (isSendH264){
                    if(mainAC.getH264SendQueue().size()< 100){
                        try {
                            Thread.sleep(10);
                        }catch (InterruptedException e){}
                    }else{
                        try{
                            //maybe wrong
                            byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
                            if (sendStream != null){
    //                            sendStream.write(b);
                                sendStream.write(tmp);
                                sendStream.flush();
                                Log.d(TAG,"send one h264");
                                try {
                                    H264fos.write(tmp, 0, tmp.length);
                                } catch (IOException e) {
                                }
                            }
                        }catch (IOException e){}
                        try {
                            Thread.sleep(2);
                        }catch (InterruptedException e){}
                    }

                }
            }
        }
    }



    private boolean isRecv = false;
    public void startRecvH264(){
        if (!isRecv){
            new recvSocketThread().start();
            isRecv = true;
        }
    }
    class recvSocketThread extends Thread{
        @Override
        public void run() {
            super.run();

                mainAC.getH264RecvQueue().clear();
                while(true){
                    while(isRecv){
                            try {
                            byte[] readByte = new byte[2000];
                            int n;
                            while((n = recvSream.read(readByte))!=-1){
                                Log.d(TAG,"receive:"+mainAC.getH264RecvQueue().size());
//                                Log.d(TAG,""+mainAC.getH264RecvQueue().size());

                                byte[] toOffer = new byte[n];


                                //get timestamp
                                byte[] t = new byte[8];
                                System.arraycopy(readByte,0,t,0,8);

                                //get real data timestamp
//                                System.arraycopy(readByte,8,toOffer,0,n-8);

                                //real data without timestamp
//                                System.arraycopy(readByte,0,toOffer,0,n);
                                System.arraycopy(readByte,0,toOffer,0,n);

//                                mainAC.getH264RecvQueue().offer(toOffer);
                                mainAC.getH264RecvQueue().offer(toOffer);
                            }

                        }
                        catch (IOException e){
                            Log.d(TAG,"wrong");
                        }
                }
            }
        }
    }



    private FileOutputStream H264fos = null;
    public void initFile(){
        if (H264fos == null){
            try{
                Log.d(TAG,"init file");
                H264fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/carTempToSend.h264",true);
            }catch (FileNotFoundException e){
            }
        }
    }
}
