package com.streambus.basemodule.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/5/12
 * 描    述: 只考虑 RecyclerView 子View条目获取焦点情况
 * 修订历史：
 * ================================================
 */
public class FocusLimitRecyclerView extends RecyclerView {

    public void setOnFocusSearchListener(OnFocusSearchListener onFocusSearchListener) {
        mOnFocusSearchListener = onFocusSearchListener;
    }

    private OnFocusSearchListener mOnFocusSearchListener;

    public FocusLimitRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FocusLimitRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View search = super.focusSearch(focused, direction);
        if (mOnFocusSearchListener != null) {
            View returnSearch = mOnFocusSearchListener.focusSearch(focused, search, direction);
            if (returnSearch != search) {
                return returnSearch;
            }
        }
        try {
            int orientation = ((LinearLayoutManager) getLayoutManager()).getOrientation();
            if (orientation == HORIZONTAL) {
                if (direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT) {
                    if (search.getParent() != this) {
                        return focused;
                    }
                }
            } else if (orientation == VERTICAL) {
                if (direction == View.FOCUS_UP || direction == View.FOCUS_DOWN) {
                    if (search.getParent() != this) {
                        return focused;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return search;
    }

    public interface OnFocusSearchListener{
        View focusSearch(View focused, View search, int direction);
    }
}
