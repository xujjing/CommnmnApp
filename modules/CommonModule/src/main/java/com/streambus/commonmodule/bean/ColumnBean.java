package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ColumnBean implements Serializable {

    //仅用于首页
    private long id;
    private String name;
    private int type;
    private String description;
    private String layout;
    private String result;

    private int total;

    private Map<String,List<String>> pictureList;
    private List<ChannelVodBean> channelList;
    private List<CategoryBean> categoryList;
    private List<ColumnBean> columnList;


    public boolean isEmpty() {
        return channelList == null && categoryList == null  && columnList == null;
    }

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

    public List<CategoryBean> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryBean> categoryList) {
        this.categoryList = categoryList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Map<String, List<String>> getPictureList() {
        return pictureList;
    }

    public void setPictureList(Map<String, List<String>> pictureList) {
        this.pictureList = pictureList;
    }

    public List<String> getIconList() {
        if (pictureList != null) {
            List<String> list = pictureList.get(PictureBean.TYPE_ICON);
            if (list != null && list.size() == 3) {
                return list;
            }
        }
        return null;
    }

    public String getHomeIcon() {
        if (pictureList != null) {
            List<String> list = pictureList.get(PictureBean.TYPE_ICON);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public String getBackground() {
        if (pictureList != null) {
            List<String> list = pictureList.get(PictureBean.TYPE_BACKGROUND);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "ColumnBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", pictureList=" + pictureList +
                ", channelList=" + channelList +
                ", columnList=" + columnList +
                ", categoryList=" + categoryList +
                '}';
    }
}
