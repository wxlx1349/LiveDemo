package com.example.wangxi.livedemo.params;

/**
 * Created by wangxi on 2017/3/18.
 */

public class AudioParam {

    private int sampleRateInHz=44100;

    private int channel=1;

    public AudioParam(){

    }

    public AudioParam(int sampleRateInHz,int channel){
        super();
        this.sampleRateInHz=sampleRateInHz;
        this.channel=channel;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
