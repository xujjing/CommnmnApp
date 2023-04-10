package com.streambus.commonmodule.bean;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class RoleListBean implements Serializable {
    protected long currentTime;
    protected int pageNum;
    protected int pageSize;
    protected int result;
    protected int total;

    protected List<RoleBean> data;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<RoleBean> getData() {
        return data;
    }

    public void setData(List<RoleBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RoleListBean{" +
                "currentTime=" + currentTime +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", result=" + result +
                ", total=" + total +
                ", data=" + data +
                '}';
    }
}
