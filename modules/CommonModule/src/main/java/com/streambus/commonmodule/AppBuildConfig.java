package com.streambus.commonmodule;

import android.content.pm.ApplicationInfo;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/7/9
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class AppBuildConfig {

    private static final String KEY_CHANNEL = "UMENG_CHANNEL";
    private static final String KEY_LOG_TAG = "_log";

    public static String APPLICATION_ID;
    public static String VERSION_NAME;
    public static int VERSION_CODE;
    public static String FLAVOR;

    public static boolean IS_DEBUG;
    public static boolean IS_LOG;

    public static void setChannel(ApplicationInfo appInfo) {
        String channel = appInfo.metaData.getString(AppBuildConfig.KEY_CHANNEL);
        if (channel != null && channel.endsWith(AppBuildConfig.KEY_LOG_TAG)) {
            AppBuildConfig.IS_LOG = true;
            channel = channel.substring(0, channel.length() - AppBuildConfig.KEY_LOG_TAG.length());
        }
        AppBuildConfig.FLAVOR = channel;
    }
}
