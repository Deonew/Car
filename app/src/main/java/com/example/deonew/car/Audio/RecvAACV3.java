package com.example.deonew.car.Audio;

import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */

public class RecvAACV3 {
    private String TAG = "RecvAACV3";

    private VideoActivity3 mAudioAC;
    private Socket recvSocket;
    private boolean isRecv = false;

    public RecvAACV3(VideoActivity3 audioAC){
        mAudioAC = audioAC;
    }


    public void startRecvAAC(){
        if (!isRecv){
            isRecv = true;
            new RecvAACThread().start();
        }
    }
    class RecvAACThread extends Thread{
        @Override
        public void run() {
            super.run();
                try {
                    recvSocket = new Socket("10.105.36.224",18888);
//                    recvSocket = new Socket("192.168.1.105",18888);
    //                        recvSocket = new Socket("10.1.1.1",8888);

                    Log.d(TAG,"okay");
                    InputStream ins = recvSocket.getInputStream();

                    mAudioAC.getAACRecvQueue().clear();
                    while(true){
                        if (isRecv){
                            byte[] readByte = new byte[2000];
                            int n;
                            while((n = ins.read(readByte))!=-1){
                                Log.d(TAG,"receive");
                                byte[] toOffer = new byte[n];
                                System.arraycopy(readByte,0,toOffer,0,n);
                                mAudioAC.getAACRecvQueue().offer(toOffer);
                            }
                        }
                    }
                }catch (IOException e){
                    Log.d(TAG,"wrong");
                }

        }
    }
}
