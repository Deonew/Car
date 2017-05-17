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

import com.example.deonew.car.Tool.TimeStamp;
import com.example.deonew.car.Video.VideoActivity3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;

public class PlayAACV3 {

    private static final String TAG = "PlayAACV3";
    private decodeAAC decodeAACTH;
    private BlockingQueue<byte[]> recvQueue;
    private String path;//aac文件的路径。

//    public PlayAACV3(String filename) {
//        this.path = filename;
//        readFile();
//    }
    private VideoActivity3 mainAC = null;
    public PlayAACV3(VideoActivity3 ac) {
        mainAC = ac;
        this.recvQueue = mainAC.getAACRecvQueue();


        decodeAACTH = new decodeAAC();
        decodeAACTH.start();
    }

    private boolean isSynchroning = false;
    public void audioSleep(long l){
        if (!isSynchroning){
            isSynchroning = true;
            decodeAACTH.setRunning(false);
            //set timer
            Timer t = new Timer();
            t.schedule(new TimerT(),l);
        }
    }



    public void start() {
        Log.d(TAG,"start play");
        decodeAACTH.setRunning(true);
    }

    public void stop() {
        if (decodeAACTH != null) {
            decodeAACTH.setRunning(false);
            decodeAACTH = null;
        }

    }

    class decodeAAC extends Thread {
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
            decode();
        }

        private int sampleRate = 44100;
        //stereo
//        private int channelConf = AudioFormat.CHANNEL_IN_MONO;
        private int channelConf = AudioFormat.CHANNEL_IN_STEREO;
        //data format
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private int channelCount = 2;
        private int miniBuffSize;
        private InputStream fis = null;
        private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";
        public boolean prepare() {

            miniBuffSize= AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat);
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConf, audioFormat, miniBuffSize, AudioTrack.MODE_STREAM);
            mPlayer.play();
            try {

                mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");

                MediaFormat mediaFormat = null;

                mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate,channelCount);
//                mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
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
            while (true){
                if (isRunning){
                    if (recvQueue.size()>0){
                    Log.d(TAG,"laset queue size: "+ recvQueue.size());

                    int inputBufIndex = mDecoder.dequeueInputBuffer(kTimeOutUs);
                    if (inputBufIndex >= 0) {
                        Log.d(TAG,"input available");
                        ByteBuffer dstBuf = mDecoder.getInputBuffer(inputBufIndex);
                        dstBuf.clear();
//                            int sampleSize = extractor.readSampleData(dstBuf, 0);//get data from extractor

                        byte[] b = null;
                        for (int t = 0;t<1;t++){
                            //put a frame of audio  file!!!!!!!!!!!!!!!
                            b = getOneAACFrame();
                        }
                        if (b == null){
//                            b= new byte[]{0xf, 0xf, 0xf, 0x9,0x5,0x0,0x8,0x0};//0xfff95080
                            b= new byte[]{(byte)0x0B, (byte)0x00, (byte)0x21, (byte)0x10, (byte)0x05, (byte)0x00, (byte)0xA0, (byte)0x19, (byte)0x33, (byte)0x87, (byte)0xC0, (byte)0x00, (byte)0x7E};
                        }

                        dstBuf.put(b);
                        int sampleSize = b.length;

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

                            mPlayer.write(data, 0, data.length);
                            Log.d(TAG,"write one");

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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

        //check head
//        ByteBuffer b = ByteBuffer.allocate(4);
//        b.putInt(0xfff95080);
//        byte[] naluHead = b.array();
//        if (naluu[0]!=naluHead[0] || naluu[1]!=naluHead[1] || naluu[1]!=naluHead[1] || naluu[1]!=naluHead[1]){
//            naluu = naluHead;
//        }


        return naluu;
    }
    private int nextAACHead = -1;
    public int getNextIndex(){
        nextAACHead = getNextIndexOnce();
        while(nextAACHead == -1) {
            if (!recvQueue.isEmpty()) {
                byte[] tmp = recvQueue.poll();
                int len = tmp.length;

                //set timestamp
                byte[] tBytes = new byte[8];
                System.arraycopy(tmp,0,tBytes,0,8);
                ByteBuffer bf = ByteBuffer.allocate(8);
                bf.put(tBytes);
                bf.flip();
                long ts = bf.getLong();
                TimeStamp.setAudioStamp(ts);

                Log.d(TAG,"time "+ts);

                byte[] validData = new byte[len-8];
                System.arraycopy(tmp,8,validData,0,len-8);
                System.arraycopy(validData,0,currentBuff,currentBuffEnd,validData.length);
                currentBuffEnd = currentBuffEnd + validData.length;
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
//        b.putInt(0xfff15080);//aac file
        b.putInt(0xfff95080);//record
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

    class TimerT extends TimerTask{
        @Override
        public void run() {
            isSynchroning = false;
            decodeAACTH.setRunning(true);
        }
    }
}