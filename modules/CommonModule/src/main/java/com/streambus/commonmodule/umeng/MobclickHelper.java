package com.streambus.commonmodule.umeng;

import android.content.Context;

import com.streambus.basemodule.utils.SLog;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

public class MobclickHelper {
    public static final String TAG = MobclickHelper.class.getSimpleName();

    public static void onEventObject(Context mContext, String event, Map<String, Object> params) {
        SLog.i(TAG, "onEvent->event=" + event);
        MobclickAgent.onEventObject(mContext, event, params);
    }

    public static void onEvent(Context mContext, String event) {
        SLog.i(TAG, "onEvent->event=" + event);
        MobclickAgent.onEvent(mContext, event);
    }

    public static void onPageStart(String pageName) {
        SLog.i(TAG, "onPageStart->pageName=" + pageName);
        MobclickAgent.onPageStart(pageName);
    }

    public static void onPageEnd(String pageName) {
        SLog.i(TAG, "onPageEnd->pageName=" + pageName);
        MobclickAgent.onPageEnd(pageName);
    }
}
