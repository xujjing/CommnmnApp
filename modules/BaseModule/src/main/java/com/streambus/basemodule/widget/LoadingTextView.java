package com.streambus.basemodule.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/27
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LoadingTextView extends AppCompatTextView {

    private final String mText;
    private OnHideListener mOnHideListener;

    public LoadingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mText = getText().toString();
    }

    public void showLoading() {
        setVisibility(VISIBLE);
        startLoading();
    }
    public void hideLoading() {
        stopLoading();
        setVisibility(GONE);
        if (mOnHideListener != null) {
            mOnHideListener.onHide();
        }
    }

    public void startLoading() {
        loadIndex = 0;
        post(mLoadingRunnable);
    }
    public void stopLoading() {
        removeCallbacks(mLoadingRunnable);
    }

    private int loadIndex;
    private Runnable mLoadingRunnable = new Runnable() {
        private String[] loadings = {" .    ", " . .  ", " . . .", "      "};
        @Override
        public void run() {
            setText(mText + loadings[loadIndex++ % loadings.length]);
            postDelayed(this, 350);
        }
    };

    public void setOnHideListener(OnHideListener listener) {
        mOnHideListener = listener;
    }

    public interface OnHideListener {
        void onHide();
    }
}
