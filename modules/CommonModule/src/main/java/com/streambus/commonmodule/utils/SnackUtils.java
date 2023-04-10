package com.streambus.commonmodule.utils;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.streambus.basemodule.utils.MyToast;
import com.streambus.commonmodule.R;

public class SnackUtils {

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;
    /**
     *
     * @param view
     * @param text
     * @param duration
     * @param actionText
     * @param actionCallBack
     * @param snackBack DISMISS_EVENT_ACTION：点击了右侧按钮导致消失
     *                  DISMISS_EVENT_CONSECUTIVE：新的Snackbar出现导致旧的消失
     *                  DISMISS_EVENT_MANUAL：调用了dismiss方法导致消失
     *                  DISMISS_EVENT_SWIPE：滑动导致消失
     *                  DISMISS_EVENT_TIMEOUT：设置的显示时间到了导致消失
     */
    public static void show(@NonNull View view, String text, int duration, String actionText, View.OnClickListener actionCallBack, Snackbar.Callback snackBack) {
//        Snackbar snackbar = Snackbar.make(view, text, duration).setAction(actionText, actionCallBack).setCallback(snackBack);//.show();
//        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
//        layout.setMinimumWidth(view.getDisplay().getWidth());
//        layout.setMinimumHeight(SystemUtil.dip2px(view.getContext(), 76));
//        TextView textView = (TextView) layout.findViewById(R.id.snackbar_text);
//        ViewGroup.LayoutParams lp = textView.getLayoutParams();
//        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        textView.setGravity(Gravity.CENTER);
//        textView.setLayoutParams(lp);
//        textView.setTextSize(SystemUtil.dip2px(view.getContext(), 36));
//        layout.setBackgroundColor(Color.parseColor("#E00001"));
//        snackbar.show();
        MyToast.makeText(view.getContext(), text, Toast.LENGTH_LONG).show();
    }

    public static void show(@NonNull View view, String text) {
        show(view, text, LENGTH_LONG, null, null, null);
    }

    public static void show(@NonNull View view, String text, int duration) {
        show(view, text, duration, null, null, null);
    }
}
