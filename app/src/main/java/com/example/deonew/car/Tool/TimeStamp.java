package com.example.deonew.car.Tool;

import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/26/17.
 */

public class TimeStamp {
    public byte[] getTimeStamp(){
        long t = System.currentTimeMillis();
        ByteBuffer bf = ByteBuffer.allocate(8);
        bf.putLong(0,t);
        byte [] b = bf.array();
        return b;
    }
}
