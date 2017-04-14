package com.example.deonew.car;

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
import android.media.MediaMuxer;
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

import com.example.deonew.car.Video.RecvH264Run;
import com.example.deonew.car.Video.SendH264;
import com.example.deonew.car.Video.sendH264Thread;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
    private MediaCodec mVideoCodec;
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
    private boolean isVideoRec = false;
    private Thread mVideoDataReadTH;

    //muxer
    private boolean isMuxering = false;//control
    private MediaMuxer mMuxer;//instance
    private String muxPath = Environment.getExternalStorageDirectory() + "/carTemp.mp4";



    //
    private SendH264 sendH264;
    private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
    public BlockingQueue getH264SendQueue(){
        return H264SendQueue;
    }








    //debug
    private TextView videoStatus;
    private TextView audioStatus;
    private TextView muxStatus;
    private TextView sendH264Status;
    private TextView ipinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);

        videoStatus = (TextView)findViewById(R.id.videoRecStatus);
        audioStatus = (TextView)findViewById(R.id.audioRecStatus);
        muxStatus = (TextView)findViewById(R.id.muxStatus);
        sendH264Status = (TextView)findViewById(R.id.sendH264Status);
        ipinfo = (TextView)findViewById(R.id.ipinfo);


        sendH264Status.setText(H264Path);

        //get format
//        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//        map.getOutputFormats();
//----------------------------------------------------------video
        //camera
        initView();
        //startSendH264 video codec
        initVideoCodec();


        //startSendH264 file manager
        File file=new File(H264Path);
        //every time, create a new file
        if (file.exists()){
            file.delete();
        }
        try{
            H264fos = new FileOutputStream(H264Path,true);
        }catch (FileNotFoundException e){
        }



        sendH264 = new SendH264(this);


        //not used
//        mVideoDataReadTH = new Thread(new videoCodecRun());
//        mVideoDataReadTH.startSendH264();

//        startSendH264 read data
//        isVideoRec = true;
        //startSendH264 audio
//        initAudioManager();
        //
//        mAudioRecManager.startAudioRec();

//        initAudioRec();



//        initMuxer();
        //startSendH264 muxer
//        mMuxer.startSendH264();
//
//        initNImageReader();

        //no tile
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//        mAudioRecManager.startAudioRec();
//        isVideoRec = true;
//        isMuxering = true;

//
//        initSendH264TH();
//
//        initRecvH264TH();

        //
//        startSendH264(Environment.getExternalStorageDirectory() + "/carTempRecv.264");

//        initPlaySurfaceView();

//        mSendH264Run.mRecUIHandler.sendEmptyMessage(0);

//        mSurfaceView.setVisibility(View.INVISIBLE);//debug

//        startSendH264(Environment.getExternalStorageDirectory() + "/carTempRecv.264");
//        mPlaySurface

//        getIP();

        //put data to queue
//        putData();
    }
    public void putData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f = new File(Environment.getExternalStorageDirectory() + "/carTempRecv.264");
                DataInputStream testInput = null;
                try {
                    testInput = new DataInputStream(new FileInputStream(f));
                    byte[] re = new byte[1024];
                    while (testInput.read(re)!=-1){
                        h264DataQueue.put(re);
                    }
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e){}
                catch (InterruptedException e){}



            }
        }).start();
    }

    public void getIP(){
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ipString = (ipAddress & 0xFF ) + "." +
                ((ipAddress >> 8 ) & 0xFF) + "." +
                ((ipAddress >> 16 ) & 0xFF) + "." +
                ( ipAddress >> 24 & 0xFF);
        ipinfo.setText(ipString);
    }

    private void initNImageReader() {
        //        mNewImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
//                ImageFormat.YV12, 2);
//        mNewImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener(){
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                Image image = reader.acquireLatestImage();
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                //raw data
//                byte[] data = new byte[buffer.remaining()];
//                buffer.get(data);
//                image.close();
////                Toast.makeText(getApplicationContext(),"avail",Toast.LENGTH_SHORT).show();
//            }
//        },null);
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




    private void initMuxer() {
        File muxFile=new File(muxPath);
        //every time, create a new mux file
        if (muxFile.exists()){
            muxFile.delete();
        }
        //
        try{
            mMuxer = new MediaMuxer(muxPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }catch (IOException e){}
//        mMuxer.startSendH264();
    }
    public void addAudioTrack(){
//        add audio track
//        mAudioFormat = mAudioRecManager.getAudioCodecOutputFormat();
//        mAudioTrackIndex = mMuxer.addTrack(mAudioFormat);
//        if (mAudioTrackIndex>=0 && mVideoTrackIndex>=0){
//            mMuxer.startSendH264();
//        }
    }

    public void initVideoCodec(){
        //asynchronous
//        try{
//            MediaCodec mVideoCodec = MediaCodec.createByCodecName("mycodec");
//
//        }catch(IOException e){}
//        MediaFormat mOutPutF;
//        mVideoCodec.setCallback(new MediaCodec.Callback(){
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
//        mVideoCodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        mOutPutF= mVideoCodec.getOutputFormat();
        //surface
//        mMediaCodecSurface = mVideoCodec.createInputSurface();
//        mMediaCodecSurface.toString();

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", surfaceHeight, surfaceWidth);
        //synchronous
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, surfaceHeight * surfaceWidth *3);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try{
            mVideoCodec = MediaCodec.createEncoderByType("video/avc");
        }catch (IOException e){}
        mVideoCodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mVideoCodec.start();

    }

    class MediaThread implements Runnable{

        private byte[] mFrameData = new byte[1280 * 720 * 3 / 2];//after encode
//
//        abstract void prepare() throws IOException{
//            void
//        }

        @Override
        public void run() {
//            super.run();
            // startSendH264 mediacodec in thread
            //
            initMediaC();

        }
        private void initMediaC(){
            try{
                mVideoCodec = MediaCodec.createByCodecName("my");
            }catch (IOException e){}
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", surfaceHeight, surfaceWidth);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, surfaceHeight * surfaceWidth *5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mVideoCodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoCodec.start();
        }
        public void encode(byte[] input){

            System.arraycopy(input, 0, mFrameData, 0, 1280 * 720);
            for (int i = 1280*720; i < input.length; i += 2) {
                mFrameData[i] = input[i + 1];
                mFrameData[i + 1] = input[i];
            }
//            final ByteBuffer buf = ByteBuffer.wrap(mFrameData);
//            encode(buf, mFrameData.length, getPTSUs());

        }
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
//        HandlerThread handlerThread = new HandlerThread("Camera2");
//        handlerThread.startSendH264();
//        childHandler = new Handler(handlerThread.getLooper());

        //handle message
        mainHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                videoStatus.append(msg.obj.toString());
//                videoStatus.append("h,");

//                switch (msg.what){
//                    case 0x3://data from recv thread
////                        getTotalBuffer()
//                        break;
//
//                }
            }
        };

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


                //show all sizes
//                Size[] s = map.getOutputSizes(SurfaceTexture.class);
//                for (int i =0 ;i< s.length; i++){
//                    Message m = mainHandler.obtainMessage(1,1,1,s[i].getWidth());
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1," ");
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1,s[i].getHeight());
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1," ");
//                    mainHandler.sendMessage(m);
//                }

//                Size mVideoSize =
                //choose optimal size
                //from all size option
                //1 get all size [at least] as big as the preview surface
//                List<Size> bigEnough = new ArrayList<Size>();
//                int w = aspectRatio.getWidth();
//                int h = aspectRatio.getHeight();
//                for (Size option : choices) {
//                    if (option.getHeight() == option.getWidth() * h / w &&
//                            option.getWidth() >= surfaceWidth && option.getHeight() >= surfaceHeight) {
//                        bigEnough.add(option);
//                    }
//                }

//                if (bigEnough.size() > 0) {
//                    return Collections.min(bigEnough, new CompareSizesByArea());
//                }
//
//                int orientation = getResources().getConfiguration().orientation;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
//                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }
//                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceWidth, surfaceHeight);
//                mTextureView.set

                //set
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
                //get all camera output size
//                Size[] s1 = map.getOutputSizes(ImageFormat.YUV_420_888);
//                for (int i =0 ;i< s1.length; i++){
//                    Message m = mainHandler.obtainMessage(1,1,1,s1[i].getWidth());
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1," ");
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1,s1[i].getHeight());
//                    mainHandler.sendMessage(m);
//                    m = mainHandler.obtainMessage(1,1,1," ");
//                    mainHandler.sendMessage(m);
//                }


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


    private int s = 1;
    private void initImageReader() {
        //startSendH264 mH264Handler

//        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.YV12,1);//ok
//        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.JPEG,1);//test jpeg//ok
        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.YUV_420_888,1);//
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                //get data
                Image image = reader.acquireNextImage();

//                Toast.makeText(getApplicationContext(),format,Toast.LENGTH_SHORT).show();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();//get byte buffer
                //original image data byte[]
                byte[] rawbytes = new byte[buffer.remaining()];
                buffer.get(rawbytes);

                //it worked
//                if (s == 1){
//                //write a frame picture
//                FileOutputStream output = null;
//                String mFile = Environment.getExternalStorageDirectory() + "/carTemp.jpeg";
//                try {
//                    output = new FileOutputStream(mFile);
//                    output.write(rawbytes);
//                }catch (IOException e){}
//                    s =2;
//                }


                //get image info before image close
                Rect crop = image.getCropRect();
                int format = image.getFormat();//format: 35 = 0x23


//                rawbytes = getDataFromImage(image,1);
                //camera get yv12
                //mediacodec need I420P
                //yv12 to I420P
                //
//                byte[] I420Pbytes = new byte[rawbytes.length];
                //swap 1.0
//                byte[] tempU = new byte[surfaceHeight*surfaceWidth/4];
//                byte[] tempV = new byte[surfaceHeight*surfaceWidth/4];
//                //copy V
//                System.arraycopy(rawbytes,surfaceWidth*surfaceHeight,tempV,0,tempV.length);
//                //copy U
//                System.arraycopy(rawbytes,surfaceWidth*surfaceHeight+tempV.length,tempU,0,tempU.length);
//
//                //swap u and v
//                System.arraycopy(tempV,0,rawbytes,surfaceWidth*surfaceHeight+tempV.length,tempV.length);
//                System.arraycopy(tempU,0,rawbytes,surfaceWidth*surfaceHeight,tempU.length);
//                //end swap1.0

                //swap 2.0
//                int i = surfaceWidth*surfaceHeight;
//                for (int j = 0 ;j<surfaceWidth*surfaceHeight/4;j++){
//                    byte t;
//                    t = rawbytes[i+j];
//                    rawbytes[i+j]= rawbytes[i+j+surfaceWidth*surfaceHeight/4];
//                    rawbytes[i+j+surfaceWidth*surfaceHeight/4] = t;
//                }
                //end swap 2.0

                //swap 3.0
                //use new byte[]
//                byte[] I420Pbytes = new byte[rawbytes.length];
//                System.arraycopy(rawbytes,0,I420Pbytes,0,surfaceWidth*surfaceHeight);
//                System.arraycopy(rawbytes,surfaceWidth*surfaceHeight+surfaceWidth*surfaceHeight/4,I420Pbytes,surfaceWidth*surfaceHeight,surfaceWidth*surfaceHeight/4);
//                System.arraycopy(rawbytes,surfaceWidth*surfaceHeight,I420Pbytes,surfaceWidth*surfaceHeight+surfaceWidth*surfaceHeight/4,surfaceWidth*surfaceHeight/4);
                //end swap 3.0


                //format I420P

//                buffer.get(rawbytes);



                //encode every frame


                if(isVideoRec){

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


                    //add codec input buffer
                    //wait for 10 msec
                    int iid = mVideoCodec.dequeueInputBuffer(100);
                    if (iid>=0){
                        //get the input buffer
                        ByteBuffer iBuffer = mVideoCodec.getInputBuffer(iid);
                        iBuffer.clear();
                        //our image data put into buffer
                        iBuffer.put(data);

                        //put buffer back to the original place
                        mVideoCodec.queueInputBuffer(iid,0,data.length,100,1);
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo,0);
                    //
                    if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                        //add video track
//                        mVideoFormat = mVideoCodec.getOutputFormat();
//                        mVideoTrackIndex = mMuxer.addTrack(mVideoFormat);
//                        if (mVideoTrackIndex>= 0 && mAudioTrackIndex>=0){
//                            mMuxer.startSendH264();
//                        }
                    }else if(outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER){
                    }else{
                    while (outputBufferId>=0){
                        //output buffer
                        ByteBuffer outputBuffer = mVideoCodec.getOutputBuffer(outputBufferId);
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
                        mVideoCodec.releaseOutputBuffer(outputBufferId,false);
                        //change outputBufferId
                        //to continue this while circle
                        outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo,0);
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
            //new image reader
//            previewRequestBuilder.addTarget(mNewImageReader.getSurface());
//            previewRequestBuilder.addTarget(mMediaCodecSurface);
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
    //new preview
    private void newStartCamearPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
//        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface mSurface = new Surface(mSurfaceTexture);

    }

    @Override
    public void onClick(View v) {
//        takePicture();
    }

//    /**
//     * 拍照
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void takePicture() {
//        if (mCameraDevice == null) return;
//        // 创建拍照需要的CaptureRequest.Builder
//        final CaptureRequest.Builder captureRequestBuilder;
//        try {
//            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            // 将imageReader的surface作为CaptureRequest.Builder的目标
//            captureRequestBuilder.addTarget(mImageReader.getSurface());
//            // 自动对焦
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            // 自动曝光
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            // 获取手机方向
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            // 根据设备方向计算设置照片的方向
//            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//            //拍照
//            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
//            mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }




    //encode

    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
//        if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
//            if (VERBOSE) {
//                Log.v(TAG, "pixelStride " + pixelStride);
//                Log.v(TAG, "rowStride " + rowStride);
//                Log.v(TAG, "surfaceWidth " + surfaceWidth);
//                Log.v(TAG, "surfaceHeight " + surfaceHeight);
//                Log.v(TAG, "buffer size " + buffer.remaining());
//            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
//            if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

    //mediacodec
//    private String path = Environment.getExternalStorageDirectory() + "/easy.h264";
    private int bitrate, framerate = 30;
//    framerate = 15;
//    bitrate = 2 * surfaceWidth * surfaceHeight * framerate / 20;
    public void initMediaCodec(){
//        try{
            //create Media format
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,1080,1920);

            //config mediaformat
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080*1920*5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            //create mediacodec
            try {
                mVideoCodec = MediaCodec.createEncoderByType("video/avc");//H264
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //config mVideoCodec using mediaformat
            mVideoCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoCodec.start();

//        }catch(IOException e){}

            //test
    }

    public void toggleAudioRec(View v){
//        isAudioRecording = !isAudioRecording;

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
    public void toggleVideoRec(View v){
        isVideoRec = !isVideoRec;
        if (isVideoRec){
            videoStatus.setText("video is rec...");
        }else{
            videoStatus.setText("video not rec...");
        }
        sendH264.startSendH264();
    }
    public void toggleMux(View v){
        isMuxering = !isMuxering;
        if (isMuxering)
            muxStatus.setText("muxing...");
        else
            muxStatus.setText("not muxing...");
    }


    //test h264 send
    private boolean isSendingH264 = false;
    private sendH264Thread mSendH264Run = null;// runnable
    private Thread mSendH264TH = null;
    private FileInputStream mInStrH264 = null;
    private Handler mUIHandler = null;
    private Handler mH264Handler = null;//send message to

//    private Thread mH264SendTH=null;
    public void initSendH264TH(){
        //startSendH264 ui handler
        if (mUIHandler == null){
            mUIHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //handle message from sub-thread
//                    sendH264Status.setText(msg.obj.toString());
//                    sendH264Status.append("o");


                    switch (msg.what){
                        case 0x3:
                            //write into total buffer
                            byte[] tempB = (byte[])msg.obj;
//                            getTotalBuffer();
//                            System.arraycopy(tempB,0,getTotalBuffer(),totalBufferIndex,tempB.length);
                            writeIntoTotalBuffer(tempB);

                            //tempB.length should be changed
                            totalBufferIndex = totalBufferIndex + tempB.length;

                            break;
                        case 0x105:
                            sendH264Status.append("o");
                        case 0x106:
//                            sendH264Status.setText("");
                            sendH264Status.append("i");
                            break;
                        case 0x201:
                        case 0x202:
                        case 0x203:
                            sendH264Status.append(msg.obj.toString());
                            break;
                        case 0x301:
                            sendH264Status.append(msg.obj.toString()+" ");
                            break;
                    }

                }
            };
        }

        //startSendH264 thread
        if (mSendH264Run == null){
            mSendH264Run =new sendH264Thread(mUIHandler, this);
        }
        mSendH264TH = new Thread(mSendH264Run);
        mSendH264TH.start();


//        mSendH264Run.mRecUIHandler.sendMessage(m);
//        mH264Handler = new Handler(){}

    }

    private int lastend = 0;//offset: last byte
    /*
    *
    *toWriteBuffer:
    */
    public void writeIntoTotalBuffer(byte[] toWriteBuffer){
        if (lastend + toWriteBuffer.length < totalBuffer.length){
            //directly copy
            System.arraycopy(toWriteBuffer,0,getTotalBuffer(),lastend,toWriteBuffer.length);
            lastend = lastend + toWriteBuffer.length;
        }else{
            //previous part
            int preLen = totalBuffer.length - lastend;
            System.arraycopy(toWriteBuffer,0,getTotalBuffer(),lastend,preLen);

            //later part
            //startSendH264 at the beginning
            int laterLen = toWriteBuffer.length - preLen;
            System.arraycopy(toWriteBuffer,preLen,getTotalBuffer(),0,laterLen);

            //handle lastend
            lastend = laterLen;
        }
    }

    public void toggleH264(View v){
        //send
        Message m = new Message();
        m.what = 0x1;
        mSendH264Run.mRecUIHandler.sendMessage(m);

//        Sleep(10);
//        startSendH264(Environment.getExternalStorageDirectory() + "/carTempRecv.264");


    }

    public void recvH264(View v){
        //send recv message
        Message m = new Message();
        m.what = 0x2;
        mSendH264Run.mRecUIHandler.sendMessage(m);
    }
    //play recv h264 file
    //
    public void playRecvH264(View v){
        startDecodingThread();
    }


    class videoCodecRun implements Runnable{
        @Override
        public void run() {
            while (isVideoRec){
                readVideoData();
                //test audio data
//                readAudioData();
            }
        }
        public void readVideoData(){

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo,0);

            while (outputBufferId>=0){
                //output buffer
                ByteBuffer outputBuffer = mVideoCodec.getOutputBuffer(outputBufferId);
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
                    //write key frame
                    try{
                        H264fos.write(key, 0, key.length);
                    }catch (IOException e){}
//                        Toast.makeText(getApplicationContext(),"key",Toast.LENGTH_SHORT).show();
                }else if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    //end frame
                }else{
                    //write data
                    try{
                        H264fos.write(outData, 0, outData.length);
                        //send message


                    }catch (IOException e){}
//                        Toast.makeText(getApplicationContext(),"normal",Toast.LENGTH_SHORT).show();
                }
                //version 2.0
                //write data into muxer
//                if (isVideoRec){
//                    mMuxer.writeSampleData(mVideoTrackIndex,outputBuffer,bufferInfo);
//                }
                //release output buffer
                mVideoCodec.releaseOutputBuffer(outputBufferId,false);
                //change outputBufferId
                //to continue this while circle
                outputBufferId = mVideoCodec.dequeueOutputBuffer(bufferInfo,0);
            }
        }
        public void readAudioData(){
//            mAudioRecManager.
        }
    }


//---------------------------------------------------------------recv h264
    private RecvH264Run mRecvH264Run = null;
    private Thread mRecvH264Thread = null;
    public void initRecvH264TH(){
        mRecvH264Run = new RecvH264Run(mUIHandler);
        mRecvH264Thread = new Thread(mRecvH264Run);
        mRecvH264Thread.start();
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
    private void init(String filePath){
        File f = new File(filePath);
        if (null == f || !f.exists() || f.length() == 0) {
            Toast.makeText(this, "指定文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            //获取文件输入流
            mPlayInputStream = new DataInputStream(new FileInputStream(new File(filePath)));
//            mPlayInputStream = new DataInputStream(mSendH264Run.getReceiveStream());
        }
//        catch (Exception e){}
        catch (FileNotFoundException e) {e.printStackTrace();}

        initPlaySurfaceView();
    }

    public void initPlaySurfaceView() {
        mPlaySurface = (SurfaceView) findViewById(R.id.preview);
        mPlaySurfaceHolder = mPlaySurface.getHolder();
        mPlaySurfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                try {
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
                //设置帧率
                mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, PlayFrameRate);
                //https://developer.android.com/reference/android/media/MediaFormat.html#KEY_MAX_INPUT_SIZE
                //设置配置参数，参数介绍 ：
                // format   如果为解码器，此处表示输入数据的格式；如果为编码器，此处表示输出数据的格式。
                //surface   指定一个surface，可用作decode的输出渲染。
                //crypto    如果需要给媒体数据加密，此处指定一个crypto类.
                //   flags  如果正在配置的对象是用作编码器，此处加上CONFIGURE_FLAG_ENCODE 标签。
                mPlayCodec.configure(mediaformat, holder.getSurface(), null, 0);


                //解码器设置
                MediaFormat mediaFormat;
                mPlayCodec.setCallback(new MediaCodec.Callback(){
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int index) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(index);
                        int len = 0;
                        if (!h264DataQueue.isEmpty()){
                            try{
                                byte[] b = h264DataQueue.take();
                                len = b.length;
                                inputBuffer.put(b);
                            }catch (InterruptedException e){}

                        }
                        codec.queueInputBuffer(index,0,len,1000,0);
                    }

                    @Override
                    public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                        ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                        MediaFormat bufferFormat = codec.getOutputFormat(index); // option A
                        // bufferFormat is equivalent to mOutputFormat
                        // outputBuffer is ready to be processed or rendered.

                        if (index >= 0) {
                            while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - 1000) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            boolean doRender = (info.size != 0);
                            codec.releaseOutputBuffer(index, doRender);
                        } else {
                        }
                    }

                    @Override
                    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

                    }

                    @Override
                    public void onError(MediaCodec codec, MediaCodec.CodecException e) {

                    }
                });

                mPlayCodec.start();

            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


        //test
        File f = new File(Environment.getExternalStorageDirectory() + "/carTempRecv.264");
        try {
            mPlayInputStream = new DataInputStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {e.printStackTrace();}

        //test function getCodecData
//        try{
//            testBytes = getBytes(mPlayInputStream);
//        }catch (IOException e){}
    }

    private void startDecodingThread() {
        mPlayCodec.start();
        mDecodeThread = new Thread(new decodeH264Thread());
        mDecodeThread.start();
    }
    private boolean isPlayH264 = false;

    //added 4.3
    private int h264RecvReadIndex = 0;//read count
    private int readLength = 1024;//read length every time
    private String recvCarTempH264Path = Environment.getExternalStorageDirectory() + "/carTempRecv.264";
    private RandomAccessFile raf = null;
    private byte[] h264Bytes;

    public byte[] getTotalBuffer() {
        return totalBuffer;
    }
    //test
    public byte[] testBytes = new byte[10000000];
    public byte[] getTestBytes(){
        return testBytes;
    }
    public int totalBufferIndex =0;
    private byte[] totalBuffer = new byte[10000000];

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

                        //get data from totalbuffer
//                        byteBuffer.put(b);
//                        mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 100, 0);

                        //get data from public buffer
//                        byte[] b = getCodecPutData();
//                        byteBuffer.put(b);
//                        mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 100, 0);
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
    private int nextFrameStart = 0;
    private void decodeLoop1(){
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        long startMs = System.currentTimeMillis();
        long timeoutUs = 10000;
        byte[] marker0 = new byte[]{0, 0, 0, 1};
        byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
        byte[] streamBuffer = null;

        int bytes_cnt = 0;
        while (mStopFlag == false){
            streamBuffer = getTotalBuffer();
            bytes_cnt = streamBuffer.length;
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame;
            }
            int startIndex = 0;
            int remaining = bytes_cnt;
            while (true){
//                if (remaining == 0 || startIndex >= remaining) {
//                    break;
//                }
//                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
//                if (nextFrameStart == -1) {
//                    nextFrameStart = remaining;
//                } else {
//                }

//                if (lastFrameEnd >= lastend){
//                    break;
//                }
                if (1 == 2){
                    break;
                }



                //get byte[] to put
                byte[] toPut = getCodecPutData();

                int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
                    byteBuffer.clear();
                    byteBuffer.put(toPut);
                    mPlayCodec.queueInputBuffer(inIndex, 0,toPut.length, 0, 0);
                    startIndex = nextFrameStart;
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
                }
            }
            mStopFlag = true;
        }
    }
    public void decodeLoop2(){
        long startMs = System.currentTimeMillis();
        long timeoutUs = 10000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
        if (inIndex >= 0) {
            ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
//                        ByteBuffer byteBuffer = inputBuffers[inIndex];
            byteBuffer.clear();

            //get data from queue
            byte[] b = null;
            if (!h264DataQueue.isEmpty()){
                try{
                    b = h264DataQueue.take();
                }catch (InterruptedException e){}
            }
            byteBuffer.put(b);
            mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 0, 0);
        }
        else {
//            continue;
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

    public byte[] getCodecPutData(){
//        byte[] tempFrame = null;

        //generate lsp
        byte[] pattern = new byte[]{0, 0, 0, 1};//nalu head
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

        //get next frame offset
//        nextFrameStart = lastFrameEnd;
        int tempOffset = lastFrameEnd;
        boolean isGetFrame = false;
        int patternIndex = 0;
        boolean isParted = false;//
        int preLength = 0;
        int laterLength = 0;
        while(!isGetFrame){
            //if read end
            if (tempOffset >= streamBuffer.length){
                //
                preLength = tempOffset - lastFrameEnd;
                tempOffset = 0;
            }

            while (patternIndex > 0 && streamBuffer[tempOffset] != pattern[patternIndex]) {
                patternIndex = lsp[patternIndex - 1];  // Strictly decreasing
            }
            if (streamBuffer[tempOffset] == pattern[patternIndex]) {
                patternIndex++;
                if (patternIndex == pattern.length)
                    break;
            }
            tempOffset++;
        }
        //get later length if parted
        if (isParted){
            laterLength = tempOffset;
        }else{
            laterLength = tempOffset - lastFrameEnd;
        }

        //get temp frame data
        byte[] tempFrame = new byte[preLength + laterLength];
        if (isParted){
            //get pre-part
            System.arraycopy(streamBuffer,lastFrameEnd,tempFrame,0,preLength);
            //get later part
            System.arraycopy(streamBuffer,0,tempFrame,preLength,laterLength);
        }else{
            //directly get later
            System.arraycopy(streamBuffer,lastFrameEnd,tempFrame,0,laterLength);
        }

        //change lastFrameEnd at last
        lastFrameEnd = tempOffset;

        //
        Message m = new Message();
        m.what = 0x106;
        m.obj = lsp;
        mUIHandler.sendMessage(m);

        return tempFrame;
//        int j = 0;  // Number of chars matched in pattern
//        for (int i = lastFrameEnd; i < remain; i++) {
//            while (j > 0 && bytes[i] != pattern[j]) {
//                // Fall back in the pattern
//                j = lsp[j - 1];  // Strictly decreasing
//            }
//            if (bytes[i] == pattern[j]) {
//                // Next char matched, increment position
//                j++;
//                if (j == pattern.length)
//                    return i - (j - 1);
//            }
//        }
//        return -1;


    }

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