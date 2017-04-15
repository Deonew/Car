package com.example.deonew.car.Video;

import android.media.MediaCodec;

import com.example.deonew.car.VideoActivity2;

import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/14/17.
 */

public class DecodeH264 {

    private VideoActivity2 mVideoAC;
    public DecodeH264(VideoActivity2 ac){
        mVideoAC = ac;
    }

    private byte[] currentBuff = new byte[102400];
    private byte[] naluHead = {0,0,0,1};
    private byte[] lsp = {0,1,2,0};
    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;
    int cnt = 0;

    public byte[] getOneNalu(){
        int n = getNextIndex();
//        Log.d(TAG,"get one"+n);
        byte[] naluu = new byte[n-currentBuffStart];
        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);

        //handle currentBuff
        System.arraycopy(currentBuff, n , currentBuff, 0, currentBuff.length - n);

        //set index
        currentBuffStart = 0;
        currentBuffEnd = currentBuffEnd - naluu.length;
        return naluu;
    }
    //added by deonew
    private int nextNaluHead = -1;
    public int getNextIndex(){
        nextNaluHead = getNextIndexOnce();

        //currentBuff don't contain a nalu
        //poll data
        while(nextNaluHead == -1) {
            if (mVideoAC.getH264RecvQueue().isEmpty()){break;}
//                break;
            byte[] tmp = (byte[])mVideoAC.getH264RecvQueue().poll();

            System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
            currentBuffEnd = currentBuffEnd + tmp.length;
            nextNaluHead = getNextIndexOnce();
            cnt++;
//            Log.d(TAG,"poll"+cnt);
        }
        nextNaluHead = nextNaluHead - 3;
        // currentBuffStart = nextNaluHead;
        return nextNaluHead;
    }

    //get next 000000[01]
    public int getNextIndexOnce(){
        int nextIndex = -1;
        byte[] naluHead = {0,0,0,1};
        byte[] correctBuff = {0,1,2,0};
        int i = 0;
        int index = 0;
        for(i = currentBuffStart+1; i < currentBuffEnd;i++){
            while (index > 0 && currentBuff[i] != naluHead[index]) {
                index = correctBuff[index - 1];
            }
            if (currentBuff[i] == naluHead[index]) {
                index++;
                if (index == 4){
                    nextIndex = i;//i = 00000001中的01
                    break;
                }
            }
        }
        return nextIndex;
    }




    private MediaCodec playH264Codec;
    private class decodeH264Th extends Thread{
        @Override
        public void run() {
            super.run();

            //
            decode();
        }

        boolean mStopFlag = false;
        private void decode(){
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            long timeoutUs = 10000;

            //            int bytes_cnt = 0;
                while (mStopFlag == false){
                    while (!mStopFlag){
                        int inIndex = playH264Codec.dequeueInputBuffer(timeoutUs);
                        if (inIndex >= 0) {
                            ByteBuffer byteBuffer = playH264Codec.getInputBuffer(inIndex);
            //                        ByteBuffer byteBuffer = inputBuffers[inIndex];
                            byteBuffer.clear();

                            //放入一帧数据
            //                        byte[] b = getAFrame();
            //                        byteBuffer.put(b);

                            //队列获取文件数据
                            byte[] b = getOneNalu();
                            byteBuffer.put(b);
                            try{
                                Thread.sleep(30);
                            }catch (InterruptedException e){}
            //                        Log.d("llllllll",b.length+"");
                            playH264Codec.queueInputBuffer(inIndex, 0, b.length, 0, 0);

                        } else {
                            continue;
                        }
                        int outIndex = playH264Codec.dequeueOutputBuffer(info, timeoutUs);
                        if (outIndex >= 0) {
                            while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            boolean doRender = (info.size != 0);
                            playH264Codec.releaseOutputBuffer(outIndex, doRender);

                            playH264Codec.getOutputBuffer(outIndex);

                        } else {
                        }
                    }
                    mStopFlag = true;
                }

        }

    }
}
