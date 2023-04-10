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
 */
public class FavoriteBean implements Serializable {

    /**
     * programme_id : 123
     * programme_name : Thor
     * programme_poster : 图片
     */

    private String programmeId; //节目ID
    private String programmeName;
    private String programmePoster;
    private String channelId; //栏目ID
    private boolean isPlayback;
    private int type;  //type 1 收藏 2取消
    private boolean isFg;  //用于区分当前的选中状态 true>选中
    private String channelEpg;//存放临时Epg
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

    public String getChannelEpg() {
        return channelEpg;
    }

    public void setChannelEpg(String channelEpg) {
        this.channelEpg = channelEpg;
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

    public boolean isFg() {
        return isFg;
    }

    public void setFg(boolean fg) {
        isFg = fg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public FavoriteBean(String programme_id) {
        this.programmeId = programme_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FavoriteBean that = (FavoriteBean) o;
        return programmeId != null && programmeId.equals(that.programmeId);
    }

    @Override
    public int hashCode() {
        return programmeId != null ? programmeId.hashCode() : 0;
    }

}
