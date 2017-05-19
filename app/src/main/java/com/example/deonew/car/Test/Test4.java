package com.example.deonew.car.Test;

import android.provider.Settings;
import android.util.Log;

import com.example.deonew.car.Tool.TimeStamp;

public class Test4 {
    public static void main(String aegs[]){
        long g = 99;
        TimeStamp.setAudioStamp(g);
        System.out.println(TimeStamp.getAudioStamp());

        g = 990;
        TimeStamp.setAudioStamp(g);
        System.out.println(TimeStamp.getAudioStamp());
    }
}
