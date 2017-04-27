package com.example.deonew.car;

import android.util.Log;

/**
 * Created by deonew on 4/27/17.
 */

public class Test3 {
    public static void main(String aegs[]){
        byte[] b = new byte[2000];
        int n = b.length/1000;
        int m = b.length%1000;
        System.out.println(n);
        if (m !=0){
            n++;
        }
        for(int i = 0;i< n;i++){
            int len = 1000;
            if (i == (n -1)){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.out.println(i*1000);
            System.out.println(len);

            System.arraycopy(b,i*1000,tmp,0,len);
            System.out.println("pack "+i+ " length:"+len);
        }
    }
}
