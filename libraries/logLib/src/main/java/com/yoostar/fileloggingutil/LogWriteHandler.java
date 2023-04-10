package com.yoostar.fileloggingutil;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/18
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LogWriteHandler {
    private static final String LOG_FILE_FORMAT = "defLog_%d.log";
    private static final String DEF_HEADER = "DEF_HEADER";
    private static final int MAX_FILE_LENGTH = 10 * 1024 * 1024;
    private EncodeHelper mEncodeHelper;
    private boolean mIsEncode;
    private int mFilePosition;
    private File mSaveDir;
    private File mLogFile;
    private BufferedOutputStream mWriteOpt;
    private long mFileLength;

    private int mTrySolveCount;


    public LogWriteHandler(File saveDir, boolean isDebug) {
        mSaveDir = saveDir;
        if (!isDebug) {
            mEncodeHelper = new EncodeHelper(new EncodeHelper.IEncodeStateChanger() {
                @Override
                public void onEncodeState(boolean isEncode) {
                    mIsEncode = isEncode;
                }
            });
        }
    }

    private void newLogFile() throws Exception{
        try {
            if (!mSaveDir.isFile()) mSaveDir.delete();
            if (!mSaveDir.exists()) mSaveDir.mkdirs();
            Log.d("executeZipLogFile","mSaveDir >> "+mSaveDir);
            mLogFile = new File(mSaveDir, String.format(LOG_FILE_FORMAT, mFilePosition));
            Log.d("executeZipLogFile","mLogFile >> "+mLogFile);
            mWriteOpt = new BufferedOutputStream(new FileOutputStream(mLogFile));
            if (mIsEncode) {
                mWriteOpt.write((mEncodeHelper.getDecodeAESKey() + "\n").getBytes("UTF-8"));
            }
        } finally {
            if (mWriteOpt == null) {
                mLogFile = null;
            }
            mFilePosition++;
            mFileLength = 0;
        }
    }

    private void write(String key, byte[] data) throws Exception{
        Exception exception;
        do {

            try {
                synchronized (this) {
                    if (mWriteOpt == null) {
                        newLogFile();
                    }
                    byte[] buff = key.getBytes("UTF-8");
                    mWriteOpt.write(buff);
                    mFileLength += buff.length;

                    buff = mIsEncode ? mEncodeHelper.encodeAES(data) : data;
                    if (buff.hashCode() != data.hashCode()) {
                        mWriteOpt.write("-|||-".getBytes("UTF-8"));
                        mFileLength += 10;
                    }
                    mWriteOpt.write(buff);
                    mFileLength += buff.length;

                    mWriteOpt.write('\n');
                    mFileLength += 1;
                }
                break;
            } catch (Exception e) {
                exception = e;
            }
        } while (trySolveException(exception));

    }

    private boolean trySolveException(Exception e) throws Exception{
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
                    Log.d("executeZipLogFile","mFileLength 111 >> "+mFileLength);
                    Log.d("executeZipLogFile","closeWriteOpt 111 >> "+mLogFile.length());
                }
                newLogFile();
            } catch (Exception ignore) {
                mWriteOpt = null;
                mLogFile = null;
                Log.e("closeWriteOpt","异常 >> ",ignore);
            }
        }
        Log.d("executeZipLogFile","closeWriteOpt  logFile >> "+logFile.length());
        return logFile;
    }



    private LinkedList<File> mLogZipFiles = new LinkedList<>();

    public void handlerLog(String key, byte[] data) throws Exception {
        write(key, data);
        if (mFileLength > MAX_FILE_LENGTH) {
            generateZipFile();
        }
    }
    public synchronized void generateZipFile() {
        if (mFileLength < 1024) {
            return;
        }
        final File file = closeWriteOpt();
//        try {
//            Thread.sleep(2000);
//            Log.d("generateZipFile","++++++");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Log.d("generateZipFile","executeZipLogFile >> "+file.length()+"file >>"+file.getAbsolutePath());

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

    public LinkedList<File> getLogZipFiles() {
        return mLogZipFiles;
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
