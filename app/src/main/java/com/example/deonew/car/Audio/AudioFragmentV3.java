package com.example.deonew.car.Audio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.deonew.car.R;
import com.example.deonew.car.Video.VideoActivity3;

/**
 * Created by deonew on 4/24/17.
 */

public class AudioFragmentV3 extends Fragment {

    private String TAG = "AudioFragmentV3";

    private VideoActivity3 mainAC;
    private RecordAACV3 recordAACV3;
    private SendAACV3 sendAACV3;
    private RecvAACV3 recvAACV3;
    private PlayAACV3 playAACV3;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainAC = (VideoActivity3)getActivity();
        return inflater.inflate(R.layout.fragment_audio_v3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //
//        sendAAC = new SendAAC(mainAC);
        recordAACV3 = new RecordAACV3(mainAC);

//        sendAACV3 = new SendAACV3(mainAC);

//        recvAACV3 = new RecvAACV3(mainAC);

        playAACV3 = new PlayAACV3(mainAC);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initBtn();

    }

    public void initBtn(){
        Button button = (Button)getActivity().findViewById(R.id.recordAACV3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"clicked");
                startRecordAACV3();
            }
        });
        Button sendBtn = (Button)getActivity().findViewById(R.id.sendAACV3);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSendAACV3();
                Log.d(TAG,"send aac");
            }
        });
        Button recvBtn = (Button)getActivity().findViewById(R.id.recvAACV3);
        recvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecvAACV3();
                Log.d(TAG,"recv aac");
            }
        });
        Button playBtn = (Button)getActivity().findViewById(R.id.playAACV3);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayAACV3();
                Log.d(TAG,"play aac");
            }
        });
    }

    public void startRecordAACV3(){
        recordAACV3.startRecord();
    }

    public void startSendAACV3(){
        mainAC.startSendAAC();
//        sendAACV3.startSendAAC();
    }

    public void startRecvAACV3(){
        mainAC.startRecvAAC();
    }

    public void startPlayAACV3(){
        playAACV3.start();
    }



}
