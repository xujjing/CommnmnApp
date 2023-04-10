package com.streambus.commonmodule.advert.bean;

import java.io.Serializable;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/2/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class AdvertBean implements Serializable {


    public static final String POSITION_DISPLAY_SPLASH = "Splash";

    /**
     * title : xxxxxxxxx
     * description : xxxx
     * displayPosition : xxx
     * type : 0
     * imageList : ["xxxx","xxxx"]
     * videoList : ["xxxx","xxxx"]
     * link : xxxx
     */
    private String title;
    private String description;
    private String displayPosition;
    private int type;
    private String link;
    private List<String> imageList;
    private List<String> videoList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayPosition() {
        return displayPosition;
    }

    public void setDisplayPosition(String displayPosition) {
        this.displayPosition = displayPosition;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public List<String> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<String> videoList) {
        this.videoList = videoList;
    }
}
