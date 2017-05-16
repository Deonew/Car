package com.example.deonew.car.Tool;

import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/26/17.
 */

public class TimeStamp {
    private static long currentAudioStamp = System.currentTimeMillis();;
    private static long currentVideoStamp = System.currentTimeMillis();;

    static public long getAudioStamp(){
        return currentAudioStamp;
    }
    static public void setAudioStamp(long ast){
        currentAudioStamp = ast;
    }
    static public long getVideoStamp(){
        return currentVideoStamp;
    }
    static public void setVideoStamp(long vst){
        currentVideoStamp = vst;
    }

    public byte[] getTimeStamp(){
        long t = System.currentTimeMillis();
        ByteBuffer bf = ByteBuffer.allocate(8);
        bf.putLong(0,t);
        byte [] b = bf.array();
        return b;
    }
}
