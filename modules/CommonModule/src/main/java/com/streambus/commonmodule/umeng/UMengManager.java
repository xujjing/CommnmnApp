package com.streambus.commonmodule.umeng;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.streambus.commonmodule.AppBuildConfig;
import com.streambus.commonmodule.Constants;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.internal.crash.UMCrashManager;

public class UMengManager {
    public static final String TAG = UMengManager.class.getSimpleName();

    //非Application中调用
    public static void init(Context mContext) {
        Log.i(TAG, "init++");
        UMConfigure.setLogEnabled(AppBuildConfig.IS_DEBUG);
        try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
            // 参数一：当前上下文context；
            // 参数二：应用申请的Appkey；
            // 参数三：渠道名称；
            // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
            // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息
            UMConfigure.init(mContext, appInfo.metaData.getString(UMConstance.UMENG_APPKEY), appInfo.metaData.getString(UMConstance.UMENG_CHANNEL), UMConfigure.DEVICE_TYPE_BOX, appInfo.metaData.getString(UMConstance.MESSAGE_SECRET));
            // 支持在子进程中统计自定义事件
            UMConfigure.setProcessEvent(true);
            PushHelper.getInstance().onAppStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        MobclickAgent.onProfileSignIn(Constants.VALUE_LOGIN_ACCOUNT_NAME);
    }

    public static void onProfileSignIn(String useId) {
        MobclickAgent.onProfileSignIn(useId);
    }

    public static void onProfileSignOff() {
        MobclickAgent.onProfileSignOff();
    }

    //Application中调用
    public static void preInit(Context mContext) {
        //umeng延时启动,有利于app冷启动
        try {
            Log.i(TAG, "preInit++");
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            UMConfigure.preInit(mContext, appInfo.metaData.getString(UMConstance.UMENG_APPKEY), appInfo.metaData.getString(UMConstance.UMENG_CHANNEL));
            PushHelper.getInstance().preInit(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleException(Context mContext, Throwable ex) {
        Log.i(TAG, "handleException++");
        UMCrashManager.reportCrash(mContext, ex);
        //UMCrash.generateCustomLog(ex, "CrashHandler");//上传umeng
    }

    public static void exit(Context mContext) {
        Log.i(TAG, "exit++");
        MobclickAgent.onKillProcess(mContext);
        PushHelper.getInstance().release();
    }
}
