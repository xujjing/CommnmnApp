package com.streambus.commonmodule.bean;


import java.io.Serializable;

/**
 * Created by huanglu on 2017/5/24.
 */

public class ApkUpgradeInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private long createTime;
    private long currentTime;
    private String name;
    private String packagename;
    private String versionname;
    private String versioncode;
    private String minsdk;
    private String url;
    private String url2;
    private String md5;
    private String appsize;
    private String updatecontent;
    private String icon;
    private String img;
    private String description;
    private int status;
    private int result;
    private boolean isForce;
    private String message;

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinsdk() {
        return minsdk;
    }

    public void setMinsdk(String minsdk) {
        this.minsdk = minsdk;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl2() {
        return url2;
    }

    public void setUrl2(String url2) {
        this.url2 = url2;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getVersionname() {
        return versionname;
    }

    public void setVersionname(String versionname) {
        this.versionname = versionname;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(String versioncode) {
        this.versioncode = versioncode;
    }

    public String getAppsize() {
        return appsize;
    }

    public void setAppsize(String appsize) {
        this.appsize = appsize;
    }

    public String getUpdatecontent() {
        return updatecontent;
    }

    public void setUpdatecontent(String updatecontent) {
        this.updatecontent = updatecontent;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public String toString() {
        return "ApkUpgradeBean{" +
                "name='" + name + '\'' +
                ", packagename='" + packagename + '\'' +
                ", versionname='" + versionname + '\'' +
                ", versioncode='" + versioncode + '\'' +
                ", minsdk='" + minsdk + '\'' +
                ", url='" + url + '\'' +
                ", url2='" + url2 + '\'' +
                ", md5='" + md5 + '\'' +
                ", appsize='" + appsize + '\'' +
                ", updatecontent='" + updatecontent + '\'' +
                ", icon='" + icon + '\'' +
                ", img='" + img + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", result=" + result +
                ", isForce=" + isForce +
                ", message='" + message + '\'' +
                '}';
    }


}
