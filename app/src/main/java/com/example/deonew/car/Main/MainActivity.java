package com.example.deonew.car.Main;

import android.app.ActionBar;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.deonew.car.Fragment.TextFragment;
import com.example.deonew.car.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private MainViewPagerAdapter mainViewPagerAdapter;
//    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private EditText sendEditText = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getWifiIP();

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
        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        viewPager.setAdapter(mainViewPagerAdapter);
//viewPager.getCurrentItem()
        //set default page
        viewPager.setCurrentItem(1);

        //tablayout and viewpaer
        mainTabLayout.setupWithViewPager(viewPager);


        //fill in width
        mainTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mainTabLayout.setTabMode(TabLayout.MODE_FIXED);


    }


    /*
    * func: get wifi[obu]'s ip
    * return: ip in String format
    */
    public String getWifiIP(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        String ipString = intToIp(ipInt);
        Log.d(TAG," ip: "+ipString);
        return ipString;
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }



    public void sendTextClick(){
        sendEditText = (EditText) findViewById(R.id.sendTextInput);
        String s = sendEditText.getText().toString();
        TextFragment te = (TextFragment)mainViewPagerAdapter.getFragment(0);
        te.sendText(s);
//        viewPager.getCurrentItem().send
//        mainViewPagerAdapter.getItem(0).sendText(s);
    }
//
//    class sendTextRun extends Thread{
//        private String sendS = null;
//        public sendTextRun(String s){
//            this.sendS = s;
//        }
//        @Override
//        public void run() {
//
//
//            //UDP send
//            try {
//
//                byte[] strBytes = sendS.getBytes();
//                InetAddress sendAddr = InetAddress.getByName("10.202.0.202");
//                DatagramPacket dpSend = new DatagramPacket(strBytes,strBytes.length,sendAddr,9997);
//                ds.send(dpSend);
//                Log.d(TAG,"send: "+sendS);
//            }catch (IOException e){}
//        }
//    }
}