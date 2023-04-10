package com.streambus.basemodule.adapter;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/4/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class BaseGridAdapter<T> extends BaseAdapter<T> {

    private GridLayoutManager mGridLayoutManager;
    private int mSpanCount;

    public BaseGridAdapter(boolean isLoadingEnable, boolean isLoadMoreEnable) {
        super(isLoadingEnable, isLoadMoreEnable);
    }

    public GridLayoutManager initGridLayoutManager(GridLayoutManager gridLayoutManager) {
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
            int spanCount = gridLayoutManager.getSpanCount();
            @Override public int getSpanSize(int position) {
                int itemViewType = getItemViewType(position);
                if (itemViewType == LOADING_VIEW || itemViewType == LOADMORE_VIEW) {
                    return spanCount;
                }
                if (mIsLoadMoreEnable && (getItemCount() - 2) == position) {
                    int i = (getItemCount() -1) % spanCount;
                    if (i != 0) {
                        return spanCount - i + 1;
                    }
                }
                return 1;
            }
        });
        return gridLayoutManager;
    }
}
