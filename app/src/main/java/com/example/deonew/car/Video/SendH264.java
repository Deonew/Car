package com.example.deonew.car.Video;

import android.util.Log;

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
    private boolean isSendingH264 = false;
    public void setSendH264Status(boolean value){
        isSendingH264 = value;
    }
    public void startSendH264(){
        new sendSocket().start();
    }
    //get data frome queue
    private class sendSocket extends Thread{
        @Override
        public void run() {
            super.run();

            try{
//                send = new Socket("10.105.36.224",8888);
                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){}


            Log.d("sssssssssssssssssssss","send");
            while(true){
                if (!mainAC.getH264SendQueue().isEmpty()){
                    //
                    Log.d("H264","get one");

                    //test
//                    byte[] b = new byte[3];
//                    b[0] = b[1]= b[2] = 'c';
                    try{
                    //maybe wrong
                        byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
                        if (sendStream != null){
//                            sendStream.write(b);
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

