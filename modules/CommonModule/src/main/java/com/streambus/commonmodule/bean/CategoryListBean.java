package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huanglu on 2017/5/22.
 */

public class CategoryListBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int columnType;
    private String detail;
    private int id;
    private String name;
    private String unid;
    private List<CategoryBean> categoryList;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnid() {
        return unid;
    }

    public void setUnid(String unid) {
        this.unid = unid;
    }

    public List<CategoryBean> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryBean> categoryList) {
        this.categoryList = categoryList;
    }

    @Override
    public String toString() {
        return "CategoryListBean{" +
                "columnType=" + columnType +
                ", detail='" + detail + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", unid='" + unid + '\'' +
                ", categoryList=" + categoryList +
                '}';
    }
}
