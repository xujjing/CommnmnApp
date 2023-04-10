package com.streambus.tinkerlib.bean;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/1/19
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UploadInfo {
    private String mPackageName;
    private String mVersionCode;
    private String mPatchVersion;

    public UploadInfo(String packageName, String versionCode, String patchVersion) {
        mPackageName = packageName;
        mVersionCode = versionCode;
        mPatchVersion = patchVersion;
    }

    public void setPatchVersion(String patchVersion) {
        mPatchVersion = patchVersion;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(String versionCode) {
        mVersionCode = versionCode;
    }

    public String getPatchVersion() {
        return mPatchVersion;
    }
}
