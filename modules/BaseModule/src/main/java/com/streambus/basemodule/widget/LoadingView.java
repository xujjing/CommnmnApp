package com.streambus.basemodule.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.streambus.basemodule.R;
import com.streambus.basemodule.R2;
import com.streambus.basemodule.base.BaseDialogFragment;

import butterknife.BindView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/15
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LoadingView extends BaseDialogFragment {
    private static final String TAG = "LoadingView";
    @BindView(R2.id.iv_loading)
    ImageView mIvLoading;

    @Override
    protected int attachLayoutRes() {
        return R.layout.dialog_loading;
    }

    @Override
    protected void initViewModel() {

    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        setCancelable(false);
    }

    @Override
    protected void updateViews(boolean isRefresh) {

    }

    @Override
    public void onStart() {
        super.onStart();
        ((AnimationDrawable)mIvLoading.getDrawable()).start();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AnimationDrawable)mIvLoading.getDrawable()).stop();
    }

    public void show(@NonNull FragmentManager manager) {
        super.show(manager, TAG);
    }

}