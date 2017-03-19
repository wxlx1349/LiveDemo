package com.example.wangxi.livedemo.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.wangxi.livedemo.jni.PushNative;
import com.example.wangxi.livedemo.params.AudioParam;

/**
 * Created by wangxi on 2017/3/18.
 */

public class AudioPusher extends Pusher {

   private AudioParam audioParam;
    private AudioRecord audioRecord;
    private boolean isPushing;
    private int minBufferSize;
    private PushNative pushNative;

    public AudioPusher(AudioParam audioParam,PushNative pushNative){
        this.audioParam=audioParam;
        this.pushNative=pushNative;
        int channelConfig=audioParam.getChannel()==1?
                AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;
        minBufferSize=AudioRecord.getMinBufferSize(audioParam.getSampleRateInHz(),channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,
                audioParam.getSampleRateInHz(),channelConfig,
                AudioFormat.ENCODING_PCM_16BIT,minBufferSize);

    }

    @Override
    public void startPush() {
        isPushing=true;
        pushNative.setAudioOptions(audioParam.getSampleRateInHz(), audioParam.getChannel());
        new Thread(new AudioRecordTask()).start();
    }

    @Override
    public void stopPush() {
        isPushing=false;
        audioRecord.stop();
    }

    @Override
    public void release() {
        if(audioRecord!=null){
            audioRecord.release();
            audioRecord=null;
        }
    }

    class AudioRecordTask implements Runnable{

        @Override
        public void run() {
            audioRecord.startRecording();
            while (isPushing){
                byte[] buffer=new byte[minBufferSize];
                int len=audioRecord.read(buffer,0,buffer.length);
                if(len>0){
                    pushNative.fireAudio(buffer,len);
                }

            }
        }
    }
}
