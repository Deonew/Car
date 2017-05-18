package com.example.deonew.car.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.deonew.car.Main.MainActivity;
import com.example.deonew.car.R;
import com.example.deonew.car.Video.VideoActivity3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by deonew on 17-4-4.
 */

public class TextFragment extends Fragment{
    public String TAG = "TEXTFRAGMENT";
    //thread
    private sendTextRun mSendTextRun;

    //control
    private boolean isRecvText = false;
    private boolean isSendText = false;

    //component
    private TextView showRecv;


    private DatagramSocket ds = null;
    private MainActivity mainAC=null;

    private EditText e;
    //ui handler
    private Handler UIHandler;
//    public void setUIHandler(Handler h){
//        this.UIHandler = h;
//    }

    private Handler mTextFragmentHandler;
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
//

        //startSendH264 component
        initComponent();
//

    }
    public void initComponent(){
        showRecv = (TextView)getActivity().findViewById(R.id.showRecvText);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //send text
        Button sendBtn = (Button)getActivity().findViewById(R.id.mainSendTextBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClick();
//                (MainActivity)getActivity().sendText();
            }
        });

        //recv text
        Button recvBtn = (Button)getActivity().findViewById(R.id.mainRecvTextBtn);
        recvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recvText();
            }
        });

        //set click event
//        final FloatingActionButton floatingActionButton = (FloatingActionButton)getActivity().findViewById(R.id.TextFragmentPen);
//        floatingActionButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
////                floatingActionButton.
//                sendClick();
//            }
//        });

        mainAC  = (MainActivity)getActivity();
        e = (EditText)mainAC.findViewById(R.id.sendTextInput);
        if (e == null){
            Log.d(TAG,"errrrrr");
        }else {
            Log.d(TAG,"its ok");
        }


        //

        ListView lv = (ListView) mainAC.findViewById(R.id.textHistoryList);
        ssss.add("hhh");
        textSendAdapter = new ArrayAdapter<String>(this.getContext(),R.layout.text_send_item,ssss);
        lv.setAdapter(textSendAdapter);

    }
    private ArrayAdapter<String> textSendAdapter;
    private ArrayList<String> ssss = new ArrayList();

    public void sendClick(){
        mainAC.sendTextClick();
    }
    public void sendText(String s){

        ssss.add(s);
        textSendAdapter.notifyDataSetChanged();
        new sendTextRun(s).start();
    }
    public void recvText(){
        isRecvText = true;
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
}
