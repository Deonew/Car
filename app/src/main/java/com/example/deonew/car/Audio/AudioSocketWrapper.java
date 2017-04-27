package com.example.deonew.car.Audio;

import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 4/25/17.
 */

public class AudioSocketWrapper {
    private String TAG ="AudioSocketWrapper";

    private VideoActivity3 mainAC;
    private Socket audioSocket = null;
    private sendAACThread sendTH = null;
    private recvAACThread recvTH = null;
    private BlockingQueue audioSendQueue;
    private BlockingQueue audioRecvQueue;
    private boolean isSendingAac = false;
    private OutputStream sendStream = null;
    private boolean isRecv = false;
    private InputStream recvSream = null;



    public AudioSocketWrapper(VideoActivity3 v, BlockingQueue sendq,BlockingQueue recvq){
        this.mainAC = v;
        this.audioSendQueue = sendq;
        this.audioRecvQueue = recvq;
//        sendTH

        //new ConnectSocket().start();

        sendTH = new sendAACThread();
        recvTH = new recvAACThread();

    }

    //connect
    class ConnectSocket extends Thread{
        @Override
        public void run() {
            super.run();
            try {
//                audioSocket = new Socket("10.202.0.207",18888);
//                audioSocket = new Socket("10.105.36.224",18887);
                audioSocket = new Socket("10.105.36.224",18888);
                sendStream = audioSocket.getOutputStream();
                recvSream = audioSocket.getInputStream();

            }catch (IOException e){}
        }
    }

//    class ConnectSocket1 extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            try {
////                audioSocket = new Socket("10.202.0.207",18888);
////                audioSocket = new Socket("10.105.36.224",18887);
//                audioSocket = new Socket("10.105.36.224",18887);
//                sendStream = audioSocket.getOutputStream();
//                recvSream = audioSocket.getInputStream();
//
//            }catch (IOException e){}
//        }
//    }

    private boolean isConnected = false;
    public void startSend(){
        Log.d(TAG,"send");
        isSendingAac = true;
        if (sendTH != null){
            sendTH.start();
        }

        connectSocket();

    }
    public void connectSocket(){
        if (!isConnected){
            new ConnectSocket().start();
            isConnected = true;
        }
    }

    public void startRecv(){

        if (!isRecv){
            if (recvTH != null){
                recvTH.start();
            }
            isRecv = true;
        }
        connectSocket();
    }

    //------------------thread

    class sendAACThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                if (isSendingAac && sendStream!=null){
                    if (!audioSendQueue.isEmpty()){
                        try{
                            //maybe wrong
//                            byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
                            byte[] tmp = (byte[])audioSendQueue.poll();
                            if (sendStream != null){
                                sendStream.write(tmp);
                                sendStream.flush();
                                Log.d(TAG,"send one");
                            }
                            Log.d(TAG,"send one");
                        }catch (IOException e){}
                    }else {
                        Log.d(TAG,"send queue empty  "+mainAC.getAACSendQueue().size());
                    }
                    try {
                        Thread.sleep(1);
                    }catch (InterruptedException e){}
                }else {
                    Log.d(TAG,"socket err");
                }
            }
        }
    }




    class recvAACThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                while(true){
                    if (isRecv && recvSream!=null){
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = recvSream.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            byte[] audioData = new byte[n];
                            System.arraycopy(readByte,0,audioData,0,n);

                            //get timestamp

                            //get audio data
                            byte[] toOffer = new byte[n-8];
                            System.arraycopy(readByte,8,toOffer,0,n-8);
                            mainAC.getAACRecvQueue().offer(toOffer);
                        }
                    }

                    try{
                        Thread.sleep(20);
                    }catch (InterruptedException e){}

                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }

        }
    }
}
