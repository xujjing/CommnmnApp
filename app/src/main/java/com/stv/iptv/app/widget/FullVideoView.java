package com.stv.iptv.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/7/20
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FullVideoView extends VideoView {

    public FullVideoView(Context context) {
        super(context);
    }

    public FullVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSize=MeasureSpec.getSize(widthMeasureSpec);
        int hSize=MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(wSize,hSize);
    }

    private boolean mIntercept;
    @Override
    public void stopPlayback() {
        super.stopPlayback();
        //将mUri设置为null
        mIntercept = true;
        setVideoURI(null);
        mIntercept = false;
    }

    @Override
    public void requestLayout() {
        if (mIntercept) {
            return;
        }
        super.requestLayout();
    }

    public void invalidate() {
        if (mIntercept) {
            return;
        }
        super.invalidate();
    }
}
