package com.streambus.commonmodule.bean;

import android.graphics.Color;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.R;

import java.io.Serializable;

/**
 * Author:Yoostar
 * Date:2021/6/17 18:35
 * Description:
 */
public class SubtitleFontFace implements Serializable {
    private final String TAG = SubtitleFontFace.class.getSimpleName();

    public final int[] sizeLib = new int[] {R.dimen.d20, R.dimen.d30, R.dimen.d40};
    //public final int[] solidLib = new int[] {R.drawable.transparent_bg, R.drawable.half_tran_white, R.drawable.half_tran_black};
    public final int[] solidLib = new int[] {Color.parseColor("#00000000"), Color.parseColor("#33FFFFFF"), Color.parseColor("#33000000")};

    private int size = sizeLib[1];
    private int solid = solidLib[0];

    public SubtitleFontFace() {
    }

    public SubtitleFontFace(int size, int solid) {
        this.size = size;
        this.solid = solid;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSolid() {
        return solid;
    }

    public void setSolid(int solid) {
        this.solid = solid;
    }

    public int getSizeIndex() {
        int index = 0;
        for (int i = 0; i < sizeLib.length; i++) {
            if (size == sizeLib[i]) {
                index = i;
            }
        }
        return index;
    }

    public int getSolidIndex() {
        int index = 0;
        for (int i = 0; i < solidLib.length; i++) {
            SLog.i(TAG, "SubtitleFontFace, FontFace, match solid, i=" + i);
            if (solid == solidLib[i]) {
                index = i;
                SLog.i(TAG, "SubtitleFontFace, FontFace, match solid success, index=" + index);
            }
        }
        return index;
    }
}
