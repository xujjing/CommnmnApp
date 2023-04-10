package com.streambus.basemodule.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/4/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public T itemData;
    final boolean applyFocus;
    final boolean applyClick;
    public BaseViewHolder(View itemView) {
        this(itemView, false, false);
    }
    public BaseViewHolder(View itemView, boolean applyFocus) {
        this(itemView, applyFocus, false);
    }
    public BaseViewHolder(View itemView, boolean applyFocus, boolean applyClick) {
        super(itemView);
        this.applyFocus = applyFocus;
        this.applyClick = applyClick;
        initView(itemView);
    }

    public abstract void initView(View itemView);

    public abstract void setData(T data);

    public void onFocusChange(boolean hasFocus, int position){}
    public void onClick(int position){}
}
