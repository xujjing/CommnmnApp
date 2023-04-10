package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huanglu on 2017/12/4.
 */

public class CatagoryListBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String columnType;
    private String id;
    private String name;
    private String unid;
    private String url;

    private List<CategoryBean> categoryList;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUnid() {
        return unid;
    }

    public void setUnid(String unid) {
        this.unid = unid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<CategoryBean> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryBean> categoryList) {
        this.categoryList = categoryList;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getId() {
        return id;///999
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

    @Override
    public String toString() {
        return "CatagoryListBean{" +
                "columnType='" + columnType + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
