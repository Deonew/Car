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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AudioDecoder {

    private static final String TAG = "AudioDecoder";
    public static final int KEY_CHANNEL_COUNT = 0;
    private Worker mWorker;
    private String path;//aac文件的路径。

    public AudioDecoder(String filename) {
        this.path = filename;
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
//            try{
//                fis = new FileInputStream(new File(audioPath));
//            }catch (IOException e){}
            // 等待客户端
            miniBuffSize= AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat)*2;
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConf, audioFormat, miniBuffSize, AudioTrack.MODE_STREAM);
            mPlayer.play();
            try {
                mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");

                final String encodeFile = path;
                extractor = new MediaExtractor();
                extractor.setDataSource(encodeFile);

                MediaFormat mediaFormat = null;
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("audio/")) {
                        extractor.selectTrack(i);
                        mediaFormat = format;
                        break;
                    }
                }
                mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
                mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, KEY_CHANNEL_COUNT);
                mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, KEY_SAMPLE_RATE);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 24000);
                mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
                mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 0);
                mDecoder.configure(mediaFormat, null, null, 0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (mDecoder == null) {
                return false;
            }
            mDecoder.start();
            return true;
        }

        public void decode() {
            ByteBuffer[] codecInputBuffers = mDecoder.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = mDecoder.getOutputBuffers();

            final long kTimeOutUs = 5000;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;
            int totalRawSize = 0;
            try {
                while (!sawOutputEOS) {
                    if (!sawInputEOS) {

                        int inputBufIndex = mDecoder.dequeueInputBuffer(kTimeOutUs);
                        if (inputBufIndex >= 0) {
                            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                            int sampleSize = extractor.readSampleData(dstBuf, 0);//get data
                            Log.d(TAG,"取出一帧");//fff9 5080
                            byte [] content = new byte[dstBuf.limit()];
                            dstBuf.get(content);
                            for (int t = 0 ;t< content.length;t++){
                                Log.d(TAG,Integer.toHexString(content[t])+"one");
                            }
                            Log.d(TAG,"取出一帧完毕");
                            //从文件流读取byte数组
//                            byte[] b = new byte[miniBuffSize];
//                            byte[] b = new byte[miniBuffSize];
//                            int sampleSize = 0;
//                            try{
//                                sampleSize= fis.read(b);
//                            }catch (IOException e){}
//                            dstBuf.put(b);

//                            dstBuf = ByteBuffer.wrap(b);

                            //failed



                            // -1 means no more availalbe
                            if (sampleSize < 0) {
                                sawInputEOS = true;
                                mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
//                                mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, System.nanoTime()/1000, 0);

                                long presentationTimeUs = extractor.getSampleTime();
                                mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0);
                                extractor.advance();
                            }
                        }
                    }
                    int res = mDecoder.dequeueOutputBuffer(info, kTimeOutUs);
                    if (res >= 0) {

                        int outputBufIndex = res;
                        // Simply ignore codec config buffers.
                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                            Log.i("TAG", "audio encoder: codec config buffer");
                            mDecoder.releaseOutputBuffer(outputBufIndex, false);
                            continue;
                        }
                        if (info.size != 0) {
                            ByteBuffer outBuf = codecOutputBuffers[outputBufIndex];
                            outBuf.position(info.offset);
                            outBuf.limit(info.offset + info.size);
                            byte[] data = new byte[info.size];
                            outBuf.get(data);
                            totalRawSize += data.length;
                            mPlayer.write(data, 0, info.size);
                        }
                        mDecoder.releaseOutputBuffer(outputBufIndex, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                        }
                    } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        codecOutputBuffers = mDecoder.getOutputBuffers();
                    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat oformat = mDecoder.getOutputFormat();
                    }
                }
            } finally {
                // fosDecoder.close();
                extractor.release();
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
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }



    }
}