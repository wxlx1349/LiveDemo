package com.example.wangxi.livedemo.pusher;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.example.wangxi.livedemo.jni.PushNative;
import com.example.wangxi.livedemo.params.VideoParam;

import java.io.IOException;
import java.util.List;

/**
 * Created by wangxi on 2017/3/18.
 */

public class VideoPusher extends Pusher implements SurfaceHolder.Callback,Camera.PreviewCallback {

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private VideoParam videoParam;
    private byte[] buffers;
    private boolean isPushing;
    private PushNative pushNative;

    public VideoPusher(SurfaceHolder surfaceHolder,VideoParam videoParam,PushNative pushNative){
        this.surfaceHolder=surfaceHolder;
        this.videoParam=videoParam;
        this.pushNative=pushNative;
        surfaceHolder.addCallback(this);
    }

    @Override
    public void startPush() {
        //设置视频参数
        pushNative.setVideoOptions(videoParam.getWidth(),
                videoParam.getHeight(), videoParam.getBitrate(), videoParam.getFps());
        isPushing = true;
    }

    @Override
    public void stopPush() {
        isPushing=false;
    }

    @Override
    public void release() {
        stopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public void switchCamera(){
        if(videoParam.getCameraId()== Camera.CameraInfo.CAMERA_FACING_BACK){
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }else {
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        stopPreview();
        startPreview();
    }

    private void startPreview() {
        try {
            //SurfaceView初始化完成，开始相机预览
            camera = Camera.open(videoParam.getCameraId());
            Camera.Parameters parameters = camera.getParameters();
            //设置相机参数
            parameters.setPreviewFormat(ImageFormat.NV21); //YUV 预览图像的像素格式
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            videoParam.setOptimalPreviewSize(sizes);
            parameters.setPreviewSize(videoParam.getWidth(), videoParam.getHeight()); //预览画面宽高
            camera.setParameters(parameters);
            //parameters.setPreviewFpsRange(videoParams.getFps()-1, videoParams.getFps());
            camera.setPreviewDisplay(surfaceHolder);
            //获取预览图像数据
            buffers = new byte[videoParam.getWidth() * videoParam.getHeight() * 4];
            camera.addCallbackBuffer(buffers);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPreview(){
        if(camera!=null){
            camera.stopPreview();
            camera.release();
            camera=null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(camera!=null){
            camera.addCallbackBuffer(buffers);
        }
        if(isPushing){
            pushNative.fireVideo(bytes);
        }
    }

}
