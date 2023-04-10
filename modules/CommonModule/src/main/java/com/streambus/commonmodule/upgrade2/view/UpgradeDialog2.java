package com.streambus.commonmodule.upgrade2.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.R;
import com.streambus.commonmodule.upgrade2.UpgradeManager;
import com.streambus.commonmodule.upgrade2.bean.UpgradeBean2;
import com.streambus.commonmodule.utils.AppUtil;
import com.streambus.commonmodule.utils.FileUtils;
import com.streambus.commonmodule.utils.LifecycleUtils;

import java.io.File;

import androidx.lifecycle.Observer;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/10
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UpgradeDialog2 extends Activity {

    private static final String TAG = "UpgradeDialog";
    private static final String KEY_ACTION_TYPE = "key_action_type";
    private static final String KEY_UPGRADE_DATA = "key_upgrade_data";

    public static final int VALUE_ACTION_APK_FOCUS= 1;
    public static final int VALUE_ACTION_APK_COMMON= 2;
    public static final int VALUE_ACTION_APK_REMIND= 3;


    public static void showUpgrade(UpgradeBean2 bean, int action) {
        LifecycleUtils.subjectActivityStart(new LifecycleUtils.ActivityObserver() {
            @Override
            public void accept(Activity activity) {
                LifecycleUtils.unSubjectActivityStart(this);
                Intent intent = new Intent(activity, UpgradeDialog2.class);
                intent.putExtra(KEY_ACTION_TYPE, action);
                intent.putExtra(KEY_UPGRADE_DATA, bean);
                activity.startActivity(intent);
            }
        });
    }

    /************************************* - Activity的显示逻辑 - ****************************************/
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

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private TextView mTvContent;
    private TextView mTvMsg;
    private ProgressBar mProgressBar;
    private TextView mTvProgress;
    private TextView mTvNetSpeed;
    private View mBtCancel;
    private View mBtInstall;

    private FrameLayout mProgressLayout;
    private RelativeLayout mUpgradeInfoLayout;
    private RelativeLayout mRemindIgnoreLayout;
    private TextView mTvRemindContent;

    private int mActionType;
    private UpgradeBean2 mUpgradeBean;
    private DownloadObserver mDownloadObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setWindowFull();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upgrade2);
        initView();
        updateView(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resetView();
        updateView(intent);
    }


    @Override
    public void onBackPressed() {
        if (mActionType == VALUE_ACTION_APK_FOCUS || mActionType == VALUE_ACTION_APK_REMIND) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloadObserver != null) {
            mDownloadObserver.unSubject();
        }
    }

    private void initView() {
        mTvContent = findViewById(R.id.tv_conent);
        mProgressBar = findViewById(R.id.sb_progress);
        mTvProgress = findViewById(R.id.tv_progress);
        mTvNetSpeed = findViewById(R.id.tv_net_speed);
        mTvMsg = findViewById(R.id.tv_msg);
        mBtCancel = findViewById(R.id.bt_cancel);
        mBtCancel.setOnClickListener(v -> finish());
        mBtInstall = findViewById(R.id.bt_install);
        mProgressLayout = findViewById(R.id.progress_layout);
        mUpgradeInfoLayout = findViewById(R.id.upgrade_info_layout);
        mRemindIgnoreLayout = findViewById(R.id.remind_ignore_layout);
        mTvRemindContent = findViewById(R.id.remind_tv_conent);
        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeManager.getInstance().remindCheckIgnore(mUpgradeBean, true);
                finish();
            }
        });
        findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeManager.getInstance().remindCheckIgnore(mUpgradeBean, false);
            }
        });
    }

    private void resetView() {
        if (mDownloadObserver != null) {
            mDownloadObserver.unSubject();
        }
        SLog.d(TAG, "resetView  Old mActionType=" + mActionType + "  mUpgradeBean=" + mUpgradeBean);
        mActionType = 0;
        mProgressLayout.setVisibility(View.INVISIBLE);
        mBtCancel.setVisibility(View.INVISIBLE);
        mBtInstall.setVisibility(View.INVISIBLE);
    }

    private void updateView(Intent intent) {
        mActionType = intent.getIntExtra(KEY_ACTION_TYPE, 0);
        mUpgradeBean = (UpgradeBean2) intent.getSerializableExtra(KEY_UPGRADE_DATA);
        SLog.d(TAG, "updateView  mActionType=" + mActionType + "  mUpgradeBean=" + mUpgradeBean);
        mTvContent.setText(mUpgradeBean.getDescription());
        mTvRemindContent.setText(mUpgradeBean.getDescription());
        mUpgradeInfoLayout.setVisibility(mActionType == VALUE_ACTION_APK_REMIND ? View.GONE : View.VISIBLE);
        mRemindIgnoreLayout.setVisibility(mActionType == VALUE_ACTION_APK_REMIND ? View.VISIBLE : View.GONE);
        if (mActionType == VALUE_ACTION_APK_REMIND) {
            findViewById(R.id.tv_confirm).requestFocus();
        }
        if (mActionType != VALUE_ACTION_APK_REMIND) {
            updateUpgradeInfo();
        }
    }

    private void updateUpgradeInfo() {
        mDownloadObserver = new DownloadObserver(UpgradeManager.getInstance().getDownloadTask(mUpgradeBean.getMd5()));
        mDownloadObserver.subject();
    }

    private class DownloadObserver implements Observer<Integer>{

        private UpgradeManager.DownloadTask downloadTask;

        public DownloadObserver(UpgradeManager.DownloadTask downloadTask) {
            this.downloadTask = downloadTask;
        }

        @Override
        public void onChanged(Integer integer) {
            switch (integer) {
                case UpgradeManager.DownloadTask.START:
                    mProgressLayout.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(0);
                    mTvProgress.setText("0B/0B");
                    mTvNetSpeed.setText("0KB/S");
                    break;
                case UpgradeManager.DownloadTask.DOING:
                    mProgressLayout.setVisibility(View.VISIBLE);
                    mTvProgress.post(updateProgressRunnable);
                    break;
                case UpgradeManager.DownloadTask.SUCCESS:
                    mProgressLayout.setVisibility(View.GONE);
                    handleDownloadSuccess(downloadTask.downLoadFile);
                    break;
                case UpgradeManager.DownloadTask.ERROR:
                    mProgressLayout.setVisibility(View.GONE);
                    handleDownloadError();
                    break;
                default:
                    break;
            }
        }

        private Runnable updateProgressRunnable = new Runnable() {
            private long lastProgress;
            private long lastPostTime;
            @Override
            public void run() {
                long uptimeMillis = SystemClock.uptimeMillis();
                long upProgress = downloadTask.downloadLength;
                String tcpSpeed = "0KB/S";
                if (lastPostTime != 0) {
                    long speed = (upProgress - lastProgress) / (uptimeMillis - lastPostTime);
                    tcpSpeed = speedFormat(speed);
                }
                lastProgress = upProgress;
                lastPostTime = uptimeMillis;
                handleProgress(upProgress, downloadTask.contentLength, tcpSpeed);
                mTvProgress.postDelayed(this, 500);
            }
            private String speedFormat(long speed) {
                String result;
                if (speed > 1024) {
                    long partA = speed / 1024;
                    long partB = (speed - partA * 1024) / 100;
                    result = partA + "." + partB + "MB/S";
                } else {
                    result = speed + "KB/S";
                }
                return result;
            }
        };

        public void subject() {
            downloadTask.statusLiveData.observeForever(this);
        }

        public void unSubject() {
            downloadTask.statusLiveData.removeObserver(this);
            mTvProgress.removeCallbacks(updateProgressRunnable);
        }
    }

    private void handleProgress(long progress, long total, String tcpSpeed) {
        SLog.d(TAG, "onProgress progress=" + progress + "  total=" + total + "  tcpSpeed=" + tcpSpeed);
        mProgressBar.setProgress((int) (progress * 100 / total));
        mTvProgress.setText(FileUtils.downLoadSizeFormat(progress, total));
        mTvNetSpeed.setText(tcpSpeed);
    }

    private void handleDownloadSuccess(File file) {
        mProgressLayout.setVisibility(View.GONE);
        mBtCancel.setVisibility(mActionType == VALUE_ACTION_APK_COMMON ? View.VISIBLE : View.INVISIBLE);
        mBtInstall.setVisibility(View.VISIBLE);
        mBtInstall.requestFocus();
        mBtInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtil.showApkInstallPage(UpgradeDialog2.this, file);
            }
        });
    }

    private void handleDownloadError() {
        Toast.makeText(getApplicationContext(), R.string.dialog_download_failed, Toast.LENGTH_LONG).show();
    }

}
