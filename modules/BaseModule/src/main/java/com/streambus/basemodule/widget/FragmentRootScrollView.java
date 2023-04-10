package com.streambus.basemodule.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.streambus.basemodule.utils.SLog;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/25
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FragmentRootScrollView extends ScrollView {
    private View mFocused;
    private static final String TAG = "FragmentRootScrollView";

    public FragmentRootScrollView(Context context) {
        super(context);
    }

    public FragmentRootScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FragmentRootScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        mFocused = focused;
        SLog.d(TAG, "requestChildFocus_mCurrentFocused =>" + mFocused + "  parent=>" + mFocused.getParent());
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        SLog.d(TAG, "requestFocus direction=>" + direction + "  previouslyFocusedRect=>" + previouslyFocusedRect);
        if (mFocused != null && direction == 130) {
            mFocused.requestFocus();
            return true;
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }
}
