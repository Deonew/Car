package com.example.deonew.car.Video;

import android.app.Activity;
import android.os.Bundle;

import com.example.deonew.car.R;
import com.example.deonew.car.Video.camera.Camera2BasicFragment;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VideoActivity3 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video3);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }



    private SendH264 sendH264;
    private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264SendQueue(){
        return H264SendQueue;
    }
    public void offerSendH264Queue(byte[] b){
        int n = b.length/1000;
        for(int i = 0;i< n+1;i++){
            int len = 1000;
            if (i == n){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            getH264SendQueue().offer(tmp);
            i++;

        }
    }

    public void putI420(byte[] b){
        //
    }

}
