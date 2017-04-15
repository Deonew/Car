package com.example.deonew.car.Video;

import android.util.Log;

import com.example.deonew.car.VideoActivity2;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */

public class RecvH264 {
    private VideoActivity2 mVideoAC;
    public RecvH264(VideoActivity2 ac){
        //
        mVideoAC = ac;
    }
    public void init(){
        //mediacodec

        //recv thread
        new recvSocketThread().start();
        Log.d("sssssssssssssssss","11111111");
    }

    private boolean isRecv = false;
    private Socket recvSocket = null;
    private class recvSocketThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                recvSocket = new Socket("10.105.36.224",18888);
//                recvSocket = new Socket("192.168.1.105",18888);
//                        recvSocket = new Socket("10.1.1.1",8888);
                Log.d("ssssssssssssssss","okay");
                InputStream ins = recvSocket.getInputStream();

                mVideoAC.getH264RecvQueue().clear();
                while(true){
                    while(isRecv){
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = ins.read(readByte))!=-1){
                            Log.d("ssssssssssss","receive");
                            byte[] toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);
                            mVideoAC.getH264RecvQueue().offer(toOffer);
                            Log.d("ssssssssssssssss",""+mVideoAC.getH264RecvQueue().size());
                        }
                    }
                }
            }catch (IOException e){
                Log.d("ssssssssssssssss","wrong");
            }
        }
    }
}
