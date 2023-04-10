package com.streambus.commonmodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.streambus.basemodule.BaseApplication;
import com.streambus.basemodule.networkmonitoring.manager.NetworkManager;
import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SecurePreferences;
import com.streambus.commonmodule.advert.AdvertManager;
import com.streambus.commonmodule.api.DaoHelper;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.dialog.RemindManager;
import com.streambus.commonmodule.download.VideoDownloadManager;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.logs.CrashHandler;
import com.streambus.commonmodule.logs.LogImp;
import com.streambus.commonmodule.umeng.UMengManager;
import com.streambus.commonmodule.upgrade2.UpgradeManager;
import com.streambus.commonmodule.utils.LifecycleUtils;
import com.streambus.commonmodule.utils.ProcessUtil;
import com.streambus.requestapi.OkHttpHelper;
import com.streambus.requestapi.RALog;
import com.yoostar.fileloggingutil.FileTreeIo;

import java.io.File;

import androidx.multidex.MultiDex;
import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;
import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.Pragma;
import tv.danmaku.ijk.media.widget.media.IjkVideoView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/8
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class CommonApplication extends BaseApplication {
    private static final String TAG = "CommonApplication";
    private static final String ACTION_ATVApplicationStart = "com.streambus.commonmodule.CommonApplication";
    private static boolean AFTER_DONE = false;
    private static final Object AClock = new Object();
    private static final boolean IS_TEST_SERVICE = false;

    @Override
    public void onCreate() {
        super.onCreate();
        String currentProcessName = ProcessUtil.getCurrentProcessName(getApplicationContext());
        SLog.i(TAG, "CommonApplication_ing myPid=" + Process.myPid() + "  currentProcessName=" + currentProcessName);
        if (!getApplicationContext().getPackageName().equals(currentProcessName)) {
            return;
        }
        initBuildConfig();
        sendBroadcast(new Intent(ACTION_ATVApplicationStart).putExtra("myPid", Process.myPid()));
        registerReceiver(mHomeWatcherReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        registerReceiver(mATVApplicationStartReceiver, new IntentFilter(ACTION_ATVApplicationStart));
        registerActivityLifecycleCallbacks(LifecycleUtils.register());
        AFTER_DONE = false;
        _initConfig();
        _initConfigRunOther();
    }

    protected abstract void initBuildConfig();

    private void _initConfig() {
        PreferencesUtils.init(getApplicationContext());
        SecurePreferences.init(getApplicationContext());
        Log.i(TAG, "AppBuildConfig.IS_DEBUG=" + AppBuildConfig.IS_DEBUG);
        Timber.plant(new Timber.Tree() {
            private final FileTreeIo fileTree = FileTreeIo.setup(new File(getExternalCacheDir(), Constants.LOG_FILE_DIRECTORY).getAbsolutePath(), AppBuildConfig.IS_DEBUG);
            @Override protected void log(int priority, String tag, String message, Throwable t) {
                if (message != null && message.length() > 4096) {
                    message = message.substring(0, 4096) + "...Too much!!!";
                }
                fileTree.log(priority, tag, message, t);
            }
        });
        if (AppBuildConfig.IS_DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
        CrashHandler.setup(getApplicationContext());

        SLog.setSLogImp(LogImp.getInstance());
        RALog.setReqApiLogImp(LogImp.getInstance());
        DebugLog.setDebugImp(LogImp.getInstance());
        if (AppBuildConfig.IS_LOG) {
            //日志渠道打开日志
            Pragma.IJK_NATIVE_DEBUG = true;
            Pragma.IJK_LOG_FILE = true;
            RALog.setNativeLog(false, 0);
        }
//        Pragma.IJK_NATIVE_DEBUG = true;
//        Pragma.IJK_LOG_FILE = true;

        NetworkManager.getDefault().init(this);

        RemindManager.setup(this);
        AdvertManager.setup(this);
        UpgradeManager.setup(this);
        VideoDownloadManager.setup(this);

    }

    private void _initConfigRunOther() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (AClock) {
                    OkHttpHelper.init(getApplicationContext());
                    UMengManager.preInit(getApplicationContext());
                    Observable.bufferSize();
                    RxJavaPlugins.setErrorHandler(throwable -> SLog.w("RxJavaPlugins", "Throwable", throwable));
                }
            }
        }).start();
    }

    public void initAfterPermissions(Context context) {
        synchronized (AClock) {
            SecurePreferences.initAfter(getApplicationContext());
            UMengManager.init(getBaseContext());
            DaoHelper.initDataBse(getApplicationContext());
            MyAppLogin.init(getApplicationContext(), IS_TEST_SERVICE);
            RequestApi.setup(getApplicationContext(), IS_TEST_SERVICE);
            MyAppLogin.getInstance().autoLogin();
            AdvertManager.getInstance().updateSplashAdvert();
            UpgradeManager.getInstance().checkUpgrade(false);
            AFTER_DONE = true;
            initConfigRunUiThread(context);
        }
    }

    private void initConfigRunUiThread(Context context) {
        Glide.with(context);
        new IjkVideoView(context);
    }
    public static boolean isAFTER_DONE() {
        return AFTER_DONE;
    }


    /**
     * HOME 监听
     */
    private BroadcastReceiver mHomeWatcherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SLog.i(TAG, "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    // 短按Home键
                    SLog.i(TAG, "HomeWatcherReceiver_homekey");
                    killApk();
                }
            }
        }
    };


    /**
     * ATV产品启动时，如果报名不是本应用，则接收本应用。起到同一时间，只能存在一个ATV应用
     */
    private BroadcastReceiver mATVApplicationStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SLog.i(TAG, "onReceive: action: " + action);
            if (action.equals(ACTION_ATVApplicationStart)) {
                SLog.i(TAG, "onReceive pid=" + intent.getIntExtra("myPid", 0) + "  myPid=" + Process.myPid());
                if (intent.getIntExtra("myPid", 0) != Process.myPid()) {
                    killApk();
                }
            }
        }
    };

    public void killApk(){
        // 顺序不要调整，有需要增减修改 联系xujianjing
        unregisterReceiver(mHomeWatcherReceiver);
        if (MyAppLogin.getInstance()!= null) MyAppLogin.getInstance().logout();
        LifecycleUtils.finishAll();
        UMengManager.exit(getApplicationContext());
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
