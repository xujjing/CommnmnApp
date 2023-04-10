package com.streambus.commonmodule.bean;

import android.content.Context;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/7
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FeedbackInfo extends SystemInfo{
    public static final int TAG_BROKEN =0;
    public static final int TAG_NOT_PLAY=1;
    public static final int TAG_BLACK_SCREEN=2;
    public static final int TAG_AUDIO_PROBLEM=3;
    public static final int TAG_OTHER =4;

    /**视频类型，1直播还是2点播*/
    private String mVideoType="";

    /**所在栏目*/
    private String mCategory="";

    /**节目名字*/
    private String mChannelName="";

    /**播放器解码方式*/
    private String mDecoder="";

    /**当前播放的视频链接,p2p的返回filmid，如果是m3u8的直接返回视频链接*/
    private String mVideoURL="";

    /**当前播放器状态     0：正常；1：卡顿；3：中断*/
    private String mPlayerState="";

    /**当前APP网速*/
    private String mNetworkSpeed="";

    /**自定义异常选项类型*/
    private String mExceptionType="";

    /**异常描述或者建议*/
    private String mExceptionDescription="";

    /**终端对当前节目的IP进行ping的返回结果;如果是p2p的ping：104.148.36.58;m3u8的ping对应ip*/
    private String mPingInfo="";

    public FeedbackInfo(Context context){
        super(context);
    }

    public String getVideoType() {
        return mVideoType;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public String getExceptionType() {
        return mExceptionType;
    }

    public String getExceptionDescription() {
        return mExceptionDescription;
    }

    public String getDecoder() {
        return mDecoder;
    }

    public String getVideoURL() {
        return mVideoURL;
    }

    public String getNetworkSpeed() {
        return mNetworkSpeed;
    }

    public String getPlayerState() {
        return mPlayerState;
    }

    public String getPingInfo() {
        return mPingInfo;
    }

    public void setVideoType(String mVideoType) {
        this.mVideoType = mVideoType;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public void setChannelName(String mChannelName) {
        this.mChannelName = mChannelName;
    }

    public void setExceptionType(String mExceptionType) {
        this.mExceptionType = mExceptionType;
    }

    public void setExceptionDescription(String mExceptionDescription) {
        this.mExceptionDescription = mExceptionDescription;
    }

    public void setDecoder(String mDecoder) {
        this.mDecoder = mDecoder;
    }

    public void setVideoURL(String mVideoURL) {
        this.mVideoURL = mVideoURL;
    }

    public void setNetworkSpeed(String mNetworkSpeed) {
        this.mNetworkSpeed = mNetworkSpeed;
    }

    public void setPlayerState(String mPlayerState) {
        this.mPlayerState = mPlayerState;
    }

    public void setPingInfo(String mPingInfo) {
        this.mPingInfo = mPingInfo;
    }

}
