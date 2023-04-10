package com.streambus.tinkerlib.update;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.streambus.tinkerlib.bean.ApkUpgradeBean;
import com.streambus.tinkerlib.bean.FixUpgradeBean;
import com.streambus.tinkerlib.update.view.ForceUpgradeDialog;
import com.streambus.tinkerlib.util.AppUtil;
import com.streambus.tinkerlib.util.TinkerManager;
import com.streambus.tinkerlib.util.Utils;
import com.tencent.tinker.lib.util.TinkerLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import okhttp3.OkHttpClient;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/9
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UpgradeService extends Service {

    public static final String ACTION_CHECK_UPGRADE = "ACTION_CHECK_UPGRADE";

    private static final String KEY_SAVE_VERSION = "KEY_SAVE_VERSION";
    private static final String KEY_SAVE_UPGRADE = "KEY_SAVE_UPGRADE";
    private static final String KEY_CURRENT_UPGRADE = "KEY_CURRENT_UPGRADE";
    private ApkUpgradeBean mCurrentApkUpgradeBean;

    /************************************* - 启动配置 - ****************************************/
    private static final class CurActivityLifecycle{
        private static Activity sCurrentActivity;
        private static int sAcState;
        private static final Application.ActivityLifecycleCallbacks INSTANCE = new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                sCurrentActivity = activity; sAcState = 1;}
            @Override public void onActivityStarted(Activity activity) {sAcState = 2;}
            @Override public void onActivityResumed(Activity activity) {sAcState = 3;}
            @Override public void onActivityPaused(Activity activity) {sAcState = 4;}
            @Override public void onActivityStopped(Activity activity) {sAcState = 5;}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {
                sCurrentActivity = null; sAcState = 0;}
        };

        private static final void checkCurActivityLifecycle(Application application, Application.ActivityLifecycleCallbacks callbacks) {
            if (sCurrentActivity != null) {
                if (sAcState < 4) {
                    callbacks.onActivityCreated(sCurrentActivity, null);
                    callbacks.onActivityStarted(sCurrentActivity);
                    callbacks.onActivityResumed(sCurrentActivity);
                } else {
                    if (sAcState >= 4) {
                        callbacks.onActivityPaused(sCurrentActivity);
                    }
                    if (sAcState >= 5) {
                        callbacks.onActivityStopped(sCurrentActivity);
                    }
                }
                sCurrentActivity = null;
            }
            application.unregisterActivityLifecycleCallbacks(INSTANCE);
            application.registerActivityLifecycleCallbacks(callbacks);
        }
    }

    private static Application sApplication;
    private static OkHttpClient sOkHttpClient;
    public static void start(Activity activity, OkHttpClient client) {
        sApplication = activity.getApplication();
        sOkHttpClient = client;
        sApplication.startService(new Intent(sApplication, UpgradeService.class));
        sApplication.registerActivityLifecycleCallbacks(CurActivityLifecycle.INSTANCE);
        CurActivityLifecycle.INSTANCE.onActivityCreated(activity, null);
    }

    public static void checkUpgrade() {
        Intent intent = new Intent(sApplication, UpgradeService.class);
        intent.setAction(ACTION_CHECK_UPGRADE);
        sApplication.startService(intent);
    }


/******************************************* ************ **********************************************************************/
    /************************************* - 服务执行 - ****************************************/
    private static final long CHECK_ERROR_TIME = 10 * 60 * 1000;
    private static final long CHECK_DEFFAUT_TIME = 30 * 60 * 1000;
    private Timer mTimer;
    private boolean mIsForeground;
    private boolean mWaitForeground;
    private UpgradeHelper mUpgradeHelper;
    private Handler mHandler = new Handler();
    private ApkUpgradeBean mApkUpgradeBean;
    private FixUpgradeBean mFixUpgradeBean;

    private static final String TAG = "UpgradeService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mUpgradeBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUpgradeBinder.register(null);
        return super.onUnbind(intent);
    }

    /******************************* - 升级Dialog绑定服务 - ****************************************/
    /************************************* - 监听下载状态 - ****************************************/
    private UpgradeBinder mUpgradeBinder = new UpgradeBinder();
    public interface IUpgradeBind{
        void register(IUpgradeListener listener);
    }
    private class UpgradeBinder extends Binder implements IUpgradeBind, IUpgradeListener {
        private IUpgradeListener listener;
        private Map lastAction;
        @Override
        public void register(IUpgradeListener listener) {
            this.listener = listener;
            if (lastAction != null) {
                int key = (int) lastAction.get("key");
                switch (key) {
                    case 1:
                        onProgress((long)lastAction.get("progress"), (long)lastAction.get("total"), (String) lastAction.get("tcpSpeed"));
                        break;
                    case 2:
                        onSuccess((File) lastAction.get("file"));
                        break;
                    case 3:
                        onFailed();
                        break;
                }
            }
        }
        @Override
        public void onProgress(long progress, long total, String tcpSpeed) {
            if (listener != null) {
                listener.onProgress(progress, total, tcpSpeed);
            } else {
                lastAction = new HashMap();
                lastAction.put("key", 1);
                lastAction.put("progress", progress);
                lastAction.put("total", total);
                lastAction.put("tcpSpeed", tcpSpeed);
            }
        }
        @Override
        public void onSuccess(File file) {
            if (listener != null) {
                listener.onSuccess(file);
            }
            lastAction = new HashMap();
            lastAction.put("key", 2);
            lastAction.put("file", file);
        }
        @Override
        public void onFailed() {
            if (listener != null) {
                listener.onFailed();
            }
            lastAction = new HashMap();
            lastAction.put("key", 3);
        }
    }
    /*----------------------------------- END --------------------------------------------------*/



    /*******************************************************************************************/
    /************************************* - 启动更新服务 - ****************************************/
    /*******************************************************************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        if (CurActivityLifecycle.sCurrentActivity == null) {
            stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }
        CurActivityLifecycle.checkCurActivityLifecycle(getApplication(), mActivityLifecycleCallbacks);
        init();
    }

    private void init() {
        try {
            int saveVerson = Utils.get(this, KEY_SAVE_VERSION, 0);
            if (Integer.parseInt(AppUtil.getVersionCode(this)) == saveVerson) {
                Utils.put(this, KEY_CURRENT_UPGRADE, Utils.get(this, KEY_SAVE_UPGRADE, ""));
            }
            String curGrade = Utils.get(this, KEY_CURRENT_UPGRADE, "");
            JSONObject jobject = null;
            if (!TextUtils.isEmpty(curGrade)) {
                jobject = new JSONObject(curGrade);
            }
            mCurrentApkUpgradeBean = new ApkUpgradeBean(jobject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mUpgradeHelper = new UpgradeHelper(getApplicationContext(), sOkHttpClient);
        mTimer = new Timer();
        timerCheckup(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (ACTION_CHECK_UPGRADE.equals(intent.getAction())) {
            checkState();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkState() {
        if (mApkUpgradeBean != null) {
            showForceUpgradeDialog(mApkUpgradeBean);
        } else if (mFixUpgradeBean != null) {
            showForceUpgradeDialog(mFixUpgradeBean);
        } else {
            showForceUpgradeDialog(mCurrentApkUpgradeBean);
            if (mDisposable == null) {
                mTimer.cancel();
                mTimer = new Timer();
                timerCheckup(0);
            }
        }
    }
    
    /************************************* - 检测处理更新 - ****************************************/
    private void timerCheckup(long delay) {
        mTimer.schedule(new CheckUpdateTask(), delay);
    }

    Disposable mDisposable;
    private class CheckUpdateTask extends TimerTask {

        private volatile boolean isQueryApk;
        private volatile boolean isQueryFix;
        private ApkUpgradeBean apkUpgradeBean;
        private FixUpgradeBean fixUpgradeBean;
        @Override
        public void run() {
            TinkerLog.d(TAG, "CheckUpdateTask start run");
            mDisposable = mUpgradeHelper.checkUpdate(new UpgradeHelper.ICheckupCallBack() {
                @Override
                public void apkUpgrade(ApkUpgradeBean upgradeBean) {
                    TinkerLog.d(TAG, "apkUpgrade info=>" + upgradeBean);
                    apkUpgradeBean = upgradeBean;
                    if (upgradeBean != null && upgradeBean.isForce()) {
                        mDisposable.dispose();//取消可能还没完成的补丁请求,直接执行apk升级
                        mDisposable = null;
                        handleApkUpgrade(upgradeBean);
                        return;
                    }
                    isQueryApk = true;
                    handleUpgrade();
                }

                @Override
                public void fixUpgrade(FixUpgradeBean upgradeBean) {
                    TinkerLog.d(TAG, "fixUpgrade info=>" + upgradeBean);
                    fixUpgradeBean = upgradeBean;
                    isQueryFix = true;
                    handleUpgrade();
                }
            });
        }

        private void handleUpgrade() {
            if (!(isQueryApk && isQueryFix)) {
                return;//还有请求没有完成，等两个请求都完成了再往下执行
            }
            if (fixUpgradeBean != null && fixUpgradeBean.isForce()) {
                handleFixUpgrade(fixUpgradeBean);//强制补丁修复
            } else if (apkUpgradeBean != null && apkUpgradeBean.getStatus() == 1) {
                //有新的apk要更新
                handleApkUpgrade(apkUpgradeBean);
            }else if (fixUpgradeBean != null && fixUpgradeBean.getResult() == 1) {
                //有新的Fix要跟新
                handleFixUpgrade(fixUpgradeBean);
            }
            if (apkUpgradeBean == null || fixUpgradeBean == null) {
                TinkerLog.d(TAG, "存在错误，10分钟后再次检查更新，用于重试更新操作");
                timerCheckup(CHECK_ERROR_TIME);//存在错误，5分钟后再次检查更新，用于重试更新操作
            } else {
                TinkerLog.d(TAG, "常规30分钟循环检查更新，检测是否有更新的版本");
                timerCheckup(CHECK_DEFFAUT_TIME);//常规30分钟循环检查更新，检测是否有更新的版本
            }
            mDisposable = null;
        }
    }

    
    /************************************* - 升级修复 - ****************************************/
    /**
     * 进行apk升级
     */
    private void handleApkUpgrade(final ApkUpgradeBean upgradeBean) {
        TinkerLog.d(TAG, "handleApkUpgrade upgradeBean=>" + upgradeBean);
        mApkUpgradeBean = upgradeBean;
        if (isUpgradeDialogShow || upgradeBean.isForce()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showForceUpgradeDialog(upgradeBean);
                }
            });
        }
        File file = mUpgradeHelper.downLoadAPk(upgradeBean, mUpgradeBinder);
        if (!isUpgradeDialogShow && file != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showForceUpgradeDialog(upgradeBean);
                }
            });
        }
    }


    /**
     * 进行补丁修复
     */
    private void handleFixUpgrade(final FixUpgradeBean upgradeBean) {
        TinkerLog.d(TAG, "handleFixUpgrade upgradeBean=>" + upgradeBean);
        mFixUpgradeBean = upgradeBean;
        if (isUpgradeDialogShow || upgradeBean.isForce()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showForceUpgradeDialog(upgradeBean);
                }
            });
        }
        final File file = mUpgradeHelper.downLoadFix(upgradeBean, mUpgradeBinder);
        if (file != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    TinkerManager.IS_FORCE_RESTART = upgradeBean.isForce();
                    TinkerManager.onReceiveUpgradePatch(file.getAbsolutePath(), upgradeBean.getPatchVersion());
                    mFixUpgradeBean = null;
                }
            });
        }
    }


    /************************************* - 显示提示框 - ****************************************/

    private void showForceUpgradeDialog(ApkUpgradeBean upgradeBean) {
        if (!mIsForeground) {
            mWaitForeground = true;
            return;
        }
        TinkerLog.d(TAG, "showForceUpgradeDialog ApkUpgradeBean");
        ForceUpgradeDialog.launch(getApplication(), upgradeBean, upgradeBean == mCurrentApkUpgradeBean);
    }

    private void showForceUpgradeDialog(FixUpgradeBean upgradeBean) {
        if (!mIsForeground) {
            mWaitForeground = true;
            return;
        }
        TinkerLog.d(TAG, "showForceUpgradeDialog FixUpgradeBean");
        ForceUpgradeDialog.launch(getApplication(), upgradeBean);
    }


    private void showPointView() {
        if (mApkUpgradeBean != null) {
            showForceUpgradeDialog(mApkUpgradeBean);
        } else if (mFixUpgradeBean != null) {
            showForceUpgradeDialog(mFixUpgradeBean);
        }
    }

    private boolean isUpgradeDialogShow;
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        private int count;
        @Override
        public void onActivityStarted(Activity activity) {
            ++count; mIsForeground = true;
            TinkerLog.d(TAG, "onActivityStarted  activity=>" + activity.getClass() + "  mWaitForeground=" + mWaitForeground);
            if (mWaitForeground) {
                mWaitForeground = false;
                showPointView();
            }
        }
        @Override
        public void onActivityStopped(Activity activity) {mIsForeground = --count > 0;}
        @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            TinkerLog.d(TAG, "onActivityCreated  activity=>" + activity.getClass());
            if (activity instanceof ForceUpgradeDialog) {
                isUpgradeDialogShow = true;
            } else if (isUpgradeDialogShow) {
                boolean b = activity.moveTaskToBack(false);
                TinkerLog.d(TAG, "onActivityCreated moveTaskToBack activity=>" + activity.getClass() + " b=" + b);
                if (!b) {
                    showPointView();
                }
            }
        }
        @Override public void onActivityResumed(Activity activity) {}
        @Override public void onActivityPaused(Activity activity) {}
        @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        @Override public void onActivityDestroyed(Activity activity) {
            if (activity instanceof ForceUpgradeDialog) {
                isUpgradeDialogShow = false;
            }
        }
    };

}
