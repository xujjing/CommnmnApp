/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streambus.tinkerlib.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by zhangshaowen on 16/4/7.
 */
public class Utils {
    private static final String TAG = "Tinker.Utils";

    /**
     * the error code define by myself
     * should after {@code ShareConstants.ERROR_PATCH_INSERVICE
     */
    public static final int ERROR_PATCH_GOOGLEPLAY_CHANNEL      = -20;
    public static final int ERROR_PATCH_ROM_SPACE               = -21;
    public static final int ERROR_PATCH_MEMORY_LIMIT            = -22;
    public static final int ERROR_PATCH_CRASH_LIMIT             = -23;
    public static final int ERROR_PATCH_CONDITION_NOT_SATISFIED = -24;

    public static final String PLATFORM = "platform";

    public static final int MIN_MEMORY_HEAP_SIZE = 45;

    private static boolean background = false;

    public static SharedPreferences getSP(Context context){
        return context.getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
    }

    public static void put(Context context, String key, Object object) {
        if (context != null) {

            SharedPreferences sp = getSP(context);
            SharedPreferences.Editor editor = sp.edit();
            if (object instanceof String) {
                editor.putString(key, (String) object);
            } else if (object instanceof Integer) {
                editor.putInt(key, (Integer) object);
            } else if (object instanceof Boolean) {
                editor.putBoolean(key, (Boolean) object);
            } else if (object instanceof Float) {
                editor.putFloat(key, (Float) object);
            } else if (object instanceof Long) {
                editor.putLong(key, (Long) object);
            } else {
                editor.putString(key, object.toString());
            }
            editor.commit();
        }
    }

    public static <T> T get(Context context, String key, T defaultObject) {
        Object value = defaultObject;
        if (context != null) {
            SharedPreferences sp = getSP(context);

            if (defaultObject instanceof String) {
                value = sp.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                value = sp.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                value = sp.getBoolean(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                value = sp.getFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                value = sp.getLong(key, (Long) defaultObject);
            }
        }
        return (T) value;
    }

    public static boolean isGooglePlay() {
        return false;
    }

    public static boolean isBackground() {
        return background;
    }

    public static void setBackground(boolean back) {
        background = back;
    }

    public static int checkForPatchRecover(long roomSize, int maxMemory) {
        if (Utils.isGooglePlay()) {
            return Utils.ERROR_PATCH_GOOGLEPLAY_CHANNEL;
        }
        if (maxMemory < MIN_MEMORY_HEAP_SIZE) {
            return Utils.ERROR_PATCH_MEMORY_LIMIT;
        }
        //or you can mention user to clean their rom space!
        if (!checkRomSpaceEnough(roomSize)) {
            return Utils.ERROR_PATCH_ROM_SPACE;
        }

        return ShareConstants.ERROR_PATCH_OK;
    }

    public static boolean isXposedExists(Throwable thr) {
        StackTraceElement[] stackTraces = thr.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            final String clazzName = stackTrace.getClassName();
            if (clazzName != null && clazzName.contains("de.robv.android.xposed.XposedBridge")) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static boolean checkRomSpaceEnough(long limitSize) {
        long allSize;
        long availableSize = 0;
        try {
            File data = Environment.getDataDirectory();
            StatFs sf = new StatFs(data.getPath());
            availableSize = (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();
            allSize = (long) sf.getBlockCount() * (long) sf.getBlockSize();
        } catch (Exception e) {
            allSize = 0;
        }

        if (allSize != 0 && availableSize > limitSize) {
            return true;
        }
        return false;
    }

    public static String getExceptionCauseString(final Throwable ex) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(bos);

        try {
            // print directly
            Throwable t = ex;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            t.printStackTrace(ps);
            return toVisualString(bos.toString());
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String toVisualString(String src) {
        boolean cutFlg = false;

        if (null == src) {
            return null;
        }

        char[] chr = src.toCharArray();
        if (null == chr) {
            return null;
        }

        int i = 0;
        for (; i < chr.length; i++) {
            if (chr[i] > 127) {
                chr[i] = 0;
                cutFlg = true;
                break;
            }
        }

        if (cutFlg) {
            return new String(chr, 0, i);
        } else {
            return src;
        }
    }

    public static class ScreenState {
        public interface IOnScreenOff {
            void onScreenOff();
        }

        public ScreenState(final Context context, final IOnScreenOff onScreenOffInterface) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);

            context.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent in) {
                    String action = in == null ? "" : in.getAction();
                    TinkerLog.i(TAG, "ScreenReceiver action [%s] ", action);
                    if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                        if (onScreenOffInterface != null) {
                            onScreenOffInterface.onScreenOff();
                        }
                    }
                    context.unregisterReceiver(this);
                }
            }, filter);
        }
    }
}
