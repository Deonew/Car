package com.example.deonew.car;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.deonew.car.Audio.AudioSendRun;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnUni;
    private Button btnMulti;
    private Button btnBroad;

    private Handler mOtherHandler;
    private Handler mUiHandler;
    private TextView testView ,fileListTV;

    //audio
    boolean isRecording;
    private Button btnArStart, btnArStop;

    CarThread carThread;
    private Thread mAudioSendTH = null;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"ok");

//        setTagsForButtons();


        //tablayout
        TabLayout mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        //set title
        List<String> titleList = new ArrayList<>();
        titleList.add("Text");
        titleList.add("Audio");
        titleList.add("Video");

        //add tab according to title
        mainTabLayout.addTab(mainTabLayout.newTab().setText(titleList.get(0)));
        mainTabLayout.addTab(mainTabLayout.newTab().setText(titleList.get(1)));
        mainTabLayout.addTab(mainTabLayout.newTab().setText(titleList.get(2)));

        //new adapter
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(),this);
        //view pager and adapter
        ViewPager viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        viewPager.setAdapter(mainViewPagerAdapter);

        //set default page
        viewPager.setCurrentItem(1);

        //tablayout and viewpaer
        mainTabLayout.setupWithViewPager(viewPager);


        //fill in width
        mainTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mainTabLayout.setTabMode(TabLayout.MODE_FIXED);




        //audio send thread
        mAudioSendTH = new Thread(new AudioSendRun());
        mAudioSendTH.start();



//        initTVs();



        //ui thread handle message
        mUiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                setTestView("nihao");
                addTestView((String)msg.obj);
            }
        };
    }

    public void toggleAudio(View v){
//        mainViewPagerAdapter.getItem(2).initAudioRec();
    }


    public void sendText(){

    }

    /*
    * init textviews
    */
//    public void initTVs(){
//        fileListTV = (TextView) this.findViewById(R.id.fileListTextView);
//        testView = (TextView) this.findViewById(R.id.showTest);
//        testView.append("\nnihaoa");
//    }
    /*
    *func: set tags for buttons
    *
    */
    private void setTagsForButtons(){
    }
    @Override
    public void onClick(View v){
        String id = (String) v.getTag();
        switch (id){
            case "uni":
                uniRequire();
                break;
            case "multi":
                multiRequire();
                break;
            case "broad":
                broadRequire();
                break;
            case "arStart":
                record();
                break;
            case "arStop":
                stopRecord();
                break;
            case "toAudioAC":
                jumpToAudioAC();
                break;
            case "toVideoAC":
                jumpToVideoAC();
                break;
            default:
                break;
        }
    }
    //--------------------ui 线程的操作
    public void addTestView(String s){
        testView.append("\n");
        testView.append(s);
    }
    //------
//    public void sendMsgToTipTV(String s){
//        Message msg = new Message();
////                msg.what =
//        msg.obj = s ;
//        mUiHandler.sendMessage(msg);
//    }

    /*
    * send unicast command to server(obu)
    */
    public void uniRequire(){
        new Thread()
        {
            public void run(){
                String ip = getWifiIP();
                Message msg = new Message();
//                msg.what =
//                msg.obj = "yep";
                msg.obj = ""+ ip ;
                mUiHandler.sendMessage(msg);
//                mUiHandler.sendEmptyMessage(0);


                try{
                    Socket s = new Socket("10.202.0.206",20000);
                    //send conmand
//                    OutputStream os = s.getOutputStream();
//                    PrintWriter pw = new PrintWriter(os);
//                    String uniCommand = "uni";
//                    pw.write(uniCommand);
//                    pw.flush();

                    //receive sentence
                    InputStream is = s.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    mUiHandler.sendEmptyMessage(0x123);


                    s.close();
                }catch(IOException e){}

                //
                jumpToVideoAC();
            }

        }.start();
    }

    public void multiRequire(){
        new Thread()
        {
            public void run(){
                try{
                    Socket s = new Socket("10.8.191.213",20000);
                    //send conmand
                    OutputStream os = s.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    String multiCommand = "multi";
                    pw.write(multiCommand);
                    pw.flush();
                    s.close();
                }catch(IOException e){}
            }

        }.start();
    }

    public void broadRequire(){
        new Thread()
        {
            public void run(){
                try{
                    Socket s = new Socket("10.8.191.213",20000);
                    //send conmand
                    OutputStream os = s.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    String uniCommand = "broad";
                    pw.write(uniCommand);
                    pw.flush();
                    s.close();
                }catch(IOException e){}
            }
        }.start();
    }

    /*
    *func:save file as .txt format
    * para:fname = file name, fcontent = file content
    */
    public void saveFileTxt(String fname,String fcontent){

    }

    //内部类
    public class uniThread implements Runnable{
        public void run(){
            mOtherHandler.sendEmptyMessage(0x124);
        }
    }

    //-----------wifi处理部分
    //等待分出一个类
    /*
    * func: get wifi[obu]'s ip
    * return: ip in String format
    */
    public String getWifiIP(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        String ipString = intToIp(ipInt);
        return ipString;
//        (String)ip = ip.toString();
//        String ipString = intToIp(ip);
        //test
//        testView.setText(ip);
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }


    /*
    * init record
    * save as:
    */
    public void record(){
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
////        AudioRecord ar = new AudioRecord();
//
//        int frequency = 11025;
//        int channelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
//        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
////        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
//        File file = new File("reverseme.pcm");
//        // Delete any previous recording.
//        if (file.exists())
//            file.delete();

//        sendMsgToTipTV("file created");//ok


//        mUiHandler.sendMessage();
        // Create the new file.
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to create " + file.toString());
//        }

//        try {
//            // Create a DataOuputStream to write the audio data into the saved file.
//            OutputStream os = new FileOutputStream(file);
//            BufferedOutputStream bos = new BufferedOutputStream(os);
//            DataOutputStream dos = new DataOutputStream(bos);
//            // Create a new AudioRecord object to record the audio.
//            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration,  audioEncoding);
//            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                    frequency, channelConfiguration,
//                    audioEncoding, bufferSize);
//            short[] buffer = new short[bufferSize];
//            audioRecord.startRecording();
//            isRecording = true ;
//            while (isRecording) {
//                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
//                for (int i = 0; i < bufferReadResult; i++)
//                    dos.writeShort(buffer[i]);
//            }
//            audioRecord.stop();
//            dos.close();
//        } catch (Throwable t) {
////            Log.e("AudioRecord","Recording Failed");
//        }
//            }
//        }.init();


//        audioRecordTH recordth = new audioRecordTH();
//        recordth.init();
//        recordth.init();

    }
    public void stopRecord(){
        isRecording = false;

    }
    public void initAudioRecorder(){


    }

    //jump to vedio activity
    public void jumpToVideoAC(){
        Intent it = new Intent();
//        it.setClass(MainActivity.this,VideoActivity.class);
        it.setClass(MainActivity.this,VideoActivity1.class);
        it.setClass(MainActivity.this,VideoActivity2.class);
        startActivity(it);
        MainActivity.this.finish();
    }

    //jump to audio activity
    public void jumpToAudioAC(){
        Intent it = new Intent();
        it.setClass(MainActivity.this,AudioActivity.class);
//        it.setClass(MainActivity.this,AudioActivity1.class);
        startActivity(it);
        MainActivity.this.finish();
    }


}



















//package com.example.deonew.car;
//
//import android.graphics.Point;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.Animation.AnimationListener;
//import android.view.animation.TranslateAnimation;
//import android.widget.FrameLayout;
//
///**
// *
// * This activity has a view group that has three panes for left-side menu, <br/>
// * right-side menu and main view.<br/>
// * This class provides a layout like Facebook app.
// *
// * @author abemasafumi
// *
// */
////public class SlideMenuActivity extends FragmentActivity {
// public class MainActivity extends FragmentActivity {
//
//
//    /** Previous touch point */
//    private Point prePoint;
//
//    /** Layout for left-side menu. */
//    private View leftMenuView;
//
//    /** Layout for right-side menu. */
//    private View rightMenuView;
//
//    /** Layout for main view. */
//    private View mainView;
//
//    /** View for touch event. */
//    private View touchView;
//
//    /** Width percent of left-side. */
//    private float leftPercent;
//
//    /** Width percent of right-side. */
//    private float rightPercent;
//
//    /** slide animation */
//    private TranslateAnimation slideAnim;
//
//    /** flag for whether main view was slid. */
//    private boolean isSlid;
//
//    /** flag for whether main view is animating. */
//    private boolean isAnimating;
//
//    /** millisecond time of the slide animation */
//    private static final int ANIMATION_TIME = 200;
//
//    /** Default width percent of left menu. */
//    public static final float DEFAULT_LEFT_WIDTH_PERCENT = 0.6f;
//
//    /** Default width percent of right menu. */
//    public static final float DEFAULT_RIGHT_WIDTH_PERCENT = 0.9f;
//
//    /** Counter of touch move. This is used to check whether swipe is vertical. */
//    private int moveCount;
//
//    /** A flag to check can open left menu. */
//    private boolean canOpenLeft;
//
//    /** A flag to check can open right menu. */
//    private boolean canOpenRight;
//
//    /**
//     * Set the slide menu layout.<br/>
//     * So, DON'T SET CONETNT VIEW.
//     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.__slide_menu);
//
//        init();
//    }
//
//    /**
//     * Resize menus in here because mainview.getWidth is 0
//     * on other life cycle methods.
//     */
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//
//        if (!hasFocus) {
//            return;
//        }
//
//        if (leftPercent < 0) {
//            setLeftPercent(DEFAULT_LEFT_WIDTH_PERCENT);
//        } else {
//            setLeftPercent(leftPercent);
//        }
//
//        if (rightPercent < 0) {
//            setRightPercent(DEFAULT_RIGHT_WIDTH_PERCENT);
//        } else {
//            setRightPercent(rightPercent);
//        }
//    }
//
//    /**
//     * Open the left menu.
//     */
//    public final void openLeftMenu() {
//
//        if (!canOpenLeft) {
//            return;
//        }
//
//        int left = (int) (mainView.getWidth() * leftPercent);
//        int endX = left - mainView.getLeft();
//        isSlid = true;
//
//        leftMenuView.bringToFront();
//        mainView.bringToFront();
//        touchView.bringToFront();
//
//        showSlideAnimation(endX, left);
//    }
//
//    /**
//     * Open the right menu.
//     */
//    public final void openRightMenu() {
//
//        if (!canOpenRight) {
//            return;
//        }
//
//        int left = (int) (-mainView.getWidth() * rightPercent);
//        int endX = left - mainView.getLeft();
//        isSlid = true;
//
//        rightMenuView.bringToFront();
//        mainView.bringToFront();
//        touchView.bringToFront();
//        showSlideAnimation(endX, left);
//    }
//
//    /**
//     * Initialize variables.
//     */
//    private void init() {
//        prePoint = new Point();
//        leftPercent = -1.0f;
//        rightPercent = -1.0f;
//        setCanOpenLeft(true);
//        setCanOpenRight(true);
//        initViews();
//    }
//    /**
//     * set left menu fragment
//     *
//     * @param fragment fragment object that is set.
//     */
//    protected final void setLeftMenuFragment(Fragment fragment) {
//        setFragment(R.id.__slide_left_menu_contents, fragment);
//    }
//
//    /**
//     * set right menu fragment
//     *
//     * @param fragment fragment object that is set.
//     */
//    protected final void setRightMenuFragment(Fragment fragment) {
//        setFragment(R.id.__slide_right_menu_contents, fragment);
//    }
//
//    /**
//     * set main fragment
//     *
//     * @param fragment fragment object that is set.
//     */
//    protected final void setMainFragment(Fragment fragment) {
//        setFragment(R.id.__slide_main_contents, fragment);
//    }
//
//    /**
//     * replace the menu fragment
//     *
//     * @param id resource id of left or right menu
//     * @param fragment Fragment object set.
//     */
//    private void setFragment(int id, Fragment fragment) {
//        if (fragment == null) {
//            throw new IllegalArgumentException("fragment can not be null.");
//        }
//        getSupportFragmentManager().beginTransaction()
//                .replace(id, fragment).commit();
//    }
//
//    /**
//     * Initialize menu views.
//     */
//    private void initViews() {
//        leftMenuView  = (View) findViewById(R.id.__slide_left_menu);
//        rightMenuView = (View) findViewById(R.id.__slide_right_menu);
//        mainView      = (View) findViewById(R.id.__slide_main);
//        touchView     = (View) findViewById(R.id.__slide_touch);
//        touchView.setOnTouchListener(new MainViewTouchListener());
//    }
//
//    /**
//     * check whether can open left menu.
//     *
//     * @return true if can open left menu, false otherwise.
//     */
//    public boolean canOpenLeft() {
//        return canOpenLeft;
//    }
//
//    /**
//     * set can open left menu.
//     *
//     * @param canOpenLeft If true, you can open left menu.
//     */
//    public void setCanOpenLeft(boolean canOpenLeft) {
//        this.canOpenLeft = canOpenLeft;
//    }
//
//    /**
//     * check whether can open right menu.
//     *
//     * @return true if can open right menu, false otherwise.
//     */
//    public boolean canOpenRight() {
//        return canOpenRight;
//    }
//
//    /**
//     * set can open right menu.
//     *
//     * @param canOpenRight If true, you can open right menu.
//     */
//    public void setCanOpenRight(boolean canOpenRight) {
//        this.canOpenRight = canOpenRight;
//    }
//
//    /**
//     * get percent of left menu width.
//     *
//     * @return the leftPercent
//     */
//    public final float getLeftPercent() {
//        return leftPercent;
//    }
//
//    /**
//     * set percent of left menu width and resize left menu view.
//     *
//     * @param leftPercent the leftPercent to set
//     */
//    public final void setLeftPercent(float leftPercent) {
//        this.leftPercent = leftPercent;
//        int width = (int) (mainView.getWidth() * leftPercent);
//        int height = mainView.getHeight();
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
//        leftMenuView.setLayoutParams(params);
//    }
//
//    /**
//     * get percent of right menu width.
//     *
//     * @return the rightPercent
//     */
//    public final float getRightPercent() {
//        return rightPercent;
//    }
//
//    /**
//     * set percent of right menu width and resize right menu view.
//     *
//     * @param rightPercent the rightPercent to set
//     */
//    public final void setRightPercent(float rightPercent) {
//        this.rightPercent = rightPercent;
//        int width = (int) (mainView.getWidth() * rightPercent);
//        int height = mainView.getHeight();
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
//        // reset layout gravity because layout gravity of right menu
//        // will be left when resize.
//        params.gravity = Gravity.RIGHT;
//        rightMenuView.setLayoutParams(params);
//    }
//
//    private void showSlideAnimation(float endX, final int toLeft) {
//
//        slideAnim = new TranslateAnimation(0, endX, 0, 0);
//        slideAnim.setDuration(ANIMATION_TIME);
//        slideAnim.setFillAfter(false);
//        touchView.startAnimation(slideAnim);
//
//        slideAnim = new TranslateAnimation(0, endX, 0, 0);
//        slideAnim.setDuration(ANIMATION_TIME);
//        slideAnim.setFillAfter(false);
//        slideAnim.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//                isAnimating = true;
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mainView.layout(toLeft, mainView.getTop(),
//                        toLeft + mainView.getWidth(), mainView.getTop()
//                                + mainView.getHeight());
//                touchView.layout(toLeft, mainView.getTop(),
//                        toLeft + mainView.getWidth(), mainView.getTop()
//                                + mainView.getHeight());
//                mainView.setAnimation(null);
//                touchView.setAnimation(null);
//                isAnimating = false;
//            }
//        });
//        mainView.startAnimation(slideAnim);
//    }
//
//    /**
//     * Listener class for touch event of main view.
//     *
//     * @author abemasafumi
//     */
//    private class MainViewTouchListener implements View.OnTouchListener {
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//
//            if (v.getLeft() != 0) {
//                isSlid = true;
//                moveCount++;
//            }
//
//            int tx = (int) event.getRawX();
//            int ty = (int) event.getRawY();
//
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    if (slideAnim != null && isAnimating) {
//                        mainView.setAnimation(null);
//                        touchView.setAnimation(null);
//                        isAnimating = false;
//                    }
//                    moveCount = 0;
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                {
//
//                    int dx = Math.abs(tx - prePoint.x);
//                    int dy = Math.abs(ty - prePoint.y);
//
//                    if (moveCount == 0) {
//                        isSlid = dx > dy;
//                        moveCount++;
//                    }
//
//                    if (!isSlid) {
//                        break;
//                    }
//
//                    int left = v.getLeft() + (tx - prePoint.x);
//                    int top = v.getTop();
//
//                    if (left > 0) {
//                        leftMenuView.bringToFront();
//
//                        if (!canOpenLeft()) {
//                            left = 0;
//                        } else if (left > v.getWidth() * leftPercent) {
//                            left = (int) (v.getWidth() * leftPercent);
//                        }
//
//                    } else if (left < 0) {
//                        rightMenuView.bringToFront();
//
//                        if (!canOpenRight()) {
//                            left = 0;
//                        } else if (left < -v.getWidth() * rightPercent) {
//                            left = (int) (-v.getWidth() * rightPercent);
//                        }
//                    }
//
//                    mainView.bringToFront();
//                    touchView.bringToFront();
//
//                    v.layout(left, top, left + v.getWidth(), top + v.getHeight());
//                    mainView.layout(left, top, left + v.getWidth(), top + v.getHeight());
//                    break;
//                }
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_CANCEL:
//                    int left = 0;
//                    float endX = 0.0f;
//
//                    if (mainView.getLeft() > 0) {
//                        int center = (int) (mainView.getWidth() * leftPercent) / 2;
//                        if (mainView.getLeft() > center) {
//                            left = (int) (mainView.getWidth() * leftPercent);
//                            endX = left - mainView.getLeft();
//                        } else {
//                            left = 0;
//                            endX = -mainView.getLeft();
//                        }
//                    } else if (mainView.getLeft() < 0) {
//                        int center = (int) (-mainView.getWidth() * rightPercent) / 2;
//                        if (mainView.getLeft() < center) {
//                            left = (int) (-mainView.getWidth() * rightPercent);
//                            endX = left - mainView.getLeft();
//                        } else {
//                            left = 0;
//                            endX = -mainView.getLeft();
//                        }
//                    }
//
//                    showSlideAnimation(endX, left);
//
//                    if (isSlid) {
//                        event.setAction(MotionEvent.ACTION_CANCEL);
//                    }
//
//                    isSlid = false;
//                    break;
//                default:
//                    break;
//            }
//
//            prePoint.set(tx, ty);
//
//            if (!isSlid) {
//                mainView.dispatchTouchEvent(event);
//            } else if (moveCount == 1) {
//                // create new event to notify cancel event to children of main view.
//                // this process is called only once because cancel event commonly
//                // happens only one time.
//                MotionEvent cancelEvent = MotionEvent.obtain(event);
//                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
//                mainView.dispatchTouchEvent(cancelEvent);
//                cancelEvent.recycle();
//            }
//
//            return true;
//        }
//    }
//}
