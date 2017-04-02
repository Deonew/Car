package com.example.deonew.car;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Created by deonew on 17-3-2.
 */

//audio record thread
class audioRecordTH extends Thread
{
    protected AudioRecord m_in_rec ;
    protected int         m_in_buf_size ;
    protected byte []     m_in_bytes ;
    protected boolean     m_keep_running ;
    protected Socket s;
    protected DataOutputStream dout;
    protected LinkedList<byte[]> m_in_q ;
    public void init()
    {
        m_in_buf_size =  AudioRecord.getMinBufferSize(8000,

                AudioFormat.CHANNEL_CONFIGURATION_MONO,

                AudioFormat.ENCODING_PCM_16BIT);



        m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC,

                8000,

                AudioFormat.CHANNEL_CONFIGURATION_MONO,

                AudioFormat.ENCODING_PCM_16BIT,

                m_in_buf_size) ;



        m_in_bytes = new byte [m_in_buf_size] ;



        m_keep_running = true ;

        m_in_q=new LinkedList<byte[]>();



        try

        {

            s=new Socket("192.168.1.100",4332);

            dout=new DataOutputStream(s.getOutputStream());

            //new Thread(R1).start();

        }

        catch (UnknownHostException e)

        {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

        catch (IOException e)

        {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

    }

    @Override
    public void run() {
        int minBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);


        try{

            byte[] bytes_pkg;
            // start
            mRecord.startRecording();
//            while (isRecording) {
            while (true) {
                m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
                bytes_pkg = m_in_bytes.clone();
                if (m_in_q.size() >= 2) {
                    dout.write(m_in_q.removeFirst(), 0, m_in_q.removeFirst().length);
                }
                m_in_q.add(bytes_pkg);
            }
//            dout.close();

        }catch(Exception e)
        {
            e.printStackTrace();
        }

//                mRecord.read(m_in_bytes, 0, minBufferSize);
//                bytes_pkg = m_in_bytes.clone();
//                if (m_in_q.size() >= 2)
//                {
//                    m_in_q.removeFirst();
//                }
//                m_in_q.add(bytes_pkg);
    }

}