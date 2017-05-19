package com.example.deonew.car.Audio;

import android.util.Log;

import com.example.deonew.car.Video.VideoActivity3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */

public class SendAACV3 {
    private String TAG = "SendAACV3";

    private VideoActivity3 mainAC;
    private Socket send;
    private OutputStream sendStream;

    public SendAACV3(VideoActivity3 v){
        this.mainAC = v;
    }
    public void startSendAAC(){
        new sendAACSocket().start();
    }
    private boolean isSendingAac = false;
    public void setSendAacStatus(boolean value){
        isSendingAac = value;
    }

    //get data frome queue
    private class sendAACSocket extends Thread{
        @Override
        public void run() {
            super.run();

            try{
                send = new Socket("10.105.36.224",8888);
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){}


            while(true){
                if (isSendingAac && sendStream!=null){
                    if (!mainAC.getAACSendQueue().isEmpty()){
                        Log.d("aac","send one");
                        try{
                            //maybe wrong
                            byte[] tmp = (byte[])mainAC.getAACSendQueue().poll();
                            if (sendStream != null){
                                sendStream.write(tmp);
                                sendStream.flush();
                            }
                        }catch (IOException e){}

                    }
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){}
                }else {
                    Log.d(TAG,"socket err");
                }

                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){}

            }
        }
    }
}
