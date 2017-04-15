package com.example.deonew.car.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.deonew.car.Audio.AudioActivity2;
import com.example.deonew.car.R;

/**
 * Created by deonew on 17-4-4.
 */

public class AudioFragment extends Fragment {



    public static AudioFragment newInstance(int page) {
        AudioFragment audioFragment = new AudioFragment();
        return audioFragment;
    }

//    public void toggleAudio(View v){
//        isAudioRecording = !isAudioRecording;
//        if (isAudioRecording){
//            mAudioDataCatchThread = new Thread(new audioDataCatch());
//            mAudioDataCatchThread.startSendH264();
//        }else{}
//    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        initAudioRec();

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        return view;

    }

    private Button sendBtn;
    private Button recvBtn;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //startSendH264 button
        sendBtn = (Button)getActivity().findViewById(R.id.sendAudio);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    //to audio activity 2
//                    Intent it = new Intent();
//                    it.setClass(getActivity(),AudioActivity2.class);
//                    startActivity(it);
//                    getActivity().finish();

                toAudioActivity(v);

            }
        });

    }
    public void toAudioActivity(View v){
        Intent it = new Intent();
        it.setClass(getActivity(),AudioActivity2.class);
        startActivity(it);
        getActivity().finish();
    }


}
