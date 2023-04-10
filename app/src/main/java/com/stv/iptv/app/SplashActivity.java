package com.stv.iptv.app;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.gson.reflect.TypeToken;
import com.streambus.basemodule.base.BaseActivity;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.CommonApplication;
import com.streambus.commonmodule.advert.AdvertManager;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.OrderBean;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.commonmodule.dialog.RemindDialog;
import com.streambus.commonmodule.dialog.RemindManager;
import com.streambus.commonmodule.utils.AppUtil;
import com.streambus.commonmodule.utils.GsonHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private static final int PERMISSON_REQUEST_CODE = 1001;
    @BindView(R.id.fl_content)
    FrameLayout mFrameLayout;
    @BindView(R.id.tv_version)
    TextView mTextView;
    @BindView(R.id.video_view)
    VideoView mVideoView;

    private boolean mIsVisible;
    private boolean mIsPermission;
    private boolean mIsStartMainActivity;
    private long mVisibleTime;

    @Override
    protected int attachLayoutRes() {
        setWindowFull();
        setTheme(R.style.AppTheme_Launcher);
        return R.layout.activity_splash;
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

    @Override
    protected void initViewModel() {
        SLog.d(TAG, "initViewModel");
        String path = AdvertManager.getInstance().splashAdvertLiveData.getValue();
        if (!TextUtils.isEmpty(path)) {
            SLog.i(TAG, "splashAdvert path=>" + path);
            mFrameLayout.setBackground(Drawable.createFromPath(path));
        }
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mTextView.setText("v" + AppUtil.getAppVersionName(getApplicationContext()));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mIsVisible = true;
                mVisibleTime = SystemClock.uptimeMillis();
                initAfter();
            }
        });
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.enjoy_welcome));
//        mFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                mIsVisible = true;
//                initAfter();
//            }
//        });
    }

    @Override
    protected void updateViews(boolean isRefresh) {
        ArrayList<TestBean> list = new ArrayList<>();
        list.add(TestBean.createBean(1));
        list.add(TestBean.createBean(2));
        SLog.d(TAG, "updateViews Json=>" + GsonHelper.toJson(list));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (CommonApplication.isAFTER_DONE()) {
            superOnCreate(savedInstanceState);
            mIsStartMainActivity = true;
            SLog.i(TAG, "onCreate startMainActivity data=>" + getIntent().getStringExtra("data"));
            Intent intent = new Intent(this, MainActivity.class).putExtra("data", getIntent().getStringExtra("data"));
            startActivity(intent);
            finish();
        } else {
            super.onCreate(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsPermission) {
            checkPermission();
        }
    }

    private void checkPermission() {
        int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        SLog.d(TAG, "checkPermission permissionResult=>" + permissionResult);
        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSON_REQUEST_CODE);
            return;
        }
        if (!mIsPermission) {
            mIsPermission = true;
            initAfter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSON_REQUEST_CODE) {
            SLog.d(TAG, "onRequestPermissionsResult grantResults=>" + grantResults[0]);
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showMissingPermissionDialog(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                return;
            }
            checkPermission();
        }
    }

    /**
     * 弹出对话框, 提示用户手动授权
     * @param canShowRequest
     */
    private void showMissingPermissionDialog(boolean canShowRequest) {
        RemindDialog remindDialog = RemindDialog.newInstance(new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                dialog.dismiss();
                if (canShowRequest) {
                    checkPermission();
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        }, new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                dialog.dismiss();
                ((CommonApplication)getApplication()).killApk();
            }
        });
        remindDialog.setCancelable(false);
        remindDialog.show(canShowRequest ? getResources().getString(R.string.please_grant) : getResources().getString(R.string.please_enable), getSupportFragmentManager());
    }

    private void initAfter() {
        SLog.i(TAG, "initAfter mIsVisible= " + mIsVisible + "  mIsPermission=" + mIsPermission + "   uptimeMillis=" + SystemClock.uptimeMillis());
        if (mIsVisible && mIsPermission) {
            handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doAfter();
                }
            }, 500);
        }
    }

    private void doAfter() {
        if (!RemindManager.getInstance().checkConnectivity(getSupportFragmentManager(), false, new RemindManager.ConnectivityAction(){
            @Override public void onCancel() {
                ((CommonApplication)getApplication()).killApk();
            }
            @Override public void availableConnect() {
                SLog.d(TAG, "availableConnect doAfter");
                doAfter();
            }
        })){return;}

        ((CommonApplication) getApplication()).initAfterPermissions(SplashActivity.this);
        handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, Math.max(3500 + mVisibleTime - SystemClock.uptimeMillis(), 1500));
    }

    private void startMainActivity() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            mIsStartMainActivity = true;
            SLog.i(TAG,"startMainActivity data=>" + getIntent().getStringExtra("data"));
            Intent intent = new Intent(this, MainActivity.class).putExtra("data", getIntent().getStringExtra("data"));
            startActivity(intent);
            handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 5000);
        } else {
            getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_RESUME) {
                        getLifecycle().removeObserver(this);
                        startMainActivity();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mIsStartMainActivity) {
            ((CommonApplication) getApplication()).killApk();
        }
    }
}
