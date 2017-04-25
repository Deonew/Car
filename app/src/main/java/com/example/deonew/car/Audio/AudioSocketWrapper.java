package com.example.deonew.car.Audio;

import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/25/17.
 */

public class AudioSocketWrapper {
    private String TAG ="AudioSocketWrapper";

    private VideoActivity3 mainAC;
    private Socket audioSocket;
    private sendAACThread sendTH = null;
    private recvAACThread recvTH = null;
    public AudioSocketWrapper(VideoActivity3 v){
        this.mainAC = v;
//        sendTH

        new ConnectSocket().start();

        sendTH = new sendAACThread();
        recvTH = new recvAACThread();

    }


    //connect
    class ConnectSocket extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                audioSocket = new Socket("10.202.0.207",18888);
                sendStream = audioSocket.getOutputStream();
                recvSream = audioSocket.getInputStream();
            }catch (IOException e){}
        }
    }

    public void startSend(){
        Log.d(TAG,"send");
        isSendingAac = true;
        if (sendTH != null){
            sendTH.start();
        }
    }

    public void startRecv(){
        if (recvTH != null){
            recvTH.start();
        }
    }



    //------------------thread

    private boolean isSendingAac = false;
    private OutputStream sendStream;
    private class sendAACThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                if (isSendingAac && sendStream!=null){
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
                    }else {
                        Log.d(TAG,"queue empty");
                    }
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){}
                }else {
                    Log.d(TAG,"socket err");
                }
            }
        }
    }



    private boolean isRecv = false;
    private InputStream recvSream = null;
    class recvAACThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
//                recvSocket = new Socket("10.105.36.224",18888);
//                    recvSocket = new Socket("192.168.1.105",18888);
                //                        recvSocket = new Socket("10.1.1.1",8888);

//                Log.d(TAG,"okay");
//                InputStream recvSream = recvSocket.getInputStream();

//                mAudioAC.getAACRecvQueue().clear();
                while(true){
                    if (isRecv){
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = recvSream.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            byte[] toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);
                            mainAC.getAACRecvQueue().offer(toOffer);
                        }
                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }

        }
    }
}
