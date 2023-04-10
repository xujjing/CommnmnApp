package com.streambus.basemodule.utils;

import android.util.Log;


public class SLog {

    private static ISLog sLogImp = new ISLog() {
        @Override public void v(String tag, String msg) {
            Log.v(tag, msg);
        }
        @Override public void d(String tag, String msg) {
            Log.d(tag, msg);
        }
        @Override public void i(String tag, String msg) {
            Log.i(tag, msg);
        }
        @Override public void w(String tag, String msg) {
            Log.w(tag, msg);
        }
        @Override public void w(String tag, String msg, Throwable t) {
            Log.w(tag, msg, t);
        }
        @Override public void e(String tag, String msg) {
            Log.e(tag, msg);
        }
        @Override public void e(String tag, String msg, Throwable t) {
            Log.e(tag, msg, t);
        }
    };

    private static int sLevel;

    public static void setSLogImp(ISLog liveLog) {
        sLogImp = liveLog;
    }

    public static void setLevel(int level) {
        sLevel = level;
    }

    public static void v(String tag, String msg) {
        if (sLevel <= Log.VERBOSE) {
            sLogImp.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sLevel <= Log.DEBUG) {
            sLogImp.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sLevel <= Log.INFO) {
            sLogImp.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sLevel <= Log.WARN) {
            sLogImp.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable t) {
        if (sLevel <= Log.WARN) {
            sLogImp.w(tag, msg, t);
        }
    }

    public static void e(String tag, String msg) {
        if (sLevel <= Log.ERROR) {
            sLogImp.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable t) {
        if (sLevel <= Log.ERROR) {
            sLogImp.e(tag, msg, t);
        }
    }

    public interface ISLog {
        public void v(String tag, String msg);

        public void d(String tag, String msg);

        public void i(String tag, String msg);

        public void w(String tag, String msg);

        public void w(String tag, String msg, Throwable t);

        public void e(String tag, String msg);

        public void e(String tag, String msg, Throwable t);

    }
}
