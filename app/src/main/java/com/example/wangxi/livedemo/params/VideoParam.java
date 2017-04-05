package com.example.wangxi.livedemo.params;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by wangxi on 2017/3/18.
 */

public class VideoParam {

    private int width;
    private int height;
    // 码率480kbps
    private int bitrate = 480000;
    // 帧频默认25帧/s
    private int fps = 20;
    private int cameraId;

    public VideoParam(int width,int height,int cameraId){
        super();
        this.width=width;
        this.height=height;
        this.cameraId=cameraId;
    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setOptimalPreviewSize(List<Camera.Size> sizes){
        if(sizes!=null&&!sizes.isEmpty()){
            int size=sizes.size();
            int index=0;
            int min=Math.abs(sizes.get(0).width-width);
            for(int i=1;i<size;i++){
                if(Math.abs(sizes.get(i).width-width)<min){
                    min=Math.abs(sizes.get(i).width-width);
                    index=i;
                }
            }
            Log.e("tag2","width="+sizes.get(index).width+"height="+sizes.get(index).height+"index="+index);
            width=sizes.get(index).width;
            height=sizes.get(index).height;
        }
    }

    public void setCameraFpsRange( Camera.Parameters parameters){
        List<int[]> ranges= parameters.getSupportedPreviewFpsRange();
        for(int i=0;i<ranges.size();i++){
            Log.e("VideoParam","tag2--range"+ranges.get(i)[0]+" h="+ranges.get(i)[1]);
        }
        int fps=(this.fps)*1000;
        if(ranges!=null&&!ranges.isEmpty()){
            int t=10000000;
            int index=0;
            for(int i=0;i<ranges.size();i++){
                if(ranges.get(i)[0]-fps<t&&ranges.get(i)[0]-fps>0){
                    t=ranges.get(i)[0]-fps;
                    index=i;
                }
            }
            parameters.setPreviewFpsRange(ranges.get(index)[0],ranges.get(index)[1]);
            Log.e("tag2","fps range="+ranges.get(index)[0]+"high="+ranges.get(index)[1]);
        }
    }
}
