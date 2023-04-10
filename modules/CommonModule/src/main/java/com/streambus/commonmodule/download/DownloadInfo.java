package com.streambus.commonmodule.download;

import androidx.lifecycle.MutableLiveData;

import com.streambus.commonmodule.bean.ChannelVodBean;
import com.streambus.commonmodule.bean.LinksBean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/8/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class DownloadInfo implements Serializable {
    private ChannelVodBean channelBean;
    private LinksBean linkBean;
    private String playPath;

    public DownloadInfo() {
    }

    public DownloadInfo(ChannelVodBean channelBean, LinksBean linkBean, String playPath) {
        this.channelBean = channelBean;
        this.linkBean = linkBean;
        this.playPath = playPath;
    }

    public ChannelVodBean getChannelBean() {
        return channelBean;
    }

    public void setChannelBean(ChannelVodBean channelBean) {
        this.channelBean = channelBean;
    }

    public LinksBean getLinkBean() {
        return linkBean;
    }

    public void setLinkBean(LinksBean linkBean) {
        this.linkBean = linkBean;
    }

    public String getPlayPath() {
        return playPath;
    }

    public void setPlayPath(String playPath) {
        this.playPath = playPath;
    }
}
