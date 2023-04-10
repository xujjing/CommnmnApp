package com.streambus.basemodule.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.streambus.basemodule.R;
import com.streambus.basemodule.utils.SLog;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/5/30
 * 描    述: 必须指定 RecyclerView 宽高具体数值大小，该Item控件才能正确居中显示
 * 修订历史：
 * ================================================
 */
public class SimpleLoadingViewHolder extends BaseLoadingHolder {
    private static final String TAG = "SimpleLoadingViewHolder";
    ImageView mLoadingIv;
    TextView mLoadingDesc;
    private boolean mIsAbnormal;


    public static SimpleLoadingViewHolder createViewHolder(ViewGroup parent, BaseLoadingHolder.IGetDataPresenter presenter) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_defview, parent, false);
        return new SimpleLoadingViewHolder(itemView, presenter);
    }

    public static SimpleLoadingViewHolder createViewHolder(ViewGroup parent, BaseLoadingHolder.IGetDataPresenter presenter, FrameLayout.LayoutParams params) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_defview, parent, false);
        itemView.setLayoutParams(params);
        return new SimpleLoadingViewHolder(itemView, presenter);
    }

    private SimpleLoadingViewHolder(View itemView, IGetDataPresenter presenter) {
        super(itemView, presenter);
    }

    @Override
    protected void initView(View itemView) {
        mLoadingIv = itemView.findViewById(R.id.iv_loading);
        mLoadingDesc = itemView.findViewById(R.id.tv_loading_desc);
        SLog.e(TAG, "initView mLoadingIv=>" + mLoadingIv + "   mLoadingDesc=>" + mLoadingDesc);
        mLoadingDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAbnormal) {
                    getData();
                    showLoading();
                }
            }
        });
    }

    @Override
    public void showLoading() {
        mIsAbnormal = false;
        mLoadingIv.setVisibility(View.VISIBLE);
        ((AnimationDrawable)mLoadingIv.getBackground()).start();
        mLoadingDesc.setVisibility(View.INVISIBLE);
    }

    public void hideLoading() {
        ((AnimationDrawable )mLoadingIv.getBackground()).stop();
        mLoadingIv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoData() {
        hideLoading();
        mLoadingDesc.setVisibility(View.VISIBLE);
        mLoadingDesc.setText(R.string.no_data);
        mIsAbnormal = true;
    }

    @Override
    public void showError(String msg) {
        hideLoading();
        mLoadingDesc.setVisibility(View.VISIBLE);
        mLoadingDesc.setText(R.string.loading_failed);
        mIsAbnormal = true;
    }

    @Override
    public void showEmptyData() {
        hideLoading();
    }

    public void setFocusEnable(boolean focusEnable) {
        mLoadingDesc.setFocusable(focusEnable);
    }

    public void setClickEnable(boolean clickEnable) {
        mLoadingDesc.setClickable(clickEnable);
    }
}
