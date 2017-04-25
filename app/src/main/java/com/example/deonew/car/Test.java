package com.example.deonew.car;

/**
 * Created by deonew on 4/25/17.
 */

public class Test {
    public  static  void main(String args[]){
        long t1 = System.currentTimeMillis();
        try {
            Thread.sleep(10);
        }catch (InterruptedException e){}
        long t2 = System.currentTimeMillis();

        System.out.println(""+(t2-t1));
//        while (true){
//            System.out.println("Test"+System.currentTimeMillis());
//        }
//        System.exit(0);
    }
}
