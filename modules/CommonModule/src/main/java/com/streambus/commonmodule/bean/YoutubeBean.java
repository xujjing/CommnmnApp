package com.streambus.commonmodule.bean;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/10/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class YoutubeBean {

    private String mTitle;
    private List<JSONObject> mChannelFormats;
    private List<JSONObject> mAudioFormats;
    private List<JSONObject> mVideoFormats;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void addChannelFormat(JSONObject channelFormat) {
        if (mChannelFormats == null) {
            mChannelFormats = new ArrayList<>();
        }
        mChannelFormats.add(channelFormat);
    }

    public void addAdaptiveAudioFormat(JSONObject audioFormat) {
        if (mAudioFormats == null) {
            mAudioFormats = new ArrayList<>();
        }
        mAudioFormats.add(audioFormat);
    }

    public void addAdaptiveVideoFormat(JSONObject videoFormat) {
        if (mVideoFormats == null) {
            mVideoFormats = new ArrayList<>();
        }
        mVideoFormats.add(videoFormat);
    }

    public String preferredChannelFormatUrl() {
        if (mChannelFormats != null && !mChannelFormats.isEmpty()) {
            for (JSONObject channelFormat : mChannelFormats) {
                if ("720p".equals(channelFormat.optString("qualityLabel"))) {
                    return channelFormat.optString("url");
                }
            }
            return mChannelFormats.get(0).optString("url");
        }
        return null;
    }

    public String preferredVideoFormatUrl() {
        if (mVideoFormats != null && !mVideoFormats.isEmpty()) {
            for (JSONObject channelFormat : mVideoFormats) {
                if ("720p".equals(channelFormat.optString("qualityLabel"))) {
                    return channelFormat.optString("url");
                }
            }
            return mVideoFormats.get(0).optString("url");
        }
        return null;
    }

    public String preferredAudioFormatUrl() {
        if (mAudioFormats != null && !mAudioFormats.isEmpty()) {
            return mAudioFormats.get(0).optString("url");
        }
        return null;
    }




    /************************************* - Formats - ****************************************/
    public static class ChannelFormat {
        /**
         * approxDurationMs : 1220812
         * audioChannels : 2
         * audioQuality : AUDIO_QUALITY_MEDIUM
         * audioSampleRate : 44100
         * bitrate : 333456
         * fps : 30
         * width : 1280
         * height : 720
         * itag : 22
         * lastModified : 1603925675492686
         * mimeType : video/mp4;
         * projectionType : RECTANGULAR
         * quality : hd720
         * qualityLabel : 720p
         * url : https://r1---sn-nx57ynld.googlevideo.
         */
        private String approxDurationMs;
        private int audioChannels;
        private String audioQuality;
        private String audioSampleRate;
        private int bitrate;
        private int fps;
        private int width;
        private int height;
        private int itag;
        private String lastModified;
        private String mimeType;
        private String projectionType;
        private String quality;
        private String qualityLabel;
        private String url;
    }



    /************************************* - AdaptiveFormats - ****************************************/
    public static class AdaptiveAudioFormat {
        /**
         * approxDurationMs : 1220858
         * audioChannels : 2
         * audioQuality : AUDIO_QUALITY_ULTRALOW
         * audioSampleRate : 22050
         * averageBitrate : 30786
         * bitrate : 32855
         * contentLength : 4698317
         * indexRange : {"end":"2148","start":"641"}
         * initRange : {"end":"640","start":"0"}
         * itag : 599
         * lastModified : 1603924662657566
         * loudnessDb : -6.7999992
         * mimeType : audio/mp4;
         * projectionType : RECTANGULAR
         * quality : tiny
         * url : https://r1---sn-nx57ynld.googlevideo.com/
         */
        private String approxDurationMs;
        private int audioChannels;
        private String audioQuality;
        private String audioSampleRate;
        private int averageBitrate;
        private int bitrate;
        private String contentLength;
        private RangeEntity indexRange;
        private RangeEntity initRange;
        private int itag;
        private String lastModified;
        private double loudnessDb;
        private String mimeType;
        private String projectionType;
        private String quality;
        private String url;
    }


    public static class AdaptiveVideoFormat{
        /**
         * approxDurationMs : 1220752
         * averageBitrate : 865390
         * bitrate : 2420561
         * contentLength : 132053433
         * fps : 30
         * height : 1080
         * indexRange : {"end":"3748","start":"741"}
         * initRange : {"end":"740","start":"0"}
         * itag : 137
         * lastModified : 1603924766624426
         * mimeType : video/mp4;
         * projectionType : RECTANGULAR
         * quality : hd1080
         * qualityLabel : 1080p
         * url : https://r1---sn-nx57ynld.googlevideo.
         * width : 1920
         */
        private String approxDurationMs;
        private int averageBitrate;
        private int bitrate;
        private String contentLength;
        private int fps;
        private int height;
        private RangeEntity indexRange;
        private RangeEntity initRange;
        private int itag;
        private String lastModified;
        private String mimeType;
        private String projectionType;
        private String quality;
        private String qualityLabel;
        private String url;
        private int width;
    }

    public static class RangeEntity {
        /**
         * end : 3748
         * start : 741
         */
        private String end;
        private String start;
    }

}
