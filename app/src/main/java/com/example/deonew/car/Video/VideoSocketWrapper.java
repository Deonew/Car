package com.example.deonew.car.Video;

import android.os.Environment;
import android.util.Log;

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

        initFile();

        //            DatagramSocket dds = getSendDs();
        try {
            ds = new DatagramSocket(8889);
        }catch (IOException e){}

    }

    private boolean isSendH264 = false;
    public void startSendH264(){
        if (!isSendH264){
            new SendThread().start();
            isSendH264 = true;
        }
    }
    public void stopSendH264(){
        isSendH264 = false;
    }

    private VideoActivity3 mainAC;
    private Socket send;
    private OutputStream sendStream;
    private InputStream recvSream;


    private DatagramSocket ds = null;

    int totalPollcnt = 0;
    //get data frome queue
    class SendThread extends Thread{

        @Override
        public void run() {
            super.run();

            int c = 0;
            while(true){

                //udp send
                if (isSendH264){
                    if (!mainAC.getH264SendQueue().isEmpty()){
                        try{
                            byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
                            InetAddress sendAddr = InetAddress.getByName("10.202.0.202");
                            DatagramPacket dpSend = new DatagramPacket(tmp,tmp.length,sendAddr,8888);
                            getSendDs().send(dpSend);
                            c++;
                            Log.d(TAG,"udp send one packet "+c);
                            try {
                                Thread.sleep(0);
                            }catch (InterruptedException e){}

                        }catch (IOException e){}
                    }
                }

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


                //tcp send
//                if (isSendH264){
//                    if(mainAC.getH264SendQueue().size()< 100){
//                        try {
//                            Thread.sleep(3);
//                        }catch (InterruptedException e){}
//                    }else{
//                        try{
//                            //maybe wrong
//                            byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
//                            if (sendStream != null){
//    //                            sendStream.write(b);
//                                sendStream.write(tmp,0,tmp.length);
////                                sendStream.flush();
//                                Log.d(TAG,"send one h264, length:"+tmp.length);
//                                try {
//                                    H264fos.write(tmp, 0, tmp.length);
//                                } catch (IOException e) {
//                                }
//
////                                try {
////                                    Thread.sleep(1);
////                                }catch (InterruptedException e){}
//                            }
//                        }catch (IOException e){}
//                        try {
//                            Thread.sleep(10);
//                        }catch (InterruptedException e){}
//                    }
//                }


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
    public DatagramSocket getSendDs(){
        return ds;
    }


    class recvSocketThread extends Thread{
        @Override
        public void run() {
            super.run();

                mainAC.getH264RecvQueue().clear();
                while(true){
                    //udp receive
                    if (isRecv){
                        byte[] recvBuff = new byte[1024];
                        DatagramPacket dpRecv = new DatagramPacket(recvBuff,1024);
                        try {
                            getSendDs().receive(dpRecv);

                            byte[] buffer = dpRecv.getData();
                            int len = dpRecv.getLength();

                            //valid data
//                            byte[] toOffer = new byte[len];
//                            System.arraycopy(buffer,0,toOffer,0,len);

//                          with timestamp
//                           get timestamp
                            byte[] t = new byte[8];
                            System.arraycopy(buffer,0,t,0,8);
                            byte[] toOffer = new byte[len-8];
                            System.arraycopy(buffer,8,toOffer,0,len-8);
                            mainAC.getH264RecvQueue().offer(toOffer);
                            Log.d(TAG,"udp receive one packet"+dpRecv.getData().length+" "+dpRecv.getLength());
                        }catch (IOException e){}
                    }

                    //tcp receive
//                    while(isRecv){
//                        try {
//                            byte[] readByte = new byte[2000];
//                            int n;
//                            while((n = recvSream.read(readByte))!=-1){
////                                Log.d(TAG,"receive:"+mainAC.getH264RecvQueue().size());
////                                Log.d(TAG,""+mainAC.getH264RecvQueue().size());
//
//                                //without timestamp
//                                byte[] toOffer = new byte[n];
//                                System.arraycopy(readByte,0,toOffer,0,n);
//                                mainAC.getH264RecvQueue().offer(toOffer);
//
//                                //with timestamp
//                                //get timestamp
////                                byte[] t = new byte[8];
////                                System.arraycopy(readByte,0,t,0,8);
//                                //data
////                                byte[] toOffer = new byte[n-8];
////                                System.arraycopy(readByte,8,toOffer,0,n-8);
////                                mainAC.getH264RecvQueue().offer(toOffer);
//
//                                Log.d(TAG,"receive length:"+n+"");
//
//                            }
//
//                        }
//                        catch (IOException e){
//                            Log.d(TAG,"wrong");
//                        }
//                }


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
