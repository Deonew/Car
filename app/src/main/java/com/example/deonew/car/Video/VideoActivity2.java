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

//        getFragmentManager().beginTransaction()
//                .replace(R.id.container, Camera2BasicFragment.newInstance())
//                .commit();


        videoStatus = (TextView)findViewById(R.id.videoRecStatus);
        audioStatus = (TextView)findViewById(R.id.audioRecStatus);
        sendH264Status = (TextView)findViewById(R.id.sendH264Status);
        ipinfo = (TextView)findViewById(R.id.ipinfo);


        Log.d(TAG,"11111111");
        Log.d("sssssssssssssssssss","11111111");

        sendH264Status.setText(H264Path);

        //get format
//        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//        map.getOutputFormats();
//----------------------------------------------------------video
        //camera
//        initView();
        //startSendH264 video codec
//        initVideoCodec();

        //startSendH264 file manager
//        File file=new File(H264Path);
//        //every time, create a new file
//        if (file.exists()){
//            file.delete();
//        }
        try{
            H264fos = new FileOutputStream(H264Path,true);
        }catch (FileNotFoundException e){
        }



        sendH264 = new SendH264(this);
//        sendH264.connectToBox();
//        sendH264.connectToPhone();


        recvH264 = new RecvH264(this);


        Log.d(TAG,"11111111");



//        mDecodeThread = new Thread(new decodeH264Thread());

//        decodeH264 = new DecodeH264(this);

//        startSendH264 read data
//        isH264Record = true;
        //startSendH264 audio
//        initAudioManager();
        //
//        mAudioRecManager.startAudioRec();

//        initAudioRec();



//        initNImageReader();


//        mAudioRecManager.startAudioRec();
//        isH264Record = true;
//        isMuxering = true;

//
//        initSendH264TH();
//
//        initRecvH264TH();

        //
//        startSendH264(Environment.getExternalStorageDirectory() + "/carTempRecv.264");

//        initPlaySurfaceView();


//        mSurfaceView.setVisibility(View.INVISIBLE);//debug


//        getIP();
        initView();

        initVideoCodec();

        //
//        initMediaCodec();

        //


    }




    //--------------------------------audio
    public void initAudioRec(){
        miniBuffSize = AudioRecord.getMinBufferSize(sampleRate,channelConf,audioFormat)*2;
        //startSendH264 mAudioRec according to minibuff and given conf
        mAudioRec = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConf, audioFormat, miniBuffSize);

        //startSendH264 fos
        File file=new File(audioPath);
        if (file.exists()){
            file.delete();
        }

        try{
            audioFos = new FileOutputStream(audioPath,true);
        }catch(FileNotFoundException e){}

        //codec config
        MediaFormat f = MediaFormat.createAudioFormat("audio/mp4a-latm",sampleRate,channelCount);
        //"aac-profile"
        f.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //bit rate
        f.setInteger(MediaFormat.KEY_BIT_RATE, 25600);
        //create aac type codec
        try{
            mAudioCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        }catch (IOException e){}
        mAudioCodec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioCodec.start();

    }
    class audioDataCatch implements Runnable{
        @Override
        public void run() {
            while(isAudioRecording){
                readAudioData();
            }
        }


    //read raw data
    //encode the data
    //write it to a file according to the given string
    public void readAudioData() {
        //read raw data
        int inputBuffIndex = mAudioCodec.dequeueInputBuffer(-1);
        if (inputBuffIndex>=0){
            //available input buffer
            ByteBuffer bybu = mAudioCodec.getInputBuffer(inputBuffIndex);
            bybu.clear();
//            bybu
            int len = mAudioRec.read(bybu,miniBuffSize);
            //put it back
            mAudioCodec.queueInputBuffer(inputBuffIndex,0,len, System.nanoTime()/1000,0);
        }
        //get data out
        MediaCodec.BufferInfo buInfo = new MediaCodec.BufferInfo();
        int outputBuffIndex =mAudioCodec.dequeueOutputBuffer(buInfo,0);

        if (outputBuffIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            //add audio track
//            mVideoActivity2.addAudioTrack();
        }else if(outputBuffIndex == MediaCodec.INFO_TRY_AGAIN_LATER){
        }else{
            while(outputBuffIndex>=0){
                ByteBuffer bybuOut = mAudioCodec.getOutputBuffer(outputBuffIndex);

                //encodedData receive data from buffer
                byte[] encodedData = new byte[buInfo.size+7];
                //get raw data
                bybuOut.get(encodedData,7,buInfo.size);
                //add adts
                // to save as aac format
                int profile = 2;  //AAC LC
                int freqIdx = 4;  //44.1KHz
                int chanCfg = 2;  //CPE
                encodedData[0] = (byte)0xFF;
                encodedData[1] = (byte)0xF9;
                encodedData[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
                encodedData[3] = (byte)(((chanCfg&3)<<6) + (encodedData.length>>11));
                encodedData[4] = (byte)((encodedData.length&0x7FF) >> 3);
                encodedData[5] = (byte)(((encodedData.length&7)<<5) + 0x1F);
                encodedData[6] = (byte)0xFC;

                //write data
                try{
                    audioFos.write(encodedData,0,encodedData.length);
                }catch (IOException e){}

                //continue circle
                mAudioCodec.releaseOutputBuffer(outputBuffIndex,false);
                outputBuffIndex = mAudioCodec.dequeueOutputBuffer(buInfo,0);
            }
        }
    }
}





    public void initVideoCodec(){
        //asynchronous
//        try{
//            MediaCodec h264Encodec = MediaCodec.createByCodecName("mycodec");
//
//        }catch(IOException e){}
//        MediaFormat mOutPutF;
//        h264Encodec.setCallback(new MediaCodec.Callback(){
//            @Override
//            public void onInputBufferAvailable(MediaCodec codec, int index) {
//                //get the input buffer
//                ByteBuffer inputBuffer = codec.getInputBuffer(index);
////                codec.getInputBuffers();
//                //fill input buffer with data
//                //where is data from?
//                //mediacodec? or image reader
////                mMediaCodecSurface.
////                mImageReader.get
//
//            }
//
//            @Override
//            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
//
//            }
//
//            @Override
//            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
//
//            }
//
//            @Override
//            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
//
//            }
//        });
//        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 720, 1280);
//        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 720*1280*5);
//        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
//        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
//        h264Encodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        mOutPutF= h264Encodec.getOutputFormat();
        //surface
//        mMediaCodecSurface = h264Encodec.createInputSurface();
//        mMediaCodecSurface.toString();

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



//        mTextureView = (TextureView) findViewById(R.id.preViewTextureView);
//        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int surfaceWidth, int surfaceHeight) {
//                //when surface texture available
//                //startSendH264 camera according to given surfaceWidth and surfaceHeight
//                initCamera2(surfaceWidth,surfaceHeight);
//            }
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                if (null != mCameraDevice) {
//                    mCameraDevice.close();
//                    VideoActivity2.this.mCameraDevice = null;
//                }
//                return false;
//            }
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int surfaceWidth, int surfaceHeight) {
//
//            }
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//            }
//        });

        // mSurfaceView添加回调
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

                    Log.d(TAG,data.length+"");

                    //add codec input buffer
                    //wait for 10 msec
                    int iid = h264Encodec.dequeueInputBuffer(100);
                    if (iid>=0){
                        //get the input buffer
                        ByteBuffer iBuffer = h264Encodec.getInputBuffer(iid);
                        iBuffer.clear();
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
        if (isAudioRecording){
            mAudioDataCatchThread = new Thread(new audioDataCatch());
            mAudioDataCatchThread.start();
            audioStatus.setText("audio is rec...");
        }else{
            audioStatus.setText("audio not rec...");
        }

        mAudioRec.startRecording();

    }

    public void RecordH264(View v){
        isH264Record = true;
    }

    public void SendH264(View v){
        sendH264.startSendH264();
    }


    //test h264 send
    private boolean isSendingH264 = false;
    private sendH264Thread mSendH264Run = null;// runnable
    private Thread mSendH264TH = null;
    private FileInputStream mInStrH264 = null;
    private Handler mUIHandler = null;



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
//    private String FileName = "test.h264";
    private int Video_Width = 50;
    private int Video_Height = 30;
    private int PlayFrameRate = 15;
    private Boolean isUsePpsAndSps = false;


    private boolean isPlayH264 = false;

    //added 4.3
    public byte[] streamBuffer = null;

    //queue use
    private BlockingQueue<byte[]> h264DataQueue = new ArrayBlockingQueue<byte[]>(10000);

    private class decodeH264Thread implements Runnable{
        @Override
        public void run() {
//            while(isPlayH264){
            try {
//                decodeLoop();
//                decodeLoop1();
//                decodeLoop2();
            } catch (Exception e) {
            }
        }
        private void decodeLoop(){
            //存放目标文件的数据
//            ByteBuffer[] inputBuffers = mPlayCodec.getInputBuffers();
            //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            long timeoutUs = 10000;
            byte[] marker0 = new byte[]{0, 0, 0, 1};
            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
            try {
                streamBuffer = getBytes(mPlayInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int bytes_cnt = 0;
            while (mStopFlag == false){
                bytes_cnt = streamBuffer.length;
                if (bytes_cnt == 0) {
                    streamBuffer = dummyFrame;
                }
                int startIndex = 0;
                int remaining = bytes_cnt;
                int lastCopy = 0;
                while (true){

                    if (remaining == 0 || startIndex >= remaining && 1 ==2) {
                        break;
                    }
                    //origin
                    int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex+ 2, remaining);
                    if (nextFrameStart == -1) {
                        nextFrameStart = remaining;
                    } else {
                        //此处没写，标记一下
                    }

                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                    if (inIndex >= 0) {
                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
//                        ByteBuffer byteBuffer = inputBuffers[inIndex];
                        byteBuffer.clear();

                        //get data from file stream
                        //works
                        byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                        mPlayCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);

                        startIndex = nextFrameStart;
                    } else {
                        continue;
                    }
                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
                    if (outIndex >= 0) {
                        //帧控制是不在这种情况下工作，因为没有PTS H264是可用的
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
                    }
                }
                mStopFlag = true;
            }

        }

    }

    private int lastFrameEnd = 0;//last time read a frame




    //get byte array from a stream
    public static byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
//            BufferedOutputStream bos=new BufferedOutputStream(new ByteArrayOutputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    //march nalu head
    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(30);//works
//            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);
        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }
        return -1;  // Not found
    }

    //
    private int p = 0;
    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        //
        if (p == 0){
            Message m = new Message();
            m.obj = lsp;
            mUIHandler.sendMessage(m);
        }
        return lsp;
    }



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


            try {
                streamBuffer = getBytes(mPlayInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int bytes_cnt = 0;
            while (mStopFlag == false){
                end = streamBuffer.length;
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

        int startIndex = 0;
        private  int nextFrameStart = 0;
        private int end;
        private byte[] naluHeader = new byte[]{0, 0, 0, 1};
        public byte[] getAFrame(){
            nextFrameStart = KMPMatch(naluHeader, streamBuffer, startIndex + 2, end);
            byte[] result = new byte[nextFrameStart - startIndex];
            System.arraycopy(streamBuffer, startIndex, result,0, nextFrameStart - startIndex);
            startIndex = nextFrameStart;
            return result;
        }

    }

    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<byte[]>(10000);
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