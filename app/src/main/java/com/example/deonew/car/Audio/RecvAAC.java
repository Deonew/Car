package com.example.deonew.car.Audio;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */

public class RecvAAC {

    private AudioActivity2 mAudioAC;
    private Socket recvSocket;
    private boolean isRecv = false;

    public RecvAAC(AudioActivity2 audioAC){
        mAudioAC = audioAC;
    }

    public void init(){
        //
        new RecvAACThread().start();
    }
    class RecvAACThread extends Thread{
        @Override
        public void run() {
            super.run();

//            while(true){
                try {
                    recvSocket = new Socket("10.105.36.224",18888);
//                    recvSocket = new Socket("192.168.1.105",18888);
    //                        recvSocket = new Socket("10.1.1.1",8888);

                    Log.d("ssssssssssssssss","okay");
                    InputStream ins = recvSocket.getInputStream();

                    mAudioAC.getAACRecvQueue().clear();
                    while(true){
//                        while(isRecv){
                            byte[] readByte = new byte[2000];
                            int n;
                            while((n = ins.read(readByte))!=-1){
                                Log.d("ssssssssssss","receive");
                                byte[] toOffer = new byte[n];
                                System.arraycopy(readByte,0,toOffer,0,n);
                                mAudioAC.getAACRecvQueue().offer(toOffer);
                            }
//                        }
                    }
                }catch (IOException e){
                    Log.d("ssssssssssssssss","wrong");
                }
//            }

        }
    }
}
