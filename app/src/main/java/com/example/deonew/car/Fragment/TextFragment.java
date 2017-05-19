package com.example.deonew.car.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.deonew.car.Main.MainActivity;
import com.example.deonew.car.R;
import com.example.deonew.car.Text.ReceiveItem;
import com.example.deonew.car.Text.RecvAdapter;
import com.example.deonew.car.Text.SendHistoryAdapter;
import com.example.deonew.car.Text.SendTextItem;
import com.suke.widget.SwitchButton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deonew on 17-4-4.
 */

public class TextFragment extends Fragment{
    public String TAG = "TEXTFRAGMENT";
    //control
    private boolean isRecvText = false;
    private boolean isSendText = false;

    private DatagramSocket ds = null;
    private MainActivity mainAC=null;

    private EditText e;

    private ListView sendListView;
    private ListView recvListView;

    private List<SendTextItem> sendHistoryList=null;
    private SendHistoryAdapter sendAdapter=null;

    private List<ReceiveItem> receiveList = null;
    private RecvAdapter receiveAdapter=null;

    private String currentRecv = null;

    public static TextFragment newInstance(int page){
        TextFragment textFragment = new TextFragment();
        return textFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ds = new DatagramSocket(9997);
        }catch (IOException e){}

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text,container,false);
        return view;
    }

    private FloatingActionButton sendFloatBtn = null;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainAC  = (MainActivity)getActivity();

        sendFloatBtn = (FloatingActionButton)getActivity().findViewById(R.id.TextFragmentPen);
        sendFloatBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendClick();
            }
        });

        final com.suke.widget.SwitchButton switchButton = (com.suke.widget.SwitchButton)getActivity().findViewById(R.id.recvSwitchButton);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (switchButton.isChecked()){
                    isRecvText = true;
                }else {
                    isRecvText = false;
                }
            }
        });





        initSend();
        initRecv();

        new receiveTH().start();

    }
    public void initSend(){
        sendListView = (ListView) mainAC.findViewById(R.id.textHistoryList);
        sendListView.setDivider(null);
        LayoutInflater inflater = mainAC.getLayoutInflater();
        sendHistoryList = new ArrayList<SendTextItem>();
        SendTextItem s1 = new SendTextItem("s1");
        SendTextItem s2 = new SendTextItem("s1");
        sendHistoryList.add(s1);
        sendHistoryList.add(s2);
        sendAdapter = new SendHistoryAdapter(inflater,sendHistoryList);
        sendListView.setAdapter(sendAdapter);
    }
    public void initRecv(){
        recvListView = (ListView) mainAC.findViewById(R.id.recvTextListView);
        recvListView.setDivider(null);
        LayoutInflater inflater = mainAC.getLayoutInflater();
        receiveList = new ArrayList<ReceiveItem>();
        ReceiveItem r = new ReceiveItem("ni");
        receiveList.add(r);
        receiveAdapter = new RecvAdapter(inflater,receiveList);
        recvListView.setAdapter(receiveAdapter);
    }

    public void sendClick(){
        mainAC.sendTextClick();
    }
    public void sendText(String s){
        //fresh
        sendHistoryList.add(new SendTextItem(s));
        sendAdapter.notifyDataSetChanged();

        new sendTextRun(s).start();
    }

//    public void onceRecv(String s){
//        mainAC.onceTextRecv(s);
//    }
    public void receiveText(){
        //fresh
        receiveList.add(new ReceiveItem(currentRecv));
        receiveAdapter.notifyDataSetChanged();
    }
    class sendTextRun extends Thread{
        private String sendS = null;
        public sendTextRun(String s){
            this.sendS = s;
        }
        @Override
        public void run() {
            //UDP send
            try {
                byte[] strBytes = sendS.getBytes();
                InetAddress sendAddr = InetAddress.getByName("10.202.0.202");
                DatagramPacket dpSend = new DatagramPacket(strBytes,strBytes.length,sendAddr,9997);
                ds.send(dpSend);
                Log.d(TAG,"send: "+sendS);
            }catch (IOException e){}
        }
    }
    class receiveTH extends Thread{
        @Override
        public void run() {
            super.run();
            byte[] recvBuff = new byte[1024];
            DatagramPacket dpRecv = new DatagramPacket(recvBuff,1024);
            while(true){
                if (isRecvText){
                    try {
                        ds.receive(dpRecv);
                        byte[] buffer = dpRecv.getData();
                        int len = dpRecv.getLength();
                        byte[] t = new byte[len];
                        System.arraycopy(buffer,0,t,0,len);
                        String s = new String(t);
                        currentRecv = s;
                        receiveText();


                    }catch (IOException e){}
                }
            }
        }
    }
}
