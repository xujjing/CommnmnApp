package com.streambus.commonmodule.upgrade2.bean;


import java.io.Serializable;

/**
 * Created by huanglu on 2017/5/24.
 */

public class UpgradeBean2 implements Serializable {
    private static final String TAG = "ApkUpgradeBean";

    //    {
    //            "result": 1, // 0：终端为最新版本；1：有新版本；
    //            "msg": "xxx",
    //            "type": 0, // 0：版本更新；1：补丁更新
    //            "version": "4.11.14",
    //            "versionCode": 124,
    //            "size": 123412312,
    //            "md5": "xxxxxxxxxx",
    //            "url": "xxxxx",
    //            "description": "xxx",
    //            "upgradeType": 1 // 1: 强制升级；2：一般升级；3:静默升级；4：可忽略升级
    //    }

    private int result;
    private String msg;
    private int type;
    private String version;
    private int versionCode;
    private int size;
    private String md5;
    private String url;
    private String description;
    private int upgradeType;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
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

    public int getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(int upgradeType) {
        this.upgradeType = upgradeType;
    }






}
