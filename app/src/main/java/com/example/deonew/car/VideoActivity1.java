package com.example.deonew.car;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;

/**
 * Created by deonew on 17-3-10.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoActivity1 extends Activity{
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;//
//    private Size previewSize;
    private SurfaceHolder surfaceViewHolder;
    private ImageReader imageReader;
    //private CameraCaptureSession cameraCaptureSession;
    private Handler childHandler,mainHandler;
//    private ImageReader imageReader;
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){
    @Override
    public void onDisconnected(CameraDevice camera) {

    }

    @Override
    public void onOpened(CameraDevice camera) {
        //
        cameraDevice = camera;
        startPreview();
    }

    @Override
    public void onClosed(CameraDevice camera) {
        super.onClosed(camera);
    }

    @Override
    public void onError(CameraDevice camera, int error) {

    }
};
    private SurfaceView surfaceView;
    private CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video1);
        initPreView();
    }
    //before open the camera, prepare the preview
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initPreView(){
        surfaceView = (SurfaceView)findViewById(R.id.cameraSurfaceView);
        surfaceViewHolder =surfaceView.getHolder();
        surfaceViewHolder.setKeepScreenOn(true);
        surfaceViewHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
    //
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initCamera(){
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());

        String cameraID = ""+CameraCharacteristics.LENS_FACING_BACK;
        cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//        try{}catch (){}
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraID,stateCallback,null);
        }catch (CameraAccessException e){}


    }
    public void startPreview(){
        try{
            final CaptureRequest.Builder previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surfaceViewHolder.getSurface());
//            captureRequestBuilder.addTarget(mImageReader.getSurface());
            cameraDevice.createCaptureSession(Arrays.asList(surfaceViewHolder.getSurface(), imageReader.getSurface()),new CameraCaptureSession.StateCallback()
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSes) {
                    // start preview
                    cameraCaptureSession = cameraCaptureSes;
                    try {
                        // 自动对焦
//                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
//                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        cameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
                    } catch (CameraAccessException e)
                    {

                    }
                    //mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            },childHandler);
        }catch (CameraAccessException e){}
    }
}
