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

public class MainActivity extends AppCompatActivity{
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
//        mAudioSendTH = new Thread(new AudioSendRun());
//        mAudioSendTH.start();

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
}