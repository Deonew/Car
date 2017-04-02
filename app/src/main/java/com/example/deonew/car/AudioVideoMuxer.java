package com.example.deonew.car;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;

import java.io.IOException;

/**
 * Created by deonew on 17-3-24.
 */

public class AudioVideoMuxer {
    private MediaMuxer mMuxer;
    private String muxPath = Environment.getExternalStorageDirectory() + "/carTemp.mp4";

    public void initMuxer()throws IOException{
        //file path and format
        mMuxer = new MediaMuxer(muxPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        MediaFormat audioF = MediaFormat.createAudioFormat("");
    }

}
