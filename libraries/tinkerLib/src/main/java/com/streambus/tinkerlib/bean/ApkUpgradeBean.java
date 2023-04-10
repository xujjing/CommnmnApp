package com.streambus.tinkerlib.bean;

import com.tencent.tinker.lib.util.TinkerLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by huanglu on 2017/5/24.
 */

public class ApkUpgradeBean implements Serializable {
    private static final String TAG = "ApkUpgradeBean";
    private static final long serialVersionUID = 1L;
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

    public ApkUpgradeBean(JSONObject jobject) throws JSONException {
        TinkerLog.d(TAG, "jObject " + jobject);
        if (jobject != null) {
            Iterator<String> iterator = jobject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                setData(key, jobject.get(key));
            }
        }
    }

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

    public String toJson() {
        try {
            JSONObject jobject = new JSONObject();
            jobject.put("status", status);
            jobject.put("name", name);
            jobject.put("url", url);
            jobject.put("img", img);
            jobject.put("appsize", appsize);
            jobject.put("updatecontent", updatecontent);
            jobject.put("icon", icon);
            jobject.put("description", description);
            return jobject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void setData(String key, Object value) {
        switch (key) {
            case "result":
                result = (int) value;
                break;
            case "message":
                message = "" + value;
                break;
            case "status":
                status = (int) value;
                break;
            case "name":
                name = "" + value;
                break;
            case "minsdk":
                minsdk = "" + value;
                break;
            case "url":
                url = "" + value;
                break;
            case "url2":
                url2 = "" + value;
                break;
            case "packagename":
                packagename = "" + value;
                break;
            case "img":
                img = "" + value;
                break;
            case "versionname":
                versionname = "" + value;
                break;
            case "md5":
                md5 = "" + value;
                break;
            case "versioncode":
                versioncode = "" + value;
                break;
            case "appsize":
                appsize = "" + value;
                break;
            case "updatecontent":
                updatecontent = "" + value;
                break;
            case "icon":
                icon = "" + value;
                break;
            case "description":
                description = "" + value;
                break;
            case "isForce":
                isForce = (boolean) value;
                break;
            default:
                break;
        }




    }
}
