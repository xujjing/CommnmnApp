package com.streambus.tinkerlib.bean;

import com.tencent.tinker.lib.util.TinkerLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/1/12
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FixUpgradeBean implements Serializable {

    /**
     * result : 0
     * message :
     * patchVersion : 2
     * url : http://mavistv.com/file/123234345.apk
     * md5 : a1526a086a9933bea4819370e858dc73
     * size : 132456
     */
    private static final long serialVersionUID = 1;
    public static final String TAG ="CheckFixBean";
    private int result;
    private String message;
    private String patchVersion;
    private String url;
    private String md5;
    private String size;
    private boolean isForce;

    public FixUpgradeBean(JSONObject jsonObject) throws JSONException {
        Iterator<String> interator = jsonObject.keys();
        while (interator.hasNext()) {
            String key = interator.next();
            setData(key, jsonObject.get(key));
        }
    }

    public void setData(String key, Object value) {
        TinkerLog.d(TAG, "key:  " + key + " value:" + value);
        switch (key) {
            case "result":
                result = (int) value;
                break;
            case "message":
                message = "" + value;
                break;
            case "patchVersion":
                patchVersion = "" + value;
                break;
            case "url":
                url = "" + value;
                break;
            case "md5":
                md5 = "" + value;
                break;
            case "size":
                size = "" + value;
                break;
            case "isForce":
                isForce = (boolean) value;
                break;
            default:
                break;
        }
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
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

    public String getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(String patchVersion) {
        this.patchVersion = patchVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FixUpgradeBean{" +
                "result=" + result +
                ", message='" + message + '\'' +
                ", patchVersion='" + patchVersion + '\'' +
                ", url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", size='" + size + '\'' +
                ", isForce=" + isForce +
                '}';
    }
}
