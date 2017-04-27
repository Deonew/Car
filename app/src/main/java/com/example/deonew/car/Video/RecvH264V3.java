package com.example.deonew.car.Video;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */


public class RecvH264V3 {
    private final  String TAG = "RecvH264V3";
    private VideoActivity3 mVideoAC;
    public RecvH264V3(VideoActivity3 ac){
        //
        mVideoAC = ac;

    }
    public void startRecvH264(){

        //recv thread
        isRecv = true;
        if (isRecv){
            new recvSocketThread().start();
        }

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
                Log.d(TAG,"okay");
                InputStream ins = recvSocket.getInputStream();
                mVideoAC.getH264RecvQueue().clear();
                while(true){
                    while(isRecv){
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = ins.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            Log.d(TAG,""+mVideoAC.getH264RecvQueue().size());

                            byte[] toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);

                            //get timestamp
                            byte[] t = new byte[8];
                            System.arraycopy(readByte,0,t,0,8);

                            //get real data
                            System.arraycopy(readByte,8,toOffer,0,n-8);


                            mVideoAC.getH264RecvQueue().offer(toOffer);
                        }
                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
    }
}
