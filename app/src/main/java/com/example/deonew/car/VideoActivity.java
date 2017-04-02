package com.example.deonew.car;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Created by deonew on 17-3-8.
 */

public class VideoActivity extends Activity implements View.OnClickListener{
    //Buttons
    private Button startLive,stopLive;

    //surfaceview
    private SurfaceView surfaceView;

    //camera: front and back
    private CameraDevice fCamera,bCamera;
    private TextureView textureView;//camera preview

    //test textview
    private TextView tip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //camera set before setContentView
        Window window = getWindow();
        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //camera set
        surfaceView = (SurfaceView)this.findViewById(R.id.surfaceView);
        surfaceView.getHolder().setFixedSize(800,400);
        //surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setOnClickListener(this);

        //camera
        //get manager
//        CameraManager cm = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//        cm.openCamera();

//        getFragmentManager();



        TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
        textureView = (TextureView)findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);

        openCamera();


        tip = (TextView)this.findViewById(R.id.textView4);
        initBtns();
    }

    private void initBtns(){
        startLive = (Button)this.findViewById(R.id.startLive);
        startLive.setOnClickListener(this);
        stopLive = (Button)this.findViewById(R.id.stopLive);
        stopLive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startLive:
                startClick();
                break;
            case R.id.stopLive:
                stopClick();
                break;
            case R.id.surfaceView:
                try {
//                    mMediaStream.getCamera().autoFocus(null);
                } catch (Exception e) {
                }
                break;
        }
    }
    private void startClick(){
        tip.append("start...");
    }
    private void stopClick(){
        tip.append("stop...");
    }

    @SuppressLint("NewApi")
    private final CameraDevice.StateCallback openCallBack = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(CameraDevice camera) {

        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    //set camera
    public void setCamera(int width, int height) {

    }

    @SuppressLint("NewApi")
    private void openCamera(){
        CameraManager cm = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            cm.openCamera(cm.getCameraIdList()[0],openCallBack,null);
        }catch(CameraAccessException e){}
    }
    public void startPreview(){

    }

}
