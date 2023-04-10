package com.streambus.commonmodule.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.streambus.basemodule.base.BaseDialogFragment;
import com.streambus.basemodule.widget.FullDialog;
import com.streambus.commonmodule.R;
import com.streambus.commonmodule.R2;

import butterknife.BindView;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/12/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RemindDialog extends BaseDialogFragment {
    private static final String TAG = "RemindDialog";
    @BindView(R2.id.tv_info)
    TextView mTvInfo;
    @BindView(R2.id.tv_confirm)
    View mConfirmView;
    @BindView(R2.id.tv_cancel)
    View mCancelView;

    private String mInfo;

    private OnClickListener mOnConfirmClickListener;
    private OnClickListener mOnCancelClickListener;

    public static RemindDialog newInstance(OnClickListener onConfirmClickListener) {
        RemindDialog remindDialog = new RemindDialog();
        remindDialog.mOnConfirmClickListener = onConfirmClickListener;
        return remindDialog;
    }

    public static RemindDialog newInstance(OnClickListener onConfirmClickListener, OnClickListener onCancelClickListener) {
        RemindDialog remindDialog = new RemindDialog();
        remindDialog.mOnConfirmClickListener = onConfirmClickListener;
        remindDialog.mOnCancelClickListener = onCancelClickListener;
        return remindDialog;
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.dialog_remind;
    }

    @Override
    protected void initViewModel() {

    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCancelClickListener != null) {
                    mOnCancelClickListener.onClick(RemindDialog.this,v);
                }
            }
        });
        mConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnConfirmClickListener != null) {
                    mOnConfirmClickListener.onClick(RemindDialog.this,v);
                }
            }
        });
    }

    @Override
    protected void updateViews(boolean isRefresh) {
        mTvInfo.setText(mInfo);
        mConfirmView.requestFocus();
    }

    public void show(String info, FragmentManager fragmentManager) {
        mInfo = info;
        super.show(fragmentManager, TAG);
    }

    public interface OnClickListener{
        public void onClick(DialogFragment dialog, View v);
    }
}
