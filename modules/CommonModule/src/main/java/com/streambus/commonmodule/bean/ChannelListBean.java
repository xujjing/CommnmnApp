package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huanglu on 2017/5/22.
 */

public class ChannelListBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;///
    private String name;//

    private List<ChannelBean> channelList;

    public List<ChannelBean> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<ChannelBean> channelList) {
        this.channelList = channelList;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
