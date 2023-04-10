package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by huanglu on 2017/5/22.
 */

public class ChannelBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;///
    private String name;//
    private String ftype;///
    private String description;

    private String img;
    private ArrayList<PictureBean> pictureList;
    private String proType;
    private String type;
    private String playType; ///自己添加的，用於在FAV和HIS裏面判斷是否PLAYBACK
    private ArrayList<LinksBean> links;//
    private ArrayList<TagBean>tagList;

    //lzg edit
    private ArrayList<CategoryBean> catagoryList;
    private String categoryId;
    private String categoryName;

    //--------------------保存当前播放节目信息参数-----------
    private boolean is_playback; //当前节目是否有回放
    private String playUrl; //当前节目地址

    public boolean isIs_playback() {
        return is_playback;
    }

    public void setIs_playback(boolean is_playback) {
        this.is_playback = is_playback;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public void setPictureList(ArrayList<PictureBean> pictureList) {
        if (pictureList == null) {
            return;
        }
        this.pictureList = pictureList;
        for (PictureBean bean  : pictureList) {
            if (PictureBean.TYPE_ICON.equals(bean.getType())) {
                this.img = bean.getUrl();
                return;
            }
        }
        if (pictureList != null && pictureList.size() > 0) {
            this.img = pictureList.get(0).getUrl();
        }
    }

    public ArrayList<PictureBean> getPictureList() {
        return pictureList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getFtype() {
        return ftype;
    }

    public void setFtype(String ftype) {
        this.ftype = ftype;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getProType() {
        return proType;
    }

    public void setProType(String proType) {
        this.proType = proType;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public ArrayList<CategoryBean> getCatagoryList() {
        return catagoryList;
    }

    public void setCatagoryList(ArrayList<CategoryBean> catagoryList) {
        this.catagoryList = catagoryList;
        if (catagoryList != null && !catagoryList.isEmpty()) {
            this.categoryId = catagoryList.get(0).getId();
            this.categoryName = catagoryList.get(0).getName();
        }
    }

    public ArrayList<LinksBean> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<LinksBean> links) {
        if (links == null) {
            return;
        }
        this.links = links;
        for (LinksBean bean  : links) {
            if (LinksBean.MEDIA_TYPE_PLAYBACK.equals(bean.getMediaType())) {
                this.type = "9";
                break;
            }
        }
    }

    public ArrayList<TagBean> getTagList() {
        return tagList;
    }

    public void setTagList(ArrayList<TagBean> tagList) {
        this.tagList = tagList;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "ChannelAIDLBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ftype='" + ftype + '\'' +
                ", description='" + description + '\'' +
                ", img='" + img + '\'' +
                ", proType='" + proType + '\'' +
                ", type='" + type + '\'' +
                ", playType='" + playType + '\'' +
                ", catagoryList=" + catagoryList +
                ", links=" + links +
                ", tagList=" + tagList +
                ", categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
