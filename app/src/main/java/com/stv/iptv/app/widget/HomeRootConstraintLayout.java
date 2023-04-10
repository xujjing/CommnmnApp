package com.stv.iptv.app.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.streambus.basemodule.utils.SLog;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/15
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeRootConstraintLayout extends ConstraintLayout {
    private static final String TAG = "HomeRootConstraintLayout";
    private HomeTabTopLinearLayout mTopTabLinearLayout;
    private HomeTabBottomLinearLayout mBottomTabLinearLayout;
    private View mFocused;
    private View mViewPager;

    public HomeRootConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        mViewPager = findViewById(R.id.view_pager);
//        mTopTabLinearLayout = findViewById(R.id.ly_home_tab);
//        mBottomTabLinearLayout = findViewById(R.id.ly_bottom);
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

    @Override
    public View focusSearch(View focused, int direction) {
        SLog.d(TAG, "focusSearch_direction =>" + direction);
        if (mFocused == mViewPager) {
            if (direction == View.FOCUS_UP) {
                return mTopTabLinearLayout.focusSearch(focused, direction);
            } else if (direction == View.FOCUS_DOWN){
                return mBottomTabLinearLayout.focusSearch(focused, direction);
            }
        }
        View nextFocus = super.focusSearch(focused, direction);
        if (direction == View.FOCUS_UP) {
            if (nextFocus!=null && nextFocus.getParent() == mTopTabLinearLayout) {
                return mTopTabLinearLayout.focusSearch(focused, direction);
            }
        }
        return nextFocus;
    }
}
