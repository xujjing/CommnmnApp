package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;

public class ApkListBean implements Serializable {
    private List<ApkUpgradeInfo> applist;
    private long currentTime;
    private int result;

    public List<ApkUpgradeInfo> getApplist() {
        return applist;
    }

    public void setApplist(List<ApkUpgradeInfo> applist) {
        this.applist = applist;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
