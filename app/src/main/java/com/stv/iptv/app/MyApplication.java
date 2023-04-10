package com.stv.iptv.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.AppBuildConfig;
import com.streambus.commonmodule.CommonApplication;
import com.streambus.commonmodule.umeng.UMConstance;
import com.streambus.commonmodule.utils.ProcessUtil;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/8
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class MyApplication extends CommonApplication {

    private static final String TAG = "MyApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        String currentProcessName = ProcessUtil.getCurrentProcessName(getApplicationContext());
        SLog.i(TAG, "MyApplication_ing myPid=" + Process.myPid() + "  currentProcessName=" + currentProcessName);
        if (!getApplicationContext().getPackageName().equals(currentProcessName)) {
            return;
        }
        //TDO
    }

    @Override
    protected void initBuildConfig() {
        Log.d(TAG, String.format("BuildConfig BUILD_TYPE=%s  VERSION_NAME=%s VERSION_CODE=%s", BuildConfig.BUILD_TYPE, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        AppBuildConfig.APPLICATION_ID = BuildConfig.APPLICATION_ID;
        AppBuildConfig.VERSION_NAME = BuildConfig.VERSION_NAME;
        AppBuildConfig.VERSION_CODE = BuildConfig.VERSION_CODE;
        AppBuildConfig.IS_DEBUG = "debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE);
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            AppBuildConfig.setChannel(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format("BuildConfig  IS_DEBUG=%s  FLAVOR=%s  IS_LOG=%s", AppBuildConfig.IS_DEBUG, AppBuildConfig.FLAVOR, AppBuildConfig.IS_LOG));
    }
}
