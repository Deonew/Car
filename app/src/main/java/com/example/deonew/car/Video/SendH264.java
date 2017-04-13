package com.example.deonew.car.Video;

import android.util.Log;

import com.example.deonew.car.VideoActivity2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/13/17.
 */

public class SendH264 {
    private VideoActivity2 mainAC;
    private Socket send;
    private OutputStream sendStream;

    public SendH264(VideoActivity2 v){
        this.mainAC = v;
    }
    public void init(){


        new sendSocket().start();

    }
    //get data frome queue
    private class sendSocket extends Thread{
        @Override
        public void run() {
            super.run();

            try{
//                send = new Socket("10.105.36.224",8888);
                send = new Socket("10.1.1.1",8888);
                sendStream = send.getOutputStream();
            }catch (IOException e){}


            while(true){
                if (!mainAC.getVideoSendQueue().isEmpty()){
                    //
                    Log.d("H264","get one");

                    try{
                    //maybe wrong
                        byte[] tmp = (byte[])mainAC.getVideoSendQueue().poll();
                        if (sendStream != null){
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

