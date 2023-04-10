package com.yoostar.fileloggingutil;

import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/18
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class IjkWriteHandler {

    private static final String LOG_FILE_FORMAT = "ijkLog_%d.log";

    private static final int MAX_FILE_LENGTH = 10 * 1024 * 1024;
    private static final int IJK_FILE_TIME = 10 * 60 * 1000;

    private int mFilePosition;
    private File mSaveDir;
    private File mLogFile;
    private FileOutputStream mWriteOpt;
    private long mFileLength;
    private int mTrySolveCount;
    private ByteBuffer mFreeLogBuffer;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private volatile long mCreateFileTime;

    public IjkWriteHandler(File saveDir) {
        mSaveDir = saveDir;
    }

    private void newLogFile() throws Exception{
        if (!mSaveDir.isFile()) mSaveDir.delete();
        if (!mSaveDir.exists()) mSaveDir.mkdirs();
        mLogFile = new File(mSaveDir, String.format(LOG_FILE_FORMAT, mFilePosition));
        mWriteOpt = new FileOutputStream(mLogFile);
        mFilePosition++;
        mFileLength = 0;
    }

    private void write(byte[] data, int offset, int size) throws Exception {
        Exception exception;
        do {
            try {
                synchronized (this) {
                    if (mWriteOpt == null) {
                        newLogFile();
                    }
                    int len = size - offset;
                    mWriteOpt.write(data, offset, len);
                    mWriteOpt.flush();
                    mFileLength += len;
                }
                break;
            } catch (Exception e) {
                exception = e;
            }
        } while (trySolveException(exception));

    }

    private boolean trySolveException(Exception e) throws Exception {
        if (mTrySolveCount++ < 10) {
            if (FileUtils.getDirectoryAvailSize(mSaveDir) > (100 * 1024 * 1024)) {
                return true;
            }
        }
        throw new Exception(e);
    }


    private File closeWriteOpt(){
        File logFile = mLogFile;
        synchronized (this) {
            try {
                if (mWriteOpt != null) {
                    mWriteOpt.flush();
                    mWriteOpt.close();
                }
                newLogFile();
            } catch (Exception ignore) {
                mWriteOpt = null;
                mLogFile = null;
            }
        }
        return logFile;
    }


    public ByteBuffer getFreeLogBuffer() {
        ByteBuffer buffer = mFreeLogBuffer;
        mFreeLogBuffer = null;
        return buffer;
    }

    public void handlerIJkLog(final long startTime, final ByteBuffer logBuffer, final int offset, final boolean isGenerateZipFile) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                handlerCacheIJkLog(startTime, logBuffer, offset, isGenerateZipFile);
            }
        });
    }


    public synchronized void handlerCacheIJkLog(final long startTime, final ByteBuffer logBuffer, final int offset, final boolean isGenerateZipFile) {
        try {
            if (mCreateFileTime == 0) {
                mCreateFileTime = startTime;
            }
            write(logBuffer.array(), offset, logBuffer.position() + logBuffer.arrayOffset());
            if (isGenerateZipFile || (mFileLength > MAX_FILE_LENGTH && (SystemClock.uptimeMillis() - mCreateFileTime) > IJK_FILE_TIME)) {
                generateZipFile();
            }
        } catch (Exception e) {
            FileTreeIo.getInstance().ijkWriteHandlerError(e);
        }finally {
            if (!isGenerateZipFile) {
                logBuffer.clear();
                mFreeLogBuffer = logBuffer;
            }
        }
    }

    public LinkedList<File> getLogZipFiles() {
        return mLogZipFiles;
    }

    private LinkedList<File> mLogZipFiles = new LinkedList<>();
    private void generateZipFile() {
        while (mLogZipFiles.size() > 1) {
            File file = mLogZipFiles.removeFirst();
            file.delete();
        }
        final File file = closeWriteOpt();
        if (file != null) {
            ZipFileHelper.executeZipLogFile(file, new ZipFileHelper.IZipObserver() {
                @Override
                public void onResult(File zipFile) {
                    file.delete();
                    if (zipFile != null) {
                        mLogZipFiles.addLast(zipFile);
                    }
                }
            });
        }
    }


    public void exit() {
        synchronized (this) {
            try {
                if (mWriteOpt != null) {
                    mWriteOpt.flush();
                    mWriteOpt.close();
                }
            } catch (Exception ignore) {
            }
        }
    }


}
