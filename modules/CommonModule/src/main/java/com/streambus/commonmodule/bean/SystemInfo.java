package com.streambus.commonmodule.bean;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/7
 * 描    述:
 * 修订历史：
 * ================================================
 */

import android.content.Context;
import android.os.Build;

import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.utils.AppUtil;
import com.streambus.requestapi.SystemInfoUtils;

/**
 * 上报信息的基础数据，目前给用户反馈用，以后错误上报也可以继承这个类
 *
 * @author jcy
 */

public abstract class SystemInfo {

    /**
     * 机子型号名字
     */
    private String mModel = Build.MODEL;

    /**
     * CPU型号
     */
    private String mCPUInfo = Build.CPU_ABI;

    /**
     * 平台内存大小
     */
    private String mRamMemroy;

    /**
     * 平台虚拟机最大内存控制
     */
    private String vmHeapSize;

    /**
     * 平台分辨率
     */
    private String mScreenDisplay;

    /**
     * SDK发布的版本编号
     */
    private String mSDKReleaseVersion = Build.VERSION.RELEASE;

    /**
     * SDK标准的版本编号
     */
    private String mSDKVersion = Build.VERSION.SDK_INT + "";

    /**
     * APP包名
     */
    private String mAppPackageName;

    /**
     * APP版本
     */
    private String mAppVersionName;

    /**
     * 终端MAC地址
     */
    private String mMAC;

    /**
     * 终端使用的ID
     */
    private String mID = "";

    /**
     * 终端使用的CODE
     */
    private String mCODE = "";

    /**
     * 账号的有效期天数
     */
    private String mValidity = "";

    private Context mContext;

    public SystemInfo(Context context) {
        this.mContext = context;
        initData();
    }

    private void initData() {
        mAppPackageName = AppUtil.getAppPackageName(mContext);
        mAppVersionName = AppUtil.getAppVersionName(mContext);
        mRamMemroy = SystemInfoUtils.getTotalMemory(mContext);
        mScreenDisplay = SystemInfoUtils.getDisplaySize(mContext);
        vmHeapSize = SystemInfoUtils.getInternalToatalSpace(mContext);
        mMAC = SystemInfoUtils.getWiFiMacId(mContext);
        mID = Constants.VALUE_LOGIN_ACCOUNT_ID;
        mCODE = Constants.VALUE_LOGIN_ACCOUNT_NAME;
        mValidity = String.valueOf(MyAppLogin.getInstance().getValidityDay().getValue());
    }

    public String getModel() {
        return mModel;
    }

    public String getCPUInfo() {
        return mCPUInfo;
    }

    public String getRamMemroy() {
        return mRamMemroy;
    }

    public String getVmHeapSize() {
        return vmHeapSize;
    }

    public String getScreenDisplay() {
        return mScreenDisplay;
    }

    public String getSDKReleaseVersion() {
        return mSDKReleaseVersion;
    }

    public String getSDKVersion() {
        return mSDKVersion;
    }

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public String getAppVersionName() {
        return mAppVersionName;
    }

    public String getMAC() {
        return mMAC;
    }

    public String getID() {
        return mID;
    }

    public String getCODE() {
        return mCODE;
    }

    public String getValidity() {
        return mValidity;
    }

}
