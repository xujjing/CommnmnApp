package com.streambus.commonmodule.bean;

import com.streambus.commonmodule.advert.bean.AdvertBean;

import java.io.Serializable;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeItemBean implements Serializable {

    private List<AdvertBean> advertList;

    private List<ChannelVodBean> channelList;
    private List<ColumnBean> columnList;

    public List<ChannelVodBean> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<ChannelVodBean> channelList) {
        this.channelList = channelList;
    }

    public List<ColumnBean> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ColumnBean> columnList) {
        this.columnList = columnList;
    }


    public List<AdvertBean> getAdvertList() {
        return advertList;
    }

    public void setAdvertList(List<AdvertBean> advertList) {
        this.advertList = advertList;
    }
}
