package com.example.deonew.car.Video;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by deonew on 17-4-3.
 */


public class RecvH264Run implements Runnable {
    private InputStream mRecvInputSream=null;
    public InputStream getReceiveStream(){
        return mRecvInputSream;
    }

    private Handler mUIHandler;
    public Handler mRecUIHandler = null;
    //get ui handler
    public RecvH264Run(Handler h){
        this.mUIHandler = h;
    }
    @Override
    public void run() {
        Looper.prepare();
        //startSendH264 handler to recv command from ui thread
        mRecUIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0x2://recv file
//                        receiveFile();
//                        sendToUIHandler("recv file");
                        break;

                }
            }
        };
        Looper.loop();

//        while(true){
//            try {
//                // Socket socket = new Socket("10.8.191.213",20000);
//                Socket socket = new Socket(" 10.105.36.224",20000);
//                //works
//                File outf = new File("/storage/emulated/0/carTempRecv.264");
////            File outf = new File("./carTempRecv.264");
//                FileOutputStream fos = new FileOutputStream(outf);
//                //ins: read data from client
//                mRecvInputSream = socket.getInputStream();
//
//
//                //read
//                int size  = 0;
//                byte[] recBuff = new byte[1024];
//                while((size = mRecvInputSream.read(recBuff))!=-1){
//                    fos.write(recBuff,0,size);
//                    fos.flush();
//                }
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
    }


//
//    public void initSocket(){
//    }
}
