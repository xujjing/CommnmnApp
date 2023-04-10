package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class VodTagBean implements Serializable {
    private String homeType;
    private String columnType;
    private String columnName;

    public VodTagBean() {
    }

    public VodTagBean(String homeType, String columnType, String columnName) {
        this.homeType = homeType;
        this.columnType = columnType;
        this.columnName = columnName;
    }

    public String getHomeType() {
        return homeType;
    }

    public void setHomeType(String homeType) {
        this.homeType = homeType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "VodTagBean{" +
                "homeType='" + homeType + '\'' +
                ", columnType='" + columnType + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
