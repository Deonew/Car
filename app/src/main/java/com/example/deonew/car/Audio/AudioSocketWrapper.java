package com.example.deonew.car.Audio;

import android.os.Environment;
import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private boolean isSendAAC = false;
    private OutputStream sendStream = null;
    private boolean isRecvAAC = false;
    private InputStream recvSream = null;



    public AudioSocketWrapper(VideoActivity3 v, BlockingQueue sendq,BlockingQueue recvq){
        this.mainAC = v;
        this.audioSendQueue = sendq;
        this.audioRecvQueue = recvq;
//        sendTH

        //new ConnectSocket().start();

        sendTH = new sendAACThread();
        recvTH = new recvAACThread();

        initFile();
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
    public void connectSocket(){
        if (!isConnected){
            new ConnectSocket().start();
            isConnected = true;
        }
    }

    public void startSend(){
        connectSocket();
        Log.d(TAG,"send");
        if (!isSendAAC){
            sendTH.start();
            isSendAAC = true;
        }
    }

    public void startRecv(){
        connectSocket();
        Log.d(TAG,"recv");
        if (!isRecvAAC){
            recvTH.start();
            isRecvAAC = true;
        }
    }

    //------------------thread

    class sendAACThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
//                if (isSendAAC){
//                    if (!mainAC.getAACSendQueue().isEmpty()){
//                        byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
//                        try {
//                            AACfos.write(tmp, 0, tmp.length);
//                            Log.d(TAG,"size: "+mainAC.getAACSendQueue().size());
//                        } catch (IOException e) {}
//                    }else {
//                        Log.d(TAG,"queue empty");
//                    }
//                    try {
//                        Thread.sleep(5);
//                    }catch (InterruptedException e){}
//                }

                if (isSendAAC && sendStream!=null){
//                    if (mainAC.getAACSendQueue().size()<100){
//                        try {
//                            Thread.sleep(10);
//                        }catch (InterruptedException e){}
//                    }

                    if (!mainAC.getAACSendQueue().isEmpty()){
                        try{
                            //maybe wrong
                            byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
                            if (sendStream != null){
                                sendStream.write(tmp);
                                sendStream.flush();
                                Log.d(TAG,"send one");
                            }
                        }catch (IOException e){}
                    }

                }else {
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){}
                }

//                if (isSendAAC && sendStream!=null){
//                    if (!audioSendQueue.isEmpty()){
//                        try{
//                            //maybe wrong
////                            byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
//                            byte[] tmp = (byte[])audioSendQueue.poll();
//                            if (sendStream != null){
//                                sendStream.write(tmp);
//                                sendStream.flush();
//                                Log.d(TAG,"send one");
//                            }
//                            Log.d(TAG,"send one");
//                        }catch (IOException e){}
//                    }else {
//                        Log.d(TAG,"send queue empty  "+mainAC.getAACSendQueue().size());
//                    }
//                    try {
//                        Thread.sleep(1);
//                    }catch (InterruptedException e){}
//                }else {
//                    Log.d(TAG,"socket err");
//                }
            }
        }
    }




    class recvAACThread extends Thread{
        @Override
        public void run() {
            super.run();

            mainAC.getH264RecvQueue().clear();
            try {
                while(true){

                    if (isRecvAAC && recvSream!=null){
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = recvSream.read(readByte))!=-1){
                            Log.d(TAG,"receive");

                            //with timestamp
//                            byte[] audioData = new byte[n];
//                            System.arraycopy(readByte,0,audioData,0,n);
//                            //get timestamp
//                            //get audio data
//                            byte[] toOffer = new byte[n-8];
//                            System.arraycopy(readByte,8,toOffer,0,n-8);

                            //without timestamp
                            byte[]toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);
                            mainAC.getAACRecvQueue().offer(toOffer);
                            Log.d(TAG,"recv "+mainAC.getAACRecvQueue().size());
                        }
                    }else {
                        try{
                            Thread.sleep(20);
                        }catch (InterruptedException e){}
                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
    }


    private FileOutputStream AACfos = null;
    public void initFile(){
        if (AACfos == null){
            try{
                Log.d(TAG,"init audio file");
                AACfos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/carTempA.aac",true);
            }catch (FileNotFoundException e){
            }
        }
    }
}
