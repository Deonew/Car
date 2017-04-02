package com.example.deonew.car.Video;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    //get the ui thread handler
    public sendH264Thread(Handler h){
        this.msendUIHandler = h;
    }
    @Override
    public void run() {
        Looper.prepare();
        mRecUIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                //handle message here
//                sendToUIHandler("haha");

                switch (msg.what){
                    case 0x1:
                        sendFile();
                        sendToUIHandler("send file");
                        break;
                    case 0x2:
                        receiveFile();
                        sendToUIHandler("recv file");
                        break;
                }
            }
        };
        Looper.loop();
    }
    private InputStream mRecvInputSream=null;
    public InputStream getReceiveStream(){
        return mRecvInputSream;
    }
    public void request(){

    }
    public void receiveFile(){
        try {
            // Socket socket = new Socket("10.8.191.213",20000);
            Socket socket = new Socket("10.105.36.224",20000);
            //works
             File outf = new File("/storage/emulated/0/carTempRecv.264");
//            File outf = new File("./carTempRecv.264");
            FileOutputStream fos = new FileOutputStream(outf);
            //ins: read data from client
            mRecvInputSream = socket.getInputStream();
            //read
            int size  = 0;
            byte[] recBuff = new byte[1024];
            while((size = mRecvInputSream.read(recBuff))!=-1){
                fos.write(recBuff,0,size);
                fos.flush();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //send carTemp.h264 to server
    public void sendFile(){
        //file input stream
        FileInputStream fis = null;
        try{
            File sendf = new File("/storage/emulated/0/carTemp.h264");
            fis = new FileInputStream(sendf);
        }catch(FileNotFoundException e){}
        if(fis == null){
            sendToUIHandler("file error");
        }
        //socket output stream
        OutputStream os = null;
        try {
            // Socket socket = new Socket("10.8.191.213",20000);
            Socket socket = new Socket("10.105.36.224",20000);
            os = socket.getOutputStream();
            byte[] buff = new byte[1024];
            int size = 0;
            int cnt = 1;//debug
            while((size = fis.read(buff)) != -1){//read file
                //write and flush
                os.write(buff,0,size);
                os.flush();
            }
            sendToUIHandler("send success");
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
