package com.example.deonew.car.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.deonew.car.R;
import com.example.deonew.car.Video.VideoActivity2;
import com.example.deonew.car.Video.VideoActivity3;

/**
 * Created by deonew on 17-4-5.
 */

public class VideoFragment extends Fragment{
    public static VideoFragment newInstance(int page) {
        VideoFragment audioFragment = new VideoFragment();
        return audioFragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //
        Button button = (Button)getActivity().findViewById(R.id.toVideo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toVideoActivity(v);
            }
        });
        Button button1 = (Button)getActivity().findViewById(R.id.toVideo3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toVideoActivity3(v);
            }
        });

    }

    public void toVideoActivity(View v){
        Intent it = new Intent();
        it.setClass(getActivity(),VideoActivity2.class);
        startActivity(it);
        getActivity().finish();
    }
    public void toVideoActivity3(View v){
        Intent it = new Intent();
        it.setClass(getActivity(),VideoActivity3.class);
        startActivity(it);
        getActivity().finish();
    }
}
