package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/3/24
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LineBean implements Serializable {

    private ArrayList<LinksBean> linkList; // 播放线路列表

    private ArrayList<SubtitleBean> sbtList; // 字幕

    private String index;  // 播放源编号

    private String title; // 标题

    public ArrayList<LinksBean> getLinkList() {
        return linkList;
    }

    public void setLinkList(ArrayList<LinksBean> linkList) {
        this.linkList = linkList;
    }

    public ArrayList<SubtitleBean> getSbtList() {
        return sbtList;
    }

    public void setSbtList(ArrayList<SubtitleBean> sbtList) {
        this.sbtList = sbtList;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "LineBean{" +
                "linkList=" + linkList +
                ", sbtList=" + sbtList +
                ", index='" + index + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
