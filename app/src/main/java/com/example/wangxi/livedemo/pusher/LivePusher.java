package com.example.wangxi.livedemo.pusher;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.example.wangxi.livedemo.jni.PushNative;
import com.example.wangxi.livedemo.listener.LiveStateChangeListener;
import com.example.wangxi.livedemo.params.AudioParam;
import com.example.wangxi.livedemo.params.VideoParam;

/**
 * Created by wangxi on 2017/3/18.
 */

public class LivePusher implements SurfaceHolder.Callback{

    private SurfaceHolder surfaceHolder;
    private VideoPusher videoPusher;
    private AudioPusher audioPusher;
    private PushNative pushNative;

    public LivePusher(SurfaceHolder surfaceHolder){
        this.surfaceHolder=surfaceHolder;
        surfaceHolder.addCallback(this);
        prepare();
    }

    private void prepare(){
        pushNative=new PushNative();
        VideoParam videoParam=new VideoParam(720,480, Camera.CameraInfo.CAMERA_FACING_BACK);
        videoPusher =new VideoPusher(surfaceHolder,videoParam,pushNative);

        AudioParam audioParam=new AudioParam();
        audioPusher=new AudioPusher(audioParam,pushNative);
    }

    public void switchCamera(){
        videoPusher.switchCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPush();
        release();
    }

    public void startPush(String url,LiveStateChangeListener liveStateChangeListener) {
        videoPusher.startPush();
        audioPusher.startPush();
        pushNative.startPush(url);
        pushNative.setLiveStateChangeListener(liveStateChangeListener);
    }

    public void stopPush() {
        videoPusher.stopPush();
        audioPusher.stopPush();
        pushNative.stopPush();
        pushNative.removeLiveStateChangeListener();

    }

    public void release() {
        videoPusher.release();
        audioPusher.release();
        pushNative.release();

    }
}
