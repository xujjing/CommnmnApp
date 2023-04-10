package com.streambus.commonmodule.logs;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.umeng.UMengManager;
import com.yoostar.fileloggingutil.FileTreeIo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author jcy
 */
public class CrashHandler implements UncaughtExceptionHandler {


    private static final String TAG = "CrashHandler";
    private static CrashHandler INSTANCE;

    private Context mContext;

    private UncaughtExceptionHandler mDefaultCrashHandler;

    public static final String PATH_CRASH_EXCEPTION = "/Crash";

    private CrashHandler(Context c) {
        mContext = c;
        init();
    }

    public static void setup(Context context) {
        synchronized (CrashHandler.class) {
            if (INSTANCE == null) {
                INSTANCE = new CrashHandler(context);
            }
        }
    }

    public static CrashHandler getInstance(Context c) {
        if (c == null) {
            SLog.e(TAG, "Context is null");
            return null;
        }
        synchronized (CrashHandler.class) {
            if (INSTANCE == null) {
                INSTANCE = new CrashHandler(c);
            }
        }
        return INSTANCE;
    }

    private void init() {
        if (mContext == null) {
            return;
        }
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        deleteExpiredCrashLog();
    }

    /**
     * 删除过期的日志
     */
    private void deleteExpiredCrashLog() {
        File saveDir = new File(mContext.getExternalCacheDir(), PATH_CRASH_EXCEPTION);
        File[] files = saveDir.listFiles();
        if (files != null) {
            long currentTimeMillis = System.currentTimeMillis();
            long lastModified;
            for (File file  : files) {
                lastModified = file.lastModified();
                if ((currentTimeMillis - lastModified) > (7 * 24 * 60 * 60 * 1000)) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        byte[] temLog = FileTreeIo.getInstance().appCrash();
        handleException(temLog, thread, ex);
        UMengManager.handleException(mContext, ex);
        SLog.e(TAG, "uncaughtException", ex);
        SLog.e(TAG, "===========================End UncaughtException================================");
        SLog.e(TAG, "===========================End UncaughtException================================");
        SLog.e(TAG, "===========================End UncaughtException================================");
        mDefaultCrashHandler.uncaughtException(thread, ex);
        //MyApplication.exitApp(false);
    }

    /**
     * 保存异常信息，下次启动时上传服务器
     */
    private void handleException(byte[] loges, Thread thread, Throwable ex) {
        SLog.e(TAG, "[handleException]", ex);
        File folder = new File(mContext.getExternalCacheDir(), PATH_CRASH_EXCEPTION);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String exceptType = ex.getClass().getSimpleName();
        String exception = formatCrashInfo(thread, ex);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "doUpCrashLog start");
                    String fileName = "Crash-" + new SimpleDateFormat("MM-dd HH-mm-ss").format(Calendar.getInstance().getTime()) + "(" + Process.myPid() + ")" + ".txt";
                    File saveFile = new File(folder, fileName);
                    FileOutputStream opt = new FileOutputStream(saveFile);
                    opt.write(loges);
                    opt.write((exceptType + exception).getBytes("UTF-8"));
                    opt.close();
                    LogCollectionUtils.doUpCrashLog(exceptType, exception, saveFile);
                    Log.i(TAG, "doUpCrashLog Success");
                } catch (Exception e) {
                    SLog.e(TAG, "doUpCrashLog Exception", e);
                    e.printStackTrace();
                }finally {
                    synchronized (INSTANCE) {
                        INSTANCE.notifyAll();
                    }
                }
            }
        }).start();

        try {
            synchronized (INSTANCE) {
                INSTANCE.wait(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化异常信息
     */
    private String formatCrashInfo(Thread thread, Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        String dump = info.toString();
        printWriter.close();
        String crashInfo = String.format("AndroidRuntime: FATAL EXCEPTION: Thread-%s\n" +
                        "Message: %s\n" +
                        "%s",
                thread.getName(), ex.getMessage(), dump);
        return crashInfo;
    }

}
