package com.streambus.basemodule.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.streambus.basemodule.R;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/19
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class MyToast {
    public static Toast makeText(Context context, CharSequence text, int duration) {
        TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.toast_text, null);
        Toast toast = new Toast(context);
        toast.setView(textView);
        textView.setText(text);
        toast.setDuration(duration);
        return toast;
    }
}