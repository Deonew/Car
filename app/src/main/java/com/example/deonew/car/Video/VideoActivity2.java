package com.example.deonew.car.Video;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deonew.car.R;
import com.example.deonew.car.Video.camera.Camera2BasicFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 17-3-10.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoActivity2 extends Activity implements View.OnClickListener {
    private static final String TAG = "Video2";

    public int isFirst = 0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // camera preview
    private SurfaceView mSurfaceView;
    //new preview
    private TextureView mTextureView;
    //
//    private Size mPreviewSize = new Size(800,600);
    private ImageReader mNewImageReader;

    //h264 preview
    private SurfaceView mH264SurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView iv_show;
    private CameraManager mCameraManager;
    private Handler childHandler, mainHandler;
    private String mCameraID;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;

    //video encode
    private MediaCodec h264Encodec;

    String H264Path = Environment.getExternalStorageDirectory() + "/carTemp.h264";
    // write into file every frame
    private FileOutputStream H264fos;

    private int surfaceHeight = 960;
    private int surfaceWidth = 540;
//    private int surfaceHeight = 1920;
//    private int surfaceWidth = 1080;


    //audio
    private FileOutputStream audioFos;
    private String audioPath = Environment.getExternalStorageDirectory() + "/carTemp.aac";
    private AudioRecord mAudioRec;
    //for startSendH264
    private int sampleRate = 44100;
    private int channelCount = 2;
    //stereo
    private int channelConf = AudioFormat.CHANNEL_IN_STEREO;
    //data format
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //generate by config
    //used when read data
    private int miniBuffSize;
    //thread to read data from audio buffer in mAudioRec
//    private AudioRecManager.dataCatch mDataCatch;
    private Thread mAudioDataCatchThread;

    //control record
    private boolean isAudioRecording = false;

    //audio encode
    private MediaCodec mAudioCodec;

    //read [video] data from codec
    // thread

    private boolean isH264Record = false;
    private Thread mVideoDataReadTH;



    //
    private SendH264 sendH264;
    private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264SendQueue(){
        return H264SendQueue;
    }

    //
    private RecvH264 recvH264 = null;


    private DecodeH264 decodeH264;



    //-------------------------------aac receive
    private BlockingQueue<byte[]> AACRecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getAACRecvQueue(){
        return AACRecvQueue;
    }


    //--------------------------------h264 receive
    private BlockingQueue<byte[]> H264RecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264RecvQueue(){
        return H264RecvQueue;
    }


    //debug
    private TextView videoStatus;
    private TextView audioStatus;
    private TextView sendH264Status;
    private TextView ipinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);

        videoStatus = (TextView)findViewById(R.id.videoRecStatus);
        audioStatus = (TextView)findViewById(R.id.audioRecStatus);
        sendH264Status = (TextView)findViewById(R.id.sendH264Status);
        ipinfo = (TextView)findViewById(R.id.ipinfo);


        Log.d(TAG,"11111111");
        Log.d("sssssssssssssssssss","11111111");

        sendH264Status.setText(H264Path);

//----------------------------------------------------------video

        try{
            H264fos = new FileOutputStream(H264Path,true);
        }catch (FileNotFoundException e){
        }



        sendH264 = new SendH264(this);
//        sendH264.connectToBox();
//        sendH264.connectToPhone();


        recvH264 = new RecvH264(this);


        Log.d(TAG,"11111111");


//        getIP();
        initView();

        initVideoCodec();
    }



    public void initVideoCodec(){
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", surfaceHeight, surfaceWidth);

        //synchronous

        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, surfaceHeight * surfaceWidth *3);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try{
            h264Encodec = MediaCodec.createEncoderByType("video/avc");
        }catch (IOException e){}
        h264Encodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        h264Encodec.start();

    }


    /**
     * startSendH264 preview
     */
    private void initView() {
        iv_show = (ImageView) findViewById(R.id.iv_show_camera2_activity);
        //mSurfaceView

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view_camera2_activity);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView创建
                // 初始化Camera
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView销毁
                // 释放Camera资源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    VideoActivity2.this.mCameraDevice = null;
                }
            }
        });
    }

    private byte[] HeadInfo = null;
    //startSendH264 with given size
    private void initCamera2() {
        //set up camera size
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;
//        mCameraID = "" + CameraCharacteristics.LENS_FACING_BACK;
        initImageReader();
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        //test set surfaceview size
        mSurfaceView.getLayoutParams().width = surfaceWidth;
        mSurfaceView.getLayoutParams().height = surfaceHeight;
//        mSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(surfaceWidth,surfaceHeight));

        try{
            for (String cameraID: mCameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraID);
                // if get the front camera
                //continue
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                //set
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                mCameraID = cameraID;
            }
        }catch (CameraAccessException e){}
        //open camera
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void initImageReader() {
        //startSendH264 mH264Handler

//        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.YV12,1);//ok
//        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.JPEG,1);//test jpeg//ok
        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.YUV_420_888,1);//
//        mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.YUV_420_888,1);//
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                Log.d("sssssssssssssss","fffffffffffffffff");
                //get data
                Image image = reader.acquireNextImage();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();//get byte buffer

                //original image data byte[]
                byte[] rawbytes = new byte[buffer.remaining()];
                buffer.get(rawbytes);

                Rect crop = image.getCropRect();
                int format = image.getFormat();//format: 35 = 0x23
                if(isH264Record){

                    //yuv_420_888 to I420

//                    int wi = crop.surfaceWidth();
//                    int he = crop.surfaceHeight();
//                    Image.Plane[] planes = image.getPlanes();
//
////                    new a byte[]
//                    byte[] data = new byte[wi * he * ImageFormat.getBitsPerPixel(format) / 8];
////                    data[0]=0x9;
//                    //index when write into data
//                    int offsetIndex = 0;
//                    int outputStride = 1;
//                    for (int i = 0; i< 3 ;i++){
//                        if (i == 0){
//                            offsetIndex= 0;
//                            outputStride = 1;
//                        }else if(i == 1){
//                            offsetIndex = wi * he;
//                            outputStride = 1;
//                        }else if(i == 2){
//                            offsetIndex = (int) (wi * he * 1.25);
//                            outputStride = 1;
//                        }
//                        ByteBuffer bf = planes[i].getBuffer();
//                        int rowStride = planes[i].getRowStride();
//                        int pixelStride = planes[i].getPixelStride();
//                        int shift = (i == 0) ? 0 : 1;
//                        int w = wi >> shift;
//                        int h = he >> shift;
//                        bf.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
//                        for (int row = 0; row < h; row++) {
//                            int length;
//                            if (pixelStride == 1 && outputStride == 1) {
//                                length = w;
//                                bf.get(data, offsetIndex, length);
//                                offsetIndex += length;
//                            } else {
//                                length = (w - 1) * pixelStride + 1;
//                                bf.get(rawbytes, 0, length);
//                                for (int col = 0; col < w; col++) {
//                                    data[offsetIndex] = rawbytes[col * pixelStride];
//                                    offsetIndex += outputStride;
//                                }
//                            }
//                            if (row < h - 1) {
//                                bf.position(bf.position() + rowStride - length);
//                            }
//                        }
//                    }

                    //yuv420888 to I420
//                    Rect crop = image.getCropRect();
//                    int format = image.getFormat();

                    //test
                    int width = crop.width();
                    int height = crop.height();
                    Image.Plane[] planes = image.getPlanes();
                    byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
                    byte[] rowData = new byte[planes[0].getRowStride()];
                    int channelOffset = 0;
                    int outputStride = 1;
                    for (int i = 0; i < planes.length; i++) {
                        switch (i) {
                            case 0:
                                channelOffset = 0;
                                outputStride = 1;
                                break;
                            case 1:
                                channelOffset = width * height;
                                outputStride = 1;
                                break;
                            case 2:
                                channelOffset = (int) (width * height * 1.25);
                                outputStride = 1;
                                break;
                        }
                        ByteBuffer planebuffer = planes[i].getBuffer();
                        int rowStride = planes[i].getRowStride();
                        int pixelStride = planes[i].getPixelStride();
                        int shift = (i == 0) ? 0 : 1;
                        int w = width >> shift;
                        int h = height >> shift;
                        planebuffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
                        for (int row = 0; row < h; row++) {
                            int length;
                            if (pixelStride == 1 && outputStride == 1) {
                                length = w;
                                planebuffer.get(data, channelOffset, length);
                                channelOffset += length;
                            } else {
                                length = (w - 1) * pixelStride + 1;
                                planebuffer.get(rowData, 0, length);
                                for (int col = 0; col < w; col++) {
                                    data[channelOffset] = rowData[col * pixelStride];
                                    channelOffset += outputStride;
                                }
                            }
                            if (row < h - 1) {
                                planebuffer.position(planebuffer.position() + rowStride - length);
                            }
                        }
                    }

                    Log.d(TAG,crop.width()+"   "+crop.height()+"");
                    Log.d(TAG,data.length+"");

                    //add codec input buffer
                    //wait for 10 msec
                    int iid = h264Encodec.dequeueInputBuffer(100);
                    if (iid>=0){
                        //get the input buffer
                        ByteBuffer iBuffer = h264Encodec.getInputBuffer(iid);
                        iBuffer.clear();

                        Log.d(TAG,iBuffer.capacity()+" "+data.length);
                        //our image data put into buffer
                        iBuffer.put(data);

                        //put buffer back to the original place
                        h264Encodec.queueInputBuffer(iid,0,data.length,100,1);
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferId = h264Encodec.dequeueOutputBuffer(bufferInfo,0);
                    //
                    if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    }else if(outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER){
                    }else{
                    while (outputBufferId>=0){
                        //output buffer
                        ByteBuffer outputBuffer = h264Encodec.getOutputBuffer(outputBufferId);
                        outputBuffer.slice();

                        byte[] outData = new byte[bufferInfo.size];
                        outputBuffer.get(outData);


                        //deal key frame v3.0
                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
                            //startSendH264 frame
                            HeadInfo = new byte[outData.length];
                            HeadInfo = outData;
                        }else if (bufferInfo.flags%8 == MediaCodec.BUFFER_FLAG_KEY_FRAME){
                            //key frame
                            byte[] key = new byte[outData.length+HeadInfo.length];
                            //param: src srcpos dec decpos length
                            System.arraycopy(HeadInfo,0,key,0,HeadInfo.length);
                            System.arraycopy(outData,0,key,HeadInfo.length,outData.length);
                            //version 1.0
                            //write key frame to h264

                            try{
                                H264fos.write(key, 0, key.length);
                            }catch (IOException e){}

                            //put key frame to queue
                            offerVideoQueue(key);

                        }else if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                            //end frame
                        }else{

                            try{
                                H264fos.write(outData, 0, outData.length);
                            }catch (IOException e){}

                            //
                            offerVideoQueue(outData);

                        }
                        //release output buffer
                        h264Encodec.releaseOutputBuffer(outputBufferId,false);
                        //change outputBufferId
                        //to continue this while circle
                        outputBufferId = h264Encodec.dequeueOutputBuffer(bufferInfo,0);
                    }
                    }
                }

                image.close();
            }
        }, null);
    }

    public void offerVideoQueue(byte[] b){
        int n = b.length/1000;
        for(int i = 0;i< n+1;i++){
            int len = 1000;
            if (i == n){
                len = b.length - i*1000;
            }

            //b.length = 1000,2000
            if (len == 0)
                break;

            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            getH264SendQueue().offer(tmp);
            i++;

        }
    }

    /**
     * camera listener
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;

            //list preview size
            startCamearPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                VideoActivity2.this.mCameraDevice = null;
            }
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(VideoActivity2.this, "open camera failed", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * startSendH264 preview
     */
    private void startCamearPreview() {
        try {
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);


            //send data to imagerreader and surfaceview
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            //enable onimageavailable
            previewRequestBuilder.addTarget(mImageReader.getSurface());


            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    mCameraCaptureSession = cameraCaptureSession;
                    try {

                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        CaptureRequest previewRequest = previewRequestBuilder.build();
//                        mCameraCaptureSession.
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);//repeat request
//                        mCameraCaptureSession.setRepeatingBurst()
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(VideoActivity2.this, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
//        takePicture();
    }


    public void toggleAudioRec(View v){

        isAudioRecording = true;
        mAudioRec.startRecording();
    }

    public void RecordH264(View v){
        isH264Record = true;
    }

    public void SendH264(View v){
        sendH264.startSendH264();
    }



    public void recvH264(View v){
        recvH264.startRecvH264();
        Log.d("ssssssssssssss","recv h264");
    }

    //play recv h264 file
    //
    public void playRecvH264(View v){
        mPlayCodec.start();
        decodeH264.startDecodeH264();
    }




    //play file

    private SurfaceView mPlaySurface = null;
    private SurfaceHolder mPlaySurfaceHolder;
    private Thread mDecodeThread;
    private MediaCodec mPlayCodec;
    private boolean mStopFlag = false;
    private DataInputStream mPlayInputStream;




    //queue use
    private BlockingQueue<byte[]> h264DataQueue = new ArrayBlockingQueue<byte[]>(10000);


    //-----------------------------------------------------------decode h264 thread
    private class decodeH264Thread1 implements Runnable{
        @Override
        public void run() {

            try {
                decodeLoop();
//                        isPlay = false;
                //                decodeLoop1();
            } catch (Exception e) {}

        }
        //标记 camera旧api 已改

        private byte[] streamBuffer = null;
        private void decodeLoop(){

//            ByteBuffer[] inputBuffers = mPlayCodec.getInputBuffers();
            //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小


            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            long timeoutUs = 10000;

//            int bytes_cnt = 0;
            while (mStopFlag == false){
                while (!mStopFlag){
                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                    if (inIndex >= 0) {
                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
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

                        //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
//                        mPlayCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                        mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 0, 0);

                    } else {
                        continue;
                    }
                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
                    if (outIndex >= 0) {
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        boolean doRender = (info.size != 0);
                        mPlayCodec.releaseOutputBuffer(outIndex, doRender);
                    } else {
                        Log.e(TAG, "no output");
                    }
                }
                mStopFlag = true;
//                mHandler.sendEmptyMessage(0);
            }

        }

    }

    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<byte[]>(10000);
    private byte[] currentBuff = new byte[102400];


    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;
    int cnt = 0;


    //----------------------play
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
            if (video_data_Queue.isEmpty()){break;}
//                break;
            byte[] tmp = video_data_Queue.poll();
//            if (tmp == null)
//                return nextNaluHead;

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
}