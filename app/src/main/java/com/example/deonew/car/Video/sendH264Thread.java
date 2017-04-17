package com.example.deonew.car.Video;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 17-4-2.
 */

public class sendH264Thread implements Runnable{

    //handler to receive message from ui thread
    public Handler mRecUIHandler = null;
    //handler to send message to ui thread
    Handler msendUIHandler = null;
    private Activity mVideoAC = null;

    private Socket mSendSocket = null;//
    private OutputStream sendOs = null;//write output data to server
    //get the ui thread handler
    private FileOutputStream fos=null;//write data to local file

    public sendH264Thread(Handler h,Activity ac){
        this.mVideoAC = ac;
        this.msendUIHandler = h;
    }
    @Override
    public void run() {
        Looper.prepare();
        mRecUIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        };
        Looper.loop();

//        File outf = new File("/storage/emulated/0/carTempRecvvvv.264");
//        try{
//            fos = new FileOutputStream(outf);
//        }catch (IOException e){}
//        //read
//        int size  = 0;
//        byte[] recBuff = new byte[1024];
//        //rec
//        while(true){
//            try {
//                //works
//                while((size = mRecvInputSream.read(recBuff))!=-1){
//                    fos.write(recBuff,0,size);
//                    fos.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }
    private InputStream mRecvInputSream=null;
    public InputStream getReceiveStream(){
        return mRecvInputSream;
    }

    //send carTemp.h264 to server
    public void sendFile(byte[] b){
        //send from file
        //works
        //file input stream
//        FileInputStream fis = null;
//        try{
//            File sendf = new File("/storage/emulated/0/carTemp.h264");
//            fis = new FileInputStream(sendf);
//        }catch(FileNotFoundException e){}
//        if(fis == null){
//            sendToUIHandler("file error");
//        }
        //socket output stream
//        OutputStream sendOs = null;
        if (mSendSocket == null || sendOs == null){
            try {
                // Socket socket = new Socket("10.8.191.213",20000);
                mSendSocket = new Socket("10.105.36.224",20000);
                sendOs = mSendSocket.getOutputStream();
                mRecvInputSream = mSendSocket.getInputStream();
            }catch (IOException e){}
        }
        try {
            // Socket socket = new Socket("10.8.191.213",20000);
//            Socket socket = new Socket("10.105.36.224",20000);
//            sendOs = socket.getOutputStream();
            sendOs.write(b,0,b.length);
            sendOs.flush();
//            byte[] buff = new byte[1024];
//            int size = 0;
//            int cnt = 1;//debug
//            while((size = fis.read(buff)) != -1){//read file
//                //write and flush
//                sendOs.write(buff,0,size);
//                sendOs.flush();
//            }
//            sendToUIHandler("send success");
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void receiveFile(){
//        try {
//            // Socket socket = new Socket("10.8.191.213",20000);
//            Socket socket = new Socket(" 10.105.36.224",20000);
//            //works
//            File outf = new File("/storage/emulated/0/carTempRecv.264");
////            File outf = new File("./carTempRecv.264");
//            FileOutputStream fos = new FileOutputStream(outf);
//            //ins: read data from client
//            mRecvInputSream = socket.getInputStream();
//            //read
//            int size  = 0;
//            byte[] recBuff = new byte[1024];
//            while((size = mRecvInputSream.read(recBuff))!=-1){
//                fos.write(recBuff,0,size);
//                fos.flush();
//                //send data to ui thread
//
//            }
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            // Socket socket = new Socket("10.8.191.213",20000);
            Socket socket = new Socket("10.105.39.47",20000);
            //works
            // File outf = new File("/storage/emulated/0/carTempRecv.264");
            File outf = new File("/storage/emulated/0/carTempRecv1.264");
            FileOutputStream fos = new FileOutputStream(outf);
            //ins: read data from client
            InputStream ins = socket.getInputStream();
            //read
            int size  = 0;
//            byte[] recBuff = new byte[3072];
            byte[] recBuff = new byte[2600];
            while((size = ins.read(recBuff))!=-1){
                //write to file
//                fos.write(recBuff,0,size);
//                fos.flush();


                //send data to mainhandler
                Message m = new Message();
                m.what = 0x3;
                m.obj = recBuff;
                msendUIHandler.sendMessage(m);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //debug
    public void sendToUIHandler(String s){
        Message m = new Message();
        m.obj = s;
        msendUIHandler.sendMessage(m);
    }
}
