package com.streambus.basemodule.widget;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.streambus.basemodule.utils.SLog;

public class FocusView {
    private final static int duration = 140;
    private final static float startScale = 1.0f;
    public static final float END_SCALE = 1.05f;
    public static void onFocusIn(final View view){

        ValueAnimator animIn = ValueAnimator.ofFloat(startScale, END_SCALE);
        animIn.setDuration(duration);
        animIn.setInterpolator(new DecelerateInterpolator());
        animIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
            }
        });
        animIn.start();
    }


    public static void onFocusOut(final View view) {
        ValueAnimator animOut = ValueAnimator.ofFloat(END_SCALE, startScale);
        animOut.setDuration(duration);
        animOut.setInterpolator(new DecelerateInterpolator());
        animOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
            }
        });
        animOut.start();
    }

    public static View.OnFocusChangeListener generateScaleFocusChangeListener(float scaleX, float scaleY, long duration) {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                SLog.d("generateScaleFocusChangeListener", "onFocusChange hasFocus=" + hasFocus + "   scaleX=" + scaleX + " scaleY=" + scaleY);
                if (hasFocus) {
                    v.animate().scaleX(scaleX).scaleY(scaleY).setDuration(duration).start();
                } else {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(duration).start();
                }
            }
        };
    }
}
