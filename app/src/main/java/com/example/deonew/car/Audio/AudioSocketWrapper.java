package com.example.deonew.car.Audio;

import android.os.Environment;
import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    private boolean isReceiveAAC = false;
    private InputStream recvSream = null;



    public AudioSocketWrapper(VideoActivity3 v, BlockingQueue sendq,BlockingQueue recvq){
        this.mainAC = v;
        this.audioSendQueue = sendq;
        this.audioRecvQueue = recvq;
//        sendTH

        //new ConnectSocket().start();

        sendTH = new sendAACThread();
        sendTH.start();
        recvTH = new recvAACThread();

        initFile();

        try {
            ds = new DatagramSocket(8889);
        }catch (IOException e){}
    }

    public void startSend(){
        isSendAAC = true;
    }

    public void stopSend(){
        isSendAAC = false;
    }

    public void startRecv(){
        Log.d(TAG,"recv");
        if (!isReceiveAAC){
            recvTH.start();
            isReceiveAAC = true;
        }
    }
    public void stopRecv(){
        isReceiveAAC = false;
    }

    private DatagramSocket ds = null;
    //------------------thread
    class sendAACThread extends Thread{
        @Override
        public void run() {
            super.run();

            int c = 0;
            while(true){
                //udp send
                if (isSendAAC){
                    if (!mainAC.getAACSendQueue().isEmpty()){
                        try{
                            byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
                            InetAddress sendAddr = InetAddress.getByName("10.202.0.202");
                            DatagramPacket dpSend = new DatagramPacket(tmp,tmp.length,sendAddr,8889);
                            ds.send(dpSend);
                            c++;
                            Log.d(TAG,"udp send one packet "+c);
                            try {
                                Thread.sleep(0);
                            }catch (InterruptedException e){}

                        }catch (IOException e){}
                    }
                }
            }
        }
    }




    class recvAACThread extends Thread{
        @Override
        public void run() {
            super.run();

            mainAC.getH264RecvQueue().clear();
            int c = 0;
            while(true){
                //udp receive
                if (isReceiveAAC){
                    byte[] recvBuff = new byte[1024];
                    DatagramPacket dpRecv = new DatagramPacket(recvBuff,1024);
                    try {
                        ds.receive(dpRecv);

                        byte[] buffer = dpRecv.getData();
                        int len = dpRecv.getLength();

                        //valid data
                        byte[] toOffer = new byte[len];
                        System.arraycopy(buffer,0,toOffer,0,len);
                        mainAC.getAACRecvQueue().offer(toOffer);
                        c++;
                        Log.d(TAG,"udp receive one packet, total: "+c);
                    }catch (IOException e){}
                }

//                if(isReceiveAAC && recvSream!=null){
//                    try {
//                        byte[] readByte = new byte[2000];
//                        int n;
//                        while((n = recvSream.read(readByte))!=-1){
////                                Log.d(TAG,"receive:"+mainAC.getH264RecvQueue().size());
////                                Log.d(TAG,""+mainAC.getH264RecvQueue().size());
//
//                            //without timestamp
//                            byte[] toOffer = new byte[n];
//                            System.arraycopy(readByte,0,toOffer,0,n);
//                            mainAC.getAACRecvQueue().offer(toOffer);
//
//                            //with timestamp
//                            //get timestamp
////                                byte[] t = new byte[8];
////                                byte[] toOffer = new byte[n-8];
////                                System.arraycopy(readByte,0,t,0,8);
////                                //data
////                                System.arraycopy(readByte,8,toOffer,0,n-8);
////                                mainAC.getH264RecvQueue().offer(toOffer);
//                            Log.d(TAG,"receive length:"+n+"");
//                        }
//                    }
//                    catch (IOException e){
//                        Log.d(TAG,"wrong");
//                    }
//                }
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
