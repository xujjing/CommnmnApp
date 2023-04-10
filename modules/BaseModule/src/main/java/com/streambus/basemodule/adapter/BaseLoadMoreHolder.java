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
public abstract class BaseLoadMoreHolder{
    private final IGetMoreDataPresenter presenter;
    public View itemView;
    public BaseLoadMoreHolder(View itemView, IGetMoreDataPresenter presenter) {
        this.itemView = itemView;
        this.presenter = presenter;
        initView(itemView);
    }

    protected abstract void initView(View itemView);

    public abstract void showLoading();

    public abstract void showNoMoreData();

    public abstract void showLoadMoreError(String msg);

    protected void getMoreData() {
        if (presenter != null) {
            presenter.getMoreData();
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

    public interface IGetMoreDataPresenter {
        void getMoreData();
    }

}
