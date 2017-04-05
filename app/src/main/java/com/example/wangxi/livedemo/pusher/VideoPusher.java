package com.example.wangxi.livedemo.pusher;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.wangxi.livedemo.jni.PushNative;
import com.example.wangxi.livedemo.params.VideoParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxi on 2017/3/18.
 */

public class VideoPusher extends Pusher implements SurfaceHolder.Callback,Camera.PreviewCallback {
    public static final int MIN_REMOVE_GAP = 4;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private VideoParam videoParam;
    private byte[] buffers;
    private boolean isPushing;
    private PushNative pushNative;
    long time;
    Handler handler;
    HandlerThread handlerThread;
    List<FrameData> frameList=new ArrayList<>();
    int count;
    int removeIndex;
    int removeMinGap=MIN_REMOVE_GAP;
    int removeSize=35;

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
//        handlerThread.stop();
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
//            parameters.getSupportedPreviewFpsRange();
            videoParam.setCameraFpsRange(parameters);
            videoParam.setOptimalPreviewSize(sizes);
            parameters.setPreviewSize(videoParam.getWidth(), videoParam.getHeight()); //预览画面宽高
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);

            initHandlerThread();
            //获取预览图像数据
            buffers = new byte[videoParam.getWidth() * videoParam.getHeight() * 4];
            camera.addCallbackBuffer(buffers);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initHandlerThread(){
        handlerThread=new HandlerThread("video",10){

            public boolean handleMessage(Message msg) {
                byte[] bytes= (byte[]) msg.obj;
                long startTime=System.currentTimeMillis();
                if(isPushing){
                    pushNative.fireVideo(bytes);
                }
                Log.e("tag2","thread="+Thread.currentThread().getName()+"time="
                        +(System.currentTimeMillis()-startTime)+"time2="+(System.currentTimeMillis()-time));
                time=System.currentTimeMillis();
                return true;
            }
        };
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
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
        count++;
//        count=(count+1)%3;
        frameList.add(new FrameData(count,bytes));
        removeFrameData();
        handler.post(new VideoTask());
    }

    private void removeFrameData(){
        if(frameList.size()>removeSize){
            int size=frameList.size();
            synchronized (frameList){
                List removeList=new ArrayList();
                int removeMaxGap=8;
                int frameSize=frameList.size();
                while (frameSize>removeSize-5&&removeMaxGap>removeMinGap){
                    FrameData info=frameList.get(0);
                    int index=info.index;
                    int aCount=1;
                    for(int i=1;i<frameSize;i++){
                        info=frameList.get(i);
                        if(index+1==info.index){
                            aCount++;
                        }else {
                            aCount=1;
                        }
                        index=info.index;
                        if(aCount>=removeMaxGap){
                            removeList.add(frameList.get(i-aCount/2));
                            aCount=aCount/2;
                        }else if(i==frameSize-1&&aCount>3&&frameSize-removeList.size()>removeSize-5){
                            removeList.add(frameList.get(i));
                        }
                    }
                    removeMaxGap--;
                    if(removeList.size()>0){
                        frameList.removeAll(removeList);
                    }
                    frameSize=frameList.size();
                }
                if(removeMinGap==MIN_REMOVE_GAP-1){
                    removeMinGap=MIN_REMOVE_GAP;
                }else if(frameList.size()>removeSize-5){
                    removeMinGap=MIN_REMOVE_GAP-1;
                }
                Log.e("tag2","1frameList size="+frameList.size()+" removeSize"+(size-frameList.size()));
//                if(removeList.size()>0){
////                    removeIndex=(removeIndex+3)%removeGap;
//                    frameList.removeAll(removeList);
//                }
            }
        }
    }

    private class VideoTask implements Runnable{
        public void run() {
            FrameData data=null;
            long startTime=System.currentTimeMillis();
            synchronized (frameList){
                if(frameList.size()>0){
                    data=frameList.remove(0);
                }else {
                    return;
                }
            }
            Log.e("tag2","frameList size="+frameList.size()+" startTime="+System.currentTimeMillis()+"index="+data.index);
            byte[] bytes=data.bytes;
            if(isPushing&&bytes.length>0){
                pushNative.fireVideo(bytes);
            }
            Log.e("tag2","thread="+Thread.currentThread().getName()+"time="
                    +(System.currentTimeMillis()-startTime)+"time2="+(System.currentTimeMillis()-time)+"index="+data.index);
            time=System.currentTimeMillis();
        }
    }

    private class FrameData{
        byte[] bytes;
        int index;
        public FrameData(int index,byte[] data){
            bytes=data;
            this.index=index;
        }
    }
}
