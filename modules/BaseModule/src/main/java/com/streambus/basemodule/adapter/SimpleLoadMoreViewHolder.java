package com.streambus.basemodule.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streambus.basemodule.R;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/5/30
 * 描    述: 必须指定 RecyclerView 宽高具体数值大小，该Item控件才能正确居中显示
 * 修订历史：
 * ================================================
 */
public class SimpleLoadMoreViewHolder extends BaseLoadMoreHolder {

    private TextView mLoadingDesc;
    private boolean mIsAbnormal;
    private ImageView mLoadingIv;
    public SimpleLoadMoreViewHolder(View itemView, IGetMoreDataPresenter presenter) {
        super(itemView, presenter);
    }

    public static BaseLoadMoreHolder createViewHolder(ViewGroup parent, IGetMoreDataPresenter presenter) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loadmore_defview, parent, false);
        return new SimpleLoadMoreViewHolder(itemView, presenter);
    }

    @Override
    protected void initView(View itemView) {
        mLoadingIv = itemView.findViewById(R.id.iv_loading); //         TODO
        mLoadingDesc = itemView.findViewById(R.id.tv_loading_desc); //  TODO
        mLoadingDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAbnormal) {
                    getMoreData();
                    showLoading();
                }
            }
        });
    }


    @Override
    public void showLoading() {
        mIsAbnormal = false;
        if (mLoadingIv.getVisibility() == View.GONE){
            mLoadingIv.setVisibility(View.VISIBLE);
            ((AnimationDrawable)mLoadingIv.getBackground()).start();
            mLoadingDesc.setText(R.string.loading_more);
        }
    }

    @Override
    public void showNoMoreData() {
        mLoadingIv.setVisibility(View.GONE);
        ((AnimationDrawable)mLoadingIv.getBackground()).stop();
        mLoadingDesc.setText(R.string.no_more_data);
        mIsAbnormal = false;
    }

    @Override
    public void showLoadMoreError(String msg) {
        mLoadingIv.setVisibility(View.GONE);
        ((AnimationDrawable)mLoadingIv.getBackground()).stop();
        mLoadingDesc.setText(R.string.loading_more_failed);
        mIsAbnormal = true;
    }

    public void setFocusEnable(boolean focusEnable) {
        mLoadingDesc.setFocusable(focusEnable);
    }
}