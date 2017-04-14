package com.example.deonew.car.Audio;

import android.util.Log;

import com.example.deonew.car.Video.SendH264;
import com.example.deonew.car.Audio.AudioActivity2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by deonew on 4/14/17.
 */

public class SendAAC {

    private AudioActivity2 mainAC;
    private Socket send;
    private OutputStream sendStream;

    public SendAAC(AudioActivity2 v){
        this.mainAC = v;
    }
    public void startSendAAC(){
        new sendAACSocket().start();
    }
    //get data frome queue
    private class sendAACSocket extends Thread{
        @Override
        public void run() {
            super.run();

            try{
//                send = new Socket("10.105.36.224",8888);
                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){}


            while(true){
                if (!mainAC.getAACSendQueue().isEmpty()){
                    Log.d("aac","get one");
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
            }
        }
    }
}
