package com.streambus.basemodule.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/6/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class SupportFrameLayout extends FrameLayout {
    private OnDispatchTouchLisneter mOnDispatchTouchLisneter;

    public SupportFrameLayout(Context context) {
        super(context);
    }

    public SupportFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SupportFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDispatchTouchLisneter != null && mOnDispatchTouchLisneter.dispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    public void setOnDispatchTouchLisneter(OnDispatchTouchLisneter onDispatchTouchLisneter) {
        mOnDispatchTouchLisneter = onDispatchTouchLisneter;
    }

    public interface OnDispatchTouchLisneter{
        boolean dispatchTouchEvent(MotionEvent ev);
    }
}
