package com.example.deonew.car.Audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;

public class PlayAAC {

    private static final String TAG = "PlayAAC";
    public static final int KEY_CHANNEL_COUNT = 0;
    private Worker mWorker;
    private String path;//aac文件的路径。

    public PlayAAC(String filename) {
        this.path = filename;
        readFile();
    }
    AudioActivity2 mAudioAC = null;
    public PlayAAC(AudioActivity2 ac) {
        mAudioAC = ac;
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }

    }


    private class Worker extends Thread {
        private static final int KEY_SAMPLE_RATE = 0;
        private boolean isRunning = false;
        private AudioTrack mPlayer;
        private MediaCodec mDecoder;
        private MediaExtractor extractor;

        public void setRunning(boolean run) {
            isRunning = run;
        }

        @Override
        public void run() {
            super.run();
            if (!prepare()) {
                isRunning = false;
            }
            while (isRunning) {
                Log.d("aaaaaaaaa","decode");
                decode();
            }
            release();
        }

        private int sampleRate = 44100;
        //stereo
        private int channelConf = AudioFormat.CHANNEL_IN_STEREO;
        //data format
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private int miniBuffSize = 0;
        private InputStream fis = null;
        private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";
        public boolean prepare() {

            miniBuffSize= AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat)*2;
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConf, audioFormat, miniBuffSize, AudioTrack.MODE_STREAM);
            mPlayer.play();
            try {

                mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");

                MediaFormat mediaFormat = null;

                mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100,2);
                mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
                //adts header
                mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
                //csd-0
//                mediaFormat.setByteBuffer("csd-0", ByteBuffer.allocate(2).put(new byte[]{(byte) 0x11, (byte)0x90}));
                byte[] bytes = new byte[]{(byte) 0x11, (byte)0x90};
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                mediaFormat.setByteBuffer("csd-0", bb);
                mDecoder.configure(mediaFormat, null, null, 0);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (mDecoder == null) {
                Log.d(TAG,"decoder failed");
                return false;
            }
            mDecoder.start();
            return true;
        }

        public void decode() {

            final long kTimeOutUs = 5000;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;
            try {
                while (!sawOutputEOS) {
                    if (!sawInputEOS) {

                        Log.d(TAG,"start put data");
                        int inputBufIndex = mDecoder.dequeueInputBuffer(kTimeOutUs);
                        if (inputBufIndex >= 0) {
                            Log.d(TAG,"input available");
                            ByteBuffer dstBuf = mDecoder.getInputBuffer(inputBufIndex);
//                            int sampleSize = extractor.readSampleData(dstBuf, 0);//get data from extractor

                            //put a frame of audio file!!!!!!!!!!!!!!!
                            byte[] b = getOneAACFrame();
                            dstBuf.put(b);
                            int sampleSize = b.length;

                            Log.d(TAG,"one frame");

                            // -1 means no more availalbe
                            if (sampleSize < 0) {
                                sawInputEOS = true;
                                mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, System.nanoTime()/1000, 0);
                            }
                        }
                    }
                    int outputBufferIndex = mDecoder.dequeueOutputBuffer(info, kTimeOutUs);
                    if (outputBufferIndex >= 0) {
                        Log.d(TAG,"output available");
                        // Simply ignore codec config buffers.
                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                            Log.i("TAG", "audio encoder: codec config buffer");
                            mDecoder.releaseOutputBuffer(outputBufferIndex, false);
                            continue;
                        }
                        if (info.size != 0) {
                            ByteBuffer outBuf = mDecoder.getOutputBuffer(outputBufferIndex);//new api

                            outBuf.position(info.offset);
                            outBuf.limit(info.offset + info.size);
                            byte[] data = new byte[info.size];
                            outBuf.get(data);
                            mPlayer.write(data, 0, info.size);
                            Log.d(TAG,"write one");
                        }
                        mDecoder.releaseOutputBuffer(outputBufferIndex, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                            Log.d(TAG,"end");
                        }
                    }
                    else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat oformat = mDecoder.getOutputFormat();
                    }
                    //dequeueOutputBuffer time over, time = kTimeOutUs
                    else if(outputBufferIndex == INFO_TRY_AGAIN_LATER){

                    }
                }
            } finally {
//                extractor.release();
            }
        }

        private void release() {
            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();

            }
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }
    }
    private FileInputStream fis = null;
    private byte[] currentBuff = new byte[10240];
    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;
    private BlockingQueue<byte[]> q = new ArrayBlockingQueue<byte[]>(10000);
    public void readFile(){
        try{
            fis = new FileInputStream(this.path);
            int hhh = 0;
            do{
                //new every time
                byte[] i = new byte[1024];
                hhh = fis.read(i);
                q.offer(i);
            }while(hhh != -1);
        }catch (IOException e) {}
    }
    public byte[] getOneAACFrame(){
        int n = getNextIndex();
        if (n == -1) {
            return null;
        }
//        System.out.println(n);
        byte[] naluu = new byte[n-currentBuffStart];
        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);
        System.arraycopy(currentBuff, n, currentBuff, 0, currentBuff.length - n);
        currentBuffStart = 0;
        currentBuffEnd = currentBuffEnd - naluu.length;
        return naluu;
    }
    private int nextAACHead = -1;
    public int getNextIndex(){
        nextAACHead = getNextIndexOnce();
        while(nextAACHead == -1) {
            if (!mAudioAC.getAACRecvQueue().isEmpty()) {
                byte[] tmp = mAudioAC.getAACRecvQueue().poll();
                System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
                currentBuffEnd = currentBuffEnd + tmp.length;
                nextAACHead = getNextIndexOnce();
            }else{
                return -1;
            }
        }
        nextAACHead = nextAACHead - 3;
        return nextAACHead;
    }

    public int getNextIndexOnce(){
        int nextIndex = -1;
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(0xfff95080);
        byte[] naluHead = b.array();
        int i = 0;
        int index = 0;
        for(i = currentBuffStart+2; i < currentBuffEnd;i++){
            while (index > 0 && currentBuff[i] != naluHead[index]) {
                index = 0;
            }
            if (currentBuff[i] == naluHead[index]) {
                index++;
                if (index == 4){
                    nextIndex = i;
                    break;
                }
            }
        }
        return nextIndex;
    }
}