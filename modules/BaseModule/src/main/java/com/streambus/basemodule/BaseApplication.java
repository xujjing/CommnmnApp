package com.streambus.basemodule;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/8
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class BaseApplication extends Application {
    private static BaseApplication INSTANCE;

    public static BaseApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}