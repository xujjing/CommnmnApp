package com.yoostar.fileloggingutil;

import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/28
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ZipFileHelper {
    private static final String TAG = "ZipFileHelper";
    private static final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    public static void executeZipLogFile(final File file, final IZipObserver observer) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "executeZipLogFile file=>" + file +"file size >> "+file.length());
                File zipFile = null;
                try {
                    zipFile = FileUtils.zipFile(file);
                } catch (Exception ignore) {
                   Log.e(TAG, "executeZipLogFile file=>" + file + " error", ignore);
                } finally {
                    observer.onResult(zipFile);
                }
            }
        });
    }

    public static void executeZipCollectFile(final File saveDir, final LinkedList<File> logZipFiles, final LinkedList<File> ijkZipFiles, final IZipObserver observer) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LinkedList<File> files = new LinkedList<>(logZipFiles);
                files.addAll(ijkZipFiles);
                File collectFile = null;
                try {
                    if (files.isEmpty()) {
                        throw new Exception("files.isEmpty()");
                    }
                    collectFile = new File(saveDir, "CollectFile.zip");
                    FileUtils.deleteFiles(collectFile);
                    FileUtils.zipMergeFile(collectFile, files);
                } catch (Exception e) {
                    collectFile = null;
                   Log.e(TAG, "executeZipLogFile files=>" + files + " error", e);
                }finally {
                    observer.onResult(collectFile);
                }
            }
         });

    }

    public interface IZipObserver{
        void onResult(File zipFile);
    }
}
