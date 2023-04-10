package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/24
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RootVodChannelBean implements Serializable {
    private String result;
    private String message;
    private int contentVersion;

    private int pageSize;
    private int pageNum;
    private int total;

    private ArrayList<ChannelVodBean> channelList;//data; //Search接口 ?

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(int contentVersion) {
        this.contentVersion = contentVersion;
    }

    public ArrayList<ChannelVodBean> getChannelList() {
        return channelList;
    }

    public void setChannelList(ArrayList<ChannelVodBean> data) {
        this.channelList = data;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "RootVodChannelBean{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                ", contentVersion=" + contentVersion +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", total=" + total +
                ", channelList=" + channelList +
                '}';
    }
}
