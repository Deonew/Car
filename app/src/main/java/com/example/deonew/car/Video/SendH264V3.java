package com.example.deonew.car.Video;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/13/17.
 */

public class SendH264V3 {
    private final String TAG = "SENDH264V3";
    private VideoActivity3 mainAC;
    private Socket send;
    private OutputStream sendStream;
    private SendThread sendThread;

    public SendH264V3(VideoActivity3 v){
        Log.d(TAG,"send h264s");
        this.mainAC = v;
        sendThread = new SendThread();
    }
    private boolean isSendingH264 = false;
    public void setSendH264Status(boolean value){
        isSendingH264 = value;
    }
    public void startSendH264(){
        sendThread.start();
    }
    public void connectToBox(){
        sendThread.connectToBox();
    }
    public void connectToPhone(){
        sendThread.connectToPhone();
    }

    //get data frome queue
    private class SendThread extends Thread{
        public void connectToBox(){
            try{
                send = new Socket("10.1.1.1",8888);
                Log.d("qqqqqqqqqqq","success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d("qqqqqqqqqqqqqqqqqqqqqqq","worong");
            }
        }
        public void connectToPhone(){
            try{
                send = new Socket("10.105.38.183",8888);
                Log.d("qqqqqqqqqqq","success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d("qqqqqqqqqqqqqqqqqqqqqqq","worong");
            }
        }

        @Override
        public void run() {
            super.run();

            try{
//                send = new Socket("10.1.1.1",8888);
                send = new Socket("10.105.36.224",18888);
                Log.d(TAG,"success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d(TAG,"worong");
            }

            while(true){
                if (!mainAC.getH264SendQueue().isEmpty()){
                    //
                    Log.d(TAG,"get one");
                    try{
                        //maybe wrong
                        byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
                        if (sendStream != null){
//                            sendStream.write(b);
                            sendStream.write(tmp);
                            sendStream.flush();
                        }
                    }catch (IOException e){}
                }
                try {
                    Thread.sleep(10);
                }catch (InterruptedException e){}
            }
        }
    }


}

