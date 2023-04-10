package com.stv.iptv.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.streambus.basemodule.utils.SLog;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/15
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeTabTopLinearLayout extends LinearLayout {
    private static final String TAG = "HomeTabLinearLayout";
    private View mCurrentFocused;

    public HomeTabTopLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        mCurrentFocused = focused;
        SLog.d(TAG, "requestChildFocus_mCurrentFocused =>" + mCurrentFocused);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        SLog.d(TAG, "focusSearch_direction =>" + direction);
        if (direction == View.FOCUS_UP) {
            if (mCurrentFocused != null && mCurrentFocused.isSelected()) {
                return mCurrentFocused;
            } else {
                return getSelectItem();
            }
        }else if ((direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT)) {
            View searchFocus =  super.focusSearch(focused, direction);
            if (searchFocus!= null && searchFocus.getParent() == this) {
                return searchFocus;
            } else {
                return focused;
            }
        }
        return super.focusSearch(focused, direction);
    }

    private View getSelectItem() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).isSelected()) {
                return getChildAt(i);
            }
        }
        return null;
    }
}
