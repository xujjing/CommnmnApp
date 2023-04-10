package com.yoostar.fileloggingutil;

import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ================================================
 * 作    者：zhoujianan
 * 版    本：v1.0
 * 创建日期：2020/8/31
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FileTreeIo{

    private static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final String LOG_FORMAT = "%s %d-%d %c/%s: ";
    private static final char[] PRIORITY = {'V', 'V', 'V', 'D', 'I', 'W', 'E', 'E', 'E'};
    private static final String TAG = "FileLoggingTree";
    private static final long EXPIRED_TIME = 24 * 60 * 60 * 1000;
    private static final int DEF_LOG_TYPE = 0;
    private static final int IJK_LOG_TYPE = 1;
    private final Thread mThread;

    private TempCacheByteArrayStream mTempCache = new TempCacheByteArrayStream(1024 * 8);

    /************************************* 写入文件 ****************************************/

    private LinkedBlockingQueue<LogEntry> mLogQueue;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH-mm-ss-SSS");
    private String mSaveDirPath;


    private File mMineSaveDir;
    private LogWriteHandler mLogWriteHandler;
    private IjkWriteHandler mIjkWriteHandler;


    public void log(int priority, String tag, String message, Throwable t) {
        if (!TextUtils.isEmpty(message)) {
            try {
                message = message.trim().replace("\n", "\n    ");
                log(priority, tag, message.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                //ignore
            }
        }
    }

    private void log(int priority, String tag, byte[] data) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());
        String formatKey = String.format(LOG_FORMAT, timeStamp, Process.myPid(), Process.myTid(), PRIORITY[priority], tag);
        mTempCache.write(formatKey, data);
        if (mLogQueue != null && !mLogQueue.offer(new LogEntry(formatKey, data))) {
           Log.e(TAG, "File LogQueue is Full.!!!!");
        }
    }

    private static class LogEntry{
        private final String key;
        private final byte[] data;
        public LogEntry(String key, byte[] data) {
            this.key = key;
            this.data = data;
        }
    }

    private void runWriteFile() throws Exception{
        LogEntry logEntry;
        while (true) {
            logEntry = mLogQueue.take();
            mLogWriteHandler.handlerLog(logEntry.key, logEntry.data);
        }
    }


    /************************************* 初始化 ****************************************/
    private static FileTreeIo INSTANCE;

    private FileTreeIo(String saveDirPath, final boolean isDebug) {
        mSaveDirPath = saveDirPath;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    init();
                    prepare(isDebug);
                    mLogQueue = new LinkedBlockingQueue<>();
                    setLogNative(true);
                    setIjkLog(true);
                    if (mOnWriteListener != null) {
                        mOnWriteListener.onStart();
                    }
                    runWriteFile();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mLogQueue = null;
                    setLogNative(false);
                    setIjkLog(false);
                    mLogWriteHandler.exit();
                    mIjkWriteHandler.exit();
                    if (mOnWriteListener != null) {
                        mOnWriteListener.onLogStop();
                    }
                }
            }
        });
        mThread.start();
    }

    void ijkWriteHandlerError(Exception e) {
        setIjkLog(false);
        if (mOnWriteListener != null) {
            mOnWriteListener.onIjkLogFailed();
        }
    }

    private void init() {
        File folder = new File(mSaveDirPath);
        if (folder.isFile()) folder.delete();
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            deleteExpiredLog();
            mergeLegacyFile();
        }
    }

    private void prepare(boolean isDebug) {
        mMineSaveDir = new File(mSaveDirPath, simpleDateFormat.format(Calendar.getInstance().getTime()) + "(" + Process.myPid() + ")");
        mLogWriteHandler = new LogWriteHandler(mMineSaveDir, isDebug);
        mIjkWriteHandler = new IjkWriteHandler(mMineSaveDir);
    }

    public static FileTreeIo setup(String saveDirectory, boolean isDebug) {
        INSTANCE = new FileTreeIo(saveDirectory,isDebug);
        return INSTANCE;
    }

    public static FileTreeIo getInstance() {
        return INSTANCE;
    }

    public void exit() {
        if (mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    public void getCollectFile(final OnFileCollectListener listener) {
        mLogWriteHandler.generateZipFile();
        handlerCacheIJKLog();
        ZipFileHelper.executeZipCollectFile(mMineSaveDir, mLogWriteHandler.getLogZipFiles(), mIjkWriteHandler.getLogZipFiles(),
                new ZipFileHelper.IZipObserver() {
                    @Override
                    public void onResult(File zipFile) {
                        if (zipFile != null) {
                            listener.onNext(zipFile);
                        } else {
                            listener.onError("get logFile null");
                        }
                    }
                });
    }

    public byte[] appCrash() {
        return mTempCache.toByteArray();
    }


    /**
     * 删除过期的日志
     */
    private void deleteExpiredLog() {
        File[] files = new File(mSaveDirPath).listFiles();
        if (files == null) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        for (File file : files) {
            try {
                String fileName = file.getName();
                String time = fileName.substring(0, fileName.indexOf('('));
                Date date = simpleDateFormat.parse(time);
                date.setYear(new Date(file.lastModified()).getYear());
                long lastModified = date.getTime();
                Log.d(TAG, "deleteExpiredLog fileName=" + fileName + "   time=>" + time);
                Log.d(TAG, "deleteExpiredLog currentTimeMillis=" + currentTimeMillis + "   lastModified=" + lastModified);
                if ((currentTimeMillis - lastModified) > EXPIRED_TIME) {
                    FileUtils.deleteFiles(file);
                }
            } catch (Exception ignor) {
                FileUtils.deleteFiles(file);
            }
        }
    }

    /**
     * 处理之前退出APP后遗留未压缩文件
     */
    private void mergeLegacyFile() {
        final File[] files = new File(mSaveDirPath).listFiles();
        if (files != null) {
            Thread mergeLegacyThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (File piddir : files) {
                        File[] infles = piddir.listFiles();
                        if (infles == null) {
                            piddir.delete();
                            continue;
                        }
                        try {
                            for (File file : infles) {
                                if (file.isDirectory()) {
                                    FileUtils.deleteFiles(file);
                                } else if (file.getName().endsWith(".log")) {
                                    FileUtils.zipFile(file);
                                    file.delete();
                                }
                            }
                        } catch (Exception e) {
                            piddir.delete();
                        }
                    }
                }
            });
            mergeLegacyThread.start();
        }
    }

    public interface IOnWriteListener {
        void onStart();
        void onIjkLogFailed();
        void onLogStop();
    }


    private IOnWriteListener mOnWriteListener;
    public void setOnWriteLogListener(IOnWriteListener listener) {
        mOnWriteListener = listener;
    }

    public interface OnFileCollectListener {
        void onNext(File file);

        void onError(String errorMsg);
    }

    /************************************* - Native - ****************************************/
    static {
        System.loadLibrary("logfile_lib");
    }

    /**
     * LogNative
     * @param isOpen   是否开启native打印日志
     */
    public static native void setLogNative(boolean isOpen);

    public static native void testLoginNative(boolean b);
    /**
     * 普通native调用来打印日志到文件java log文件
     * @param priority
     * @param tag
     * @param data
     */
    static final void logNative(int priority, String tag, byte[] data){
        if (INSTANCE != null) {
            if (data != null && data.length > 0) {
                byte[] copy = new byte[data.length];
                System.arraycopy(data, 0, copy, 0, data.length);
                INSTANCE.log(priority, tag, copy);
            }
        }
    }


    /**
     * 开启IJK日志文件打印
     * @param isOpen   是否开启native打印日志
     */
    static native void setIjkLog(boolean isOpen);

    static native int getIjkBufferPosition();

    public static native void testIJkLogNative(boolean b);

    static final ByteBuffer handlerIjkLogAndNextByteBuffer(int position) {
        if (INSTANCE != null) {
            return INSTANCE.handlerIjkMediaLog(position);
        }
        return null;
    }

    private ByteBuffer mIjkLogBuffer;
    private long mCreateIjkLogTime;
    private int mIjkLogBufferOffset;
    private ByteBuffer handlerIjkMediaLog(int position) {
        synchronized (this) {
            do {
                if (mIjkLogBuffer != null) {
                    if (position > mIjkLogBufferOffset) {
                        if (position > mIjkLogBuffer.limit()) {
                            Log.w(TAG, "handlerIjkMediaLog position > mIjkLogBuffer.limit(): position="
                                    + position + "  limit=" + mIjkLogBuffer.limit());
                            position = mIjkLogBuffer.limit();
                        }
                        mIjkLogBuffer.position(position);
                        Log.i(TAG, "handlerIjkMediaLog mIjkLogBuffer ==>" + mIjkLogBuffer);
                        mIjkWriteHandler.handlerIJkLog(mCreateIjkLogTime, mIjkLogBuffer, mIjkLogBufferOffset, false);
                    }
                }
            } while (false);
            mCreateIjkLogTime = SystemClock.uptimeMillis();
            ByteBuffer freeLogBuffer = mIjkWriteHandler.getFreeLogBuffer();
            Log.i(TAG, "freeLogBuffer ==>" + freeLogBuffer);
            mIjkLogBuffer = freeLogBuffer != null ? freeLogBuffer : ByteBuffer.allocateDirect(32 * 1024);
            mIjkLogBufferOffset = mIjkLogBuffer.arrayOffset();
            return mIjkLogBuffer;
        }
    }

    private void handlerCacheIJKLog() {
        synchronized (this) {
            int position = getIjkBufferPosition();
            if (position > mIjkLogBufferOffset) {
                mIjkLogBuffer.position(position);
                mIjkWriteHandler.handlerCacheIJkLog(mCreateIjkLogTime, mIjkLogBuffer, mIjkLogBufferOffset, true);
                mIjkLogBufferOffset = position + mIjkLogBuffer.arrayOffset();
            }
        }
    }

}
