package com.streambus.basemodule.adapter;

import android.view.View;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/4/16
 * 描    述:
 * 修订历史：
 * ================================================
 */

public abstract class BaseLoadingHolder{
    private final IGetDataPresenter presenter;
    public View itemView;
    public BaseLoadingHolder(View itemView, IGetDataPresenter presenter) {
        this.itemView = itemView;
        this.presenter = presenter;
        initView(itemView);
    }

    protected abstract void initView(View itemView);

    public abstract void showLoading();

    public abstract void showError(String msg);

    public abstract void showNoData();

    public void showEmptyData() {

    }

    protected void getData() {
        if (presenter != null) {
            presenter.getData();
        }
    }

    BaseViewHolder createBaseViewHolder() {
        return new MyBaseViewHolder(itemView);
    }

    private static class MyBaseViewHolder extends BaseViewHolder {
        public MyBaseViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
        }

        @Override
        public void setData(Object data) {
        }
    }

    public interface IGetDataPresenter{
        void getData();
    }

}
