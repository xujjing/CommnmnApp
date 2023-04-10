package com.streambus.tinkerlib.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.tencent.tinker.lib.util.TinkerLog;

import androidx.annotation.Nullable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/14
 * 描    述:
 * 修订历史：
 * ================================================
 */

public class RestartIntentService extends IntentService {

    private static long stopDelayed = 2000;
    private Handler mHandler;
    private String PackageName;

    private static final String TAG = "RestartIntentService";

    public RestartIntentService(){
        super("RestartIntentService");
        mHandler = new Handler();
    }

    public static void start(Context context,long Delayed) {
        Intent intent = new Intent(context, RestartIntentService.class);
        intent.putExtra("PackageName",context.getPackageName());
        intent.putExtra("Delayed",Delayed);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        stopDelayed = intent.getLongExtra("Delayed",2000);
        PackageName = intent.getStringExtra("PackageName");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent launchIntent = RestartIntentService.this.getPackageManager().getLaunchIntentForPackage(PackageName);
                RestartIntentService.this.startActivity(launchIntent);
                TinkerLog.d(TAG, "重启APP成功,包名：" + PackageName);
            }
        },stopDelayed);

    }

    /**
     * 重启APP
     * @param context˛
     */
    public static void restartAPP(Context context){
        start(context,1500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler!=null){
            mHandler = null;
        }
    }
}