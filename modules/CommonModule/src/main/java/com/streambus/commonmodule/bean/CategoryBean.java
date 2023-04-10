package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by huanglu on 2017/5/22.
 */

public class CategoryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String url;
    private String detail;
    private String columnType;//判断成人栏目类型
    private String bigImg;
    private int total;

    private List<PictureBean> picturesList;

    //仅用于首页
    private Map<String,List<String>> pictureList;


    private List<ChannelVodBean> channelList;
    private List<CategoryBean> categoryList;

    public static final String ADULT_CATEGORY_TYPE = "2";

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

    public String getColumnType() {
        return columnType;
    }
    public List<ChannelVodBean> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<ChannelVodBean> channelList) {
        this.channelList = channelList;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getUrl() {
        return url;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<PictureBean> getPicturesList() {
        return picturesList;
    }

    public void setPicturesList(List<PictureBean> picturesList) {
        this.picturesList = picturesList;
    }

    public List<CategoryBean> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryBean> categoryList) {
        this.categoryList = categoryList;
    }

    public String getBigImg() {
        return bigImg;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setBigImg(String bigImg) {
        this.bigImg = bigImg;
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
        String iconFile = "";
        if (null != picturesList) {
            for (PictureBean pictureBean : picturesList) {
                if (pictureBean.getType().equals(PictureBean.TYPE_BACKGROUND)) {
                    iconFile = pictureBean.getUrl();
                    break;
                }
            }
        }
        return iconFile;
    }

    public String getDefBackground() {

        if (pictureList != null) {
            List<String> list = pictureList.get(PictureBean.TYPE_BACKGROUND);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return "";
    }

    public Map<String, List<String>> getPictureList() {
        return pictureList;
    }

    public void setPictureList(Map<String, List<String>> pictureList) {
        this.pictureList = pictureList;
    }

    @Override
    public String toString() {
        return "CategoryBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", detail='" + detail + '\'' +
                ", columnType='" + columnType + '\'' +
                ", picturesList=" + picturesList +
                ", channelList=" + channelList +
                '}';
    }
}
