package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/28
 * 描    述:
 * 修订历史：
 * ================================================
 *

 */
public class HistoryBean implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * programme_id : 123
     * programme_name : Thor
     * programme_poster :
     * programme_status : {"progress":123,"media_id":123,"season":1,"index":12,"video_language":"pt","sbt_language":"en"}
     */

    private String programmeId ;
    private String programmeName;
    private String programmePoster;
    private String channelId;  //栏目ID
    private boolean isPlayback;
    private StatusEntity programmeStatus;
    private boolean isFg;  //用于区分当前的选中状态 true>选中
    private float rate;
    private boolean hasSubtitle;

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public boolean isHasSubtitle() {
        return hasSubtitle;
    }

    public void setHasSubtitle(boolean hasSubtitle) {
        this.hasSubtitle = hasSubtitle;
    }

    public String getChannel_id() {
        return channelId;
    }

    public void setChannel_id(String channel_id) {
        this.channelId = channel_id;
    }

    public boolean isIs_playback() {
        return isPlayback;
    }

    public void setIs_playback(boolean is_playback) {
        this.isPlayback = is_playback;
    }

    public HistoryBean() {
    }

    public HistoryBean(String programme_id) {
        this.programmeId = programme_id;
    }

    public boolean isFg() {
        return isFg;
    }

    public void setFg(boolean fg) {
        isFg = fg;
    }

    public String getProgramme_id() {
        return programmeId;
    }

    public void setProgrammeId(String programme_id) {
        this.programmeId = programme_id;
    }

    public String getProgramme_name() {
        return programmeName;
    }

    public void setProgrammeName(String programme_name) {
        this.programmeName = programme_name;
    }

    public String getProgramme_poster() {
        return programmePoster;
    }

    public void setProgrammePoster(String programme_poster) {
        this.programmePoster = programme_poster;
    }

    public StatusEntity getProgramme_status() {
        return programmeStatus;
    }

    public void setProgrammeStatus(StatusEntity programme_status) {
        this.programmeStatus = programme_status;
    }

    public static class StatusEntity implements Serializable{
        private static final long serialVersionUID = 1L;
        /**
         * progress : 123
         * media_id : 123
         * season : 1
         * index : 12
         * video_language : pt
         * sbt_language : en
         */

        private int progress;
        private int mediaId;
        private int season;
        private int index;
        private String videoLanguage;
        private String sbtLanguage;

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getMedia_id() {
            return mediaId;
        }

        public void setMedia_id(int media_id) {
            this.mediaId = media_id;
        }

        public int getSeason() {
            return season;
        }

        public void setSeason(int season) {
            this.season = season;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getVideo_language() {
            return videoLanguage;
        }

        public void setVideo_language(String video_language) {
            this.videoLanguage = video_language;
        }

        public String getSbt_language() {
            return sbtLanguage;
        }

        public void setSbt_language(String sbt_language) {
            this.sbtLanguage = sbt_language;
        }

        @Override
        public String toString() {
            return "StatusEntity{" +
                    "progress=" + progress +
                    ", mediaId=" + mediaId +
                    ", season=" + season +
                    ", index=" + index +
                    ", videoLanguage='" + videoLanguage + '\'' +
                    ", sbtLanguage='" + sbtLanguage + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HistoryBean that = (HistoryBean) o;
        return programmeId!=null && programmeId.equals(that.programmeId);
    }

    @Override
    public int hashCode() {
        return programmeId != null ? programmeId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HistoryBean{" +
                "programme_id='" + programmeId + '\'' +
                ", programme_name='" + programmeName + '\'' +
                ", programme_poster='" + programmePoster + '\'' +
                ", programme_status=" + programmeStatus + '\'' +
                ", isFg=" + isFg + '\'' +
                ",status=" + programmeStatus + '\'' +
                '}';
    }
}
