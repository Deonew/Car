package com.example.deonew.car.Test;

import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/26/17.
 */

public class Test2 {
    public static void main(String args[]){

        //long to byte[]
        ByteBuffer bf = ByteBuffer.allocate(8);
        long l = 1111001010;
        bf.putLong(0,l);
        byte [] b = bf.array();
        System.out.println(" "+ b[5]);
        System.out.println(" "+ b[6]);
        System.out.println(" "+ b[7]);


        //byte[] to long
//        byte[] bb = new byte[8];
//        bf.put(bb);

        bf.put(b);
        bf.flip();
        long ll = bf.getLong();
        System.out.println(" "+ ll);

//        System.out.println(" "+ bf.array().length);
//        System.out.println(" "+ bf.array()[0]);
//        System.out.println(" "+ bf.array()[1]);
//        System.out.println(" "+ bf.array()[2]);
//        System.out.println(" "+ bf.array()[3]);

    }
}
