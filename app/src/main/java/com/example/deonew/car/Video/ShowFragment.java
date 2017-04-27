package com.example.deonew.car.Video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.example.deonew.car.R;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by deonew on 4/21/17.
 */

public class ShowFragment extends Fragment {
    private final String TAG = "ShowFragment";

    VideoActivity3 mainAC;
    public ShowFragment(){
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_video_show, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

//        initMediaCodec();
//        new recvSocketThread().start();
//        new decodeH2Thread().start();
//        getActivity().findViewById(R.id.videoTextureView);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mainAC = (VideoActivity3) getActivity();
        initMediaCodec();
    }

    private MediaCodec mPlayCodec;
    private int Video_Width = 500;
    private int Video_Height = 300;
    private int PlayFrameRate = 15;
    private Boolean isUsePpsAndSps = false;
    private SurfaceView mPlaySurface = null;
    private SurfaceHolder mPlaySurfaceHolder;
    public void initMediaCodec(){
//    public void initMediaCodec(SurfaceView s){

        VideoActivity3 v3 = (VideoActivity3)getActivity();
        mPlaySurface = (SurfaceView) v3.findViewById(R.id.videoPlay);
        mPlaySurfaceHolder = mPlaySurface.getHolder();
        //回调函数来啦
        mPlaySurfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                try {
                    //通过多媒体格式名创建一个可用的解码器
                    mPlayCodec = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //初始化编码器
                final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", Video_Width, Video_Height);

                //获取h264中的pps及sps数据
                if (isUsePpsAndSps) {
                    byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
                    byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
                    mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                    mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
                }
                mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, PlayFrameRate);
                //set output image format
                mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                mPlayCodec.configure(mediaformat, holder.getSurface(), null, 0);
//                mPlayCodec.configure(mediaformat, null, null, 0);
                mPlayCodec.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });
    }
    private boolean isPlay = false;
    public void startPlay(){
        if (!isPlay){
            new decodeH2Thread().start();
            isPlay = true;
        }
    }
    class decodeH2Thread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                if (isPlay){
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    long startMs = System.currentTimeMillis();
                    long timeoutUs = 10000;

                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                    if (inIndex >= 0) {
                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
                        byteBuffer.clear();
                        byte[] b = mainAC.getOneNalu();
                        if (b!=null){
                            byteBuffer.put(b);
                            mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 0, 0);
                        }else{
                            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
                            byteBuffer.put(dummyFrame);
                            mPlayCodec.queueInputBuffer(inIndex, 0, dummyFrame.length, 0, 0);
                        }
                    }

                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
                    Log.d(TAG,"get output");
//                    if (outIndex<0){
//                        Log.d(TAG,outIndex+"");//-1
//                        continue;
//                    }

                    if (outIndex >= 0) {
//                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        boolean doRende = (info.size != 0);
//                        if (doRende){
                            Image image = mPlayCodec.getOutputImage(outIndex);
                            Log.d(TAG,"  format"+image.getFormat());
//                        Bitmap bitmap = new Bitmap();
//                        SurfaceView s = (SurfaceView) getActivity().findViewById(R.id.videoSurfaceView);

//                        TextureView t = (TextureView) getActivity().findViewById(R.id.playTextureView);
//                        t.getSurfaceTexture().updateTexImage();

//                        s.getHolder().getSurface()
//s
//                            Image im = mPlayCodec.getOutputImage(outIndex);
//                            Log.d(TAG,"  format"+im.getWidth());
//                            if (im == null){
//                                Log.d(TAG,"image null");
//                            }else {
//                                Log.d(TAG,"image not null");
//                            }
//                        }
//                        if (!doRende){
//                            Log.d(TAG,"nnnnn");
//                            break;
//                        }
//                        Log.d(TAG,"aa");

//                        Image im = null;

//                        Log.d(TAG,"  format"+im.getWidth());


//                        im.close();


//                        Image image = mPlayCodec.getOutputImage(outIndex);
//                        Image.Plane[] planes = image.getPlanes();

//                        Rect crop = im.getCropRect();
//                        im.close();
//                        Image image = decoder.getOutputImage(outputBufferId);

//                        Log.d(TAG,"  format"+im.getWidth());
//                        im.getFormat();
                        ByteBuffer outputBuffer = mPlayCodec.getOutputBuffer(outIndex);
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                        Log.d(TAG,"length"+info.offset + info.size);


//                        outputBuffer.get(outData);


                        boolean doRender = (info.size != 0);
                        mPlayCodec.releaseOutputBuffer(outIndex, doRender);
                        Log.d(TAG, "no output");
                        try {
                            Log.d(TAG, "sleep");
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {

                    }
                }
            }
        }
    }
}
