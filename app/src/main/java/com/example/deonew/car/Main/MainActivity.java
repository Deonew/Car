package com.example.deonew.car.Main;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.deonew.car.R;

import java.util.ArrayList;
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
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}