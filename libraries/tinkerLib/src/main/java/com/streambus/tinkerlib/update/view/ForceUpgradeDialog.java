package com.streambus.tinkerlib.update.view;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.streambus.tinkerlib.R;
import com.streambus.tinkerlib.bean.ApkUpgradeBean;
import com.streambus.tinkerlib.bean.FixUpgradeBean;
import com.streambus.tinkerlib.update.IUpgradeListener;
import com.streambus.tinkerlib.update.UpgradeService;
import com.streambus.tinkerlib.util.AppUtil;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.File;
import java.text.DecimalFormat;

import androidx.annotation.Nullable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/10
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ForceUpgradeDialog extends Activity implements IUpgradeListener {

    private static final String KEY_UPGRADE_BEAN = "KEY_UPGRADE_BEAN";
    private static final String KEY_UPGRADE_TYPE = "KEY_UPGRADE_TYPE";
    private static final int TYPE_APK_NEWEST = 0;
    private static final int TYPE_APK_UPGRADE = 1;
    private static final int TYPE_FIX_UPGRADE = 2;
    private View mBtCancel;
    private View mBtInstall;
    private boolean mIsFouce;


    public static void launch(Application context, ApkUpgradeBean upgradeBean, boolean isNewest) {
        Intent intent = new Intent(context, ForceUpgradeDialog.class);
        int typeApk = isNewest ? TYPE_APK_NEWEST : TYPE_APK_UPGRADE;
        intent.putExtra(KEY_UPGRADE_TYPE, typeApk);
        intent.putExtra(KEY_UPGRADE_BEAN, upgradeBean);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void launch(Application context, FixUpgradeBean upgradeBean) {
        Intent intent = new Intent(context, ForceUpgradeDialog.class);
        intent.putExtra(KEY_UPGRADE_TYPE, TYPE_FIX_UPGRADE);
        intent.putExtra(KEY_UPGRADE_BEAN, upgradeBean);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private UpgradeService.IUpgradeBind mIUpgradeBind;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIUpgradeBind = (UpgradeService.IUpgradeBind) service;
            mIUpgradeBind.register(ForceUpgradeDialog.this);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /************************************* - Activity的显示逻辑 - ****************************************/
    private static final String TAG = "ForceUpgradeDialog";
    ProgressBar mProgressBar;
    TextView mTvProgress;
    TextView mTvNetSpeed;
    TextView mTvTitle;
    TextView mTvContent;
    TextView mTvMsg;
    private int mType;
    private boolean mIsWaitInstallApk;
    private File mApkFile;
    private static final String TITLE_FIX_UPGRADE = "Found the latest version, need to upgrade";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowFull();
        super.onCreate(savedInstanceState);
        TinkerLog.d(TAG, "ForceUpgradeDialog onCreate bindService");
        setContentView(R.layout.dialog_force_upgrade);
        bindService(new Intent(this, UpgradeService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        initView();
        updateIntent(getIntent());
    }

    private void setWindowFull() {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                |View.SYSTEM_UI_FLAG_FULLSCREEN//全屏
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;//隐藏导航栏
        if (Build.VERSION.SDK_INT>=19){
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }else{
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    private void updateIntent(Intent intent) {
        mType = intent.getIntExtra(KEY_UPGRADE_TYPE, 0);
        TinkerLog.d(TAG,"updateIntent mType=" + mType + " KEY_UPGRADE_BEAN=>" + intent.getSerializableExtra(KEY_UPGRADE_BEAN));
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        if (mType == TYPE_APK_NEWEST) {
            findViewById(R.id.progress_layout).setVisibility(View.GONE);
            setApkNewestView((ApkUpgradeBean) intent.getSerializableExtra(KEY_UPGRADE_BEAN));
        }else if (mType == TYPE_APK_UPGRADE) {
            setApkUpdateView((ApkUpgradeBean) intent.getSerializableExtra(KEY_UPGRADE_BEAN));
        } else if (mType == TYPE_FIX_UPGRADE) {
            setFixUpdateView((FixUpgradeBean) intent.getSerializableExtra(KEY_UPGRADE_BEAN));
        } else {
            Toast.makeText(getApplicationContext(), "ForceUpgradeDialog error keyType=" + mType, Toast.LENGTH_SHORT).show();
        }
    }


    private void initView() {
        mTvTitle = findViewById(R.id.tv_title);
        mTvContent = findViewById(R.id.tv_conent);
        mProgressBar = findViewById(R.id.sb_progress);
        mTvProgress = findViewById(R.id.tv_progress);
        mTvNetSpeed = findViewById(R.id.tv_net_speed);
        mTvMsg = findViewById(R.id.tv_msg);
        mBtCancel = findViewById(R.id.bt_cancel);
        mBtInstall = findViewById(R.id.bt_install);
    }

    private void setApkNewestView(ApkUpgradeBean upgradeBean) {
        mTvTitle.setText(R.string.tinker_title_apk_newest);
        if (upgradeBean != null) {
            mTvContent.setText(upgradeBean.getDescription());
        } else {
            mTvContent.setText("");
        }
    }

    private void setApkUpdateView(ApkUpgradeBean upgradeBean) {
        mIsFouce = upgradeBean.isForce();
        mTvTitle.setText(R.string.tinker_title_apk_upgrade);
        mTvContent.setText(upgradeBean.getDescription());
    }

    private void setFixUpdateView(FixUpgradeBean upgradeBean) {
        mIsFouce = upgradeBean.isForce();
        mTvTitle.setText(R.string.tinker_title_apk_upgrade);
        mTvContent.setText(upgradeBean.getMessage());
    }

    @Override
    public void onProgress(long progress, long total, String tcpSpeed) {
        TinkerLog.d(TAG, "onProgress progress=" + progress + "  total=" + total + "  tcpSpeed=" + tcpSpeed);
        mProgressBar.setProgress((int) (progress * 100 / total));
        mTvProgress.setText(formatFileSize(progress, total));
        mTvNetSpeed.setText(tcpSpeed);
    }

    public String formatFileSize(long progress, long total) {
        DecimalFormat df = new DecimalFormat("0.0");
        String fileSizeString;
        String progressString;
        String wrongSize = "0B/0B";
        if (total == 0) {
            return wrongSize;
        }
        if (total < 1024) {
            fileSizeString = df.format((float) total) + "B";
            progressString = df.format((float) progress) + "B";
        } else if (total < 1048576) {
            fileSizeString = df.format((float) total / 1024) + "KB";
            progressString = df.format((float) progress / 1024) + "KB";
        } else if (total < 1073741824) {
            fileSizeString = df.format((float) total / 1048576) + "MB";
            progressString = df.format((float) progress / 1048576) + "MB";
        } else {
            fileSizeString = df.format((float) total / 1073741824) + "GB";
            progressString = df.format((float) progress / 1073741824) + "GB";
        }
        return progressString + "/" + fileSizeString;
    }


    @Override
    public void onSuccess(File file) {
        TinkerLog.d(TAG, "onSuccess ======File=" + file);
        TinkerLog.d(TAG, "mType=" + mType  +"   mIsFouce=" + mIsFouce);
        mTvNetSpeed.setText("0KB/S");
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
        if (mType == TYPE_APK_UPGRADE) {
            mApkFile = file;
            if (!mIsFouce) {
                mBtCancel.setVisibility(View.VISIBLE);
                mBtInstall.setVisibility(View.VISIBLE);
                mBtInstall.requestFocus();
                mBtCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ForceUpgradeDialog.super.finish();
                    }
                });
                mBtInstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppUtil.showApkInstallPage(ForceUpgradeDialog.this, mApkFile);
                    }
                });
            } else {
                installApk();
            }
        } else if (mType == TYPE_FIX_UPGRADE) {
            patchFix(file);
        }
    }

    private void installApk() {
        Toast.makeText(this, R.string.tinker_install_to_upgrade, Toast.LENGTH_LONG).show();
        mIsWaitInstallApk = true;
        mTvMsg.setText(R.string.tinker_install_and_reopen);
        mTvMsg.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppUtil.showApkInstallPage(ForceUpgradeDialog.this, mApkFile);
            }
        }, 1000);
    }

    private void patchFix(final File file) {
        if (!mIsFouce) {
            mTvMsg.setText(R.string.tinker_patch_effect_next_time);
            mTvMsg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ForceUpgradeDialog.super.finish();
                }
            }, 5000);
        } else {
            mTvMsg.setText(R.string.tinker_patch_wait_for_restart);
        }
    }

    @Override
    public void onFailed() {
        mTvNetSpeed.setText("0KB/S");
        Toast.makeText(this, R.string.tinker_download_failed, Toast.LENGTH_LONG).show();
        super.finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mIsStop = false;
        if (mIsWaitInstallApk) {
            Toast.makeText(this, R.string.tinker_install_before_using, Toast.LENGTH_SHORT).show();
            AppUtil.showApkInstallPage(this, mApkFile);
        }
    }

    @Override
    public void finish() {
        if (!mIsFouce) {
            super.finish();
        } else {
            Toast.makeText(this, R.string.tinker_wait_for_update, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mIUpgradeBind != null) {
            mIUpgradeBind.register(null);
        }
        unbindService(mServiceConnection);
        TinkerLog.d(TAG, "ForceUpgradeDialog onDestroy unbindService");
        super.onDestroy();
    }

    private boolean mIsStop;
    @Override
    protected void onStop() {
        super.onStop();
        mIsStop = true;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        TinkerLog.d(TAG, "ForceUpgradeDialog onNewIntent");
        updateIntent(intent);
    }

}
