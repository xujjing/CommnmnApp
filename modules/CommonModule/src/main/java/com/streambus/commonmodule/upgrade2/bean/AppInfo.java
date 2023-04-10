package com.streambus.commonmodule.upgrade2.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Author:Yoostar
 * Date:2021/6/7 17:40
 * Description:
 */
public class AppInfo implements Serializable {
    private int result;
    private String msg;
    private String logUrl;
    private String version;
    private String versionCode;
    private long size;
    private String md5;
    private String url;
    private String description;
    private ArrayList<Introduction> introductionList;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Introduction> getIntroductionList() {
        return introductionList;
    }

    public void setIntroductionList(ArrayList<Introduction> introductionList) {
        this.introductionList = introductionList;
    }
}
