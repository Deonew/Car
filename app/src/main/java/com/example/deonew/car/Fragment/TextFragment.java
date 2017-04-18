package com.example.deonew.car.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.deonew.car.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

        //startSendH264 component
        initComponent();

        //send thread
        mSendTextRun = new sendTextRun();
        new Thread(mSendTextRun).start();
        Log.d(TAG,"start");

        //
//        Looper.prepare();
        mTextFragmentHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d(TAG,"rece");
//                getActivity().getHa8
//                showRecv.append("recv");
//                showRecv.post(new Runnable(){
//                    @Override
//                    public void run() {
//                        showRecv.append("recv");
//                    }
//                });
            }
        };
//        Looper.loop();

    }
    public void initComponent(){
//        UIHandler =
        showRecv = (TextView)getActivity().findViewById(R.id.showRecvText);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
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
                penClick();
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
        final FloatingActionButton floatingActionButton = (FloatingActionButton)getActivity().findViewById(R.id.TextFragmentPen);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                floatingActionButton.
                penClick();
            }
        });
    }
    public void penClick(){
        //submit
        isSendText = true;
    }
    public void recvText(){
        isRecvText = true;
    }
    private EditText toSendEditText = null;

    class sendTextRun implements Runnable{
        @Override
        public void run() {
            while (true){
                if (isSendText){
                    //send

                    //get content
                    toSendEditText = (EditText) getActivity().findViewById(R.id.sendTextInput);
                    String s = toSendEditText.getText().toString();

                    //socket send
                    try {
//                        Socket socket = new Socket("10.105.39.47",20001);
                        Socket socket = new Socket("10.105.36.224 ",20001);
                        DataOutputStream os =  new DataOutputStream(socket.getOutputStream());
                        os.writeUTF(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //
                    isSendText = false;
                }
                if (isRecvText){
                    Log.d(TAG,"success");
                    try {
//                        Socket socket = new Socket("10.105.39.47",20001);
//                        Socket socket = new Socket("192.168.1.109",20001);
                        Socket socket = new Socket("10.105.36.224 ",20001);

                        socket.close();
//                        DataInputStream is =  new DataInputStream(socket.getInputStream());

                        //get text data
                        mTextFragmentHandler.sendEmptyMessage(0);
//                        showRecv.post(new Runnable(){
//                            @Override
//                            public void run() {
//                                showRecv.append("recv");
//                            }
//                        });


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    isRecvText = false;
                }
            }
        }
    }
}
