package com.example.deonew.car.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 4/26/17.
 */

public class Test1 {
    private offer o;
    private fetch f;
    public static void main(String args[]){
        System.out.println("Test1");
        Test1 t = new Test1();
        t.init();
    }

    private BlockingQueue<byte[]> AACRecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue<byte[]> getAACRecvQueue() {
        return AACRecvQueue;
    }
    public void init(){
        o = new offer();
        o.start();
        f = new fetch();
        f.start();
    }
    class offer extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                byte [] b = new byte[2];
                b[0] = 'c';
                getAACRecvQueue().offer(b);
                System.out.println("offer: size:"+getAACRecvQueue().size());
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){}
            }
        }
    }
    class fetch extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                getAACRecvQueue().poll();
                System.out.println("fetch: size:"+getAACRecvQueue().size());
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){}
            }

        }
    }
}
