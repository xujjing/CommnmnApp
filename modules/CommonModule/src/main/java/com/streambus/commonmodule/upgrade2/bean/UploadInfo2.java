package com.streambus.commonmodule.upgrade2.bean;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.streambus.commonmodule.AppBuildConfig;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.utils.AppUtil;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/1/19
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UploadInfo2 implements Serializable {
    private String accountId;
    private String token;
    private String channelCode; // 渠道码，可为空，终端新版本软件内置

    private String pkg;
    private String version;
    private int versionCode;
    private String patchVersion;
    private String patchVersionCode;

    private String model; // "mi6", // 设备型号
    private String brand; // "xiaomi", // 设备品牌
    private String androidId;

    public UploadInfo2(Context context) {
        pkg = context.getPackageName();
        version = AppUtil.getAppVersionName(context);
        versionCode = AppUtil.getVersionCode(context);
        channelCode = AppBuildConfig.FLAVOR;

        model = Build.MODEL;
        brand = Build.BRAND;
        androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); // android ID
    }

    // 更新用户信息，在每次json格式化之前调用
    public UploadInfo2 updateAccountInfo() {
        accountId = Constants.VALUE_LOGIN_ACCOUNT_ID;
        token = Constants.VALUE_LOGIN_TOKEN;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
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

    public String getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(String patchVersion) {
        this.patchVersion = patchVersion;
    }

    public String getPatchVersionCode() {
        return patchVersionCode;
    }

    public void setPatchVersionCode(String patchVersionCode) {
        this.patchVersionCode = patchVersionCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }
}
