package com.streambus.commonmodule.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileUtils {

    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        deleteDir(context.getExternalCacheDir());
    }

    public static final void deleteExpiredFile(String dir, long expired) {
        List<File> files = fileList(new File(dir));
        long currentTimeMillis = System.currentTimeMillis();
        Iterator var8 = files.iterator();

        while(var8.hasNext()) {
            File file = (File)var8.next();
            long lastModified = file.lastModified();
            if (currentTimeMillis - lastModified > expired) {
                file.delete();
            }
        }

    }

    public static void deleteFiles(File srcFile) {
        if (srcFile.isDirectory()) {
            LinkedList<File> dirList = new LinkedList();
            dirList.add(srcFile);

            while(!dirList.isEmpty()) {
                srcFile = (File)dirList.getFirst();
                File[] files = srcFile.listFiles();
                List<File> temDirs = new ArrayList();
                if (files != null && files.length > 0) {
                    File[] var4 = files;
                    int var5 = files.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        File file = var4[var6];
                        if (file.isDirectory()) {
                            temDirs.add(file);
                        } else {
                            file.delete();
                        }
                    }
                }

                if (!temDirs.isEmpty()) {
                    dirList.addAll(0, temDirs);
                } else {
                    ((File)dirList.removeFirst()).delete();
                }
            }
        } else {
            srcFile.delete();
        }

    }

    public static List<File> fileList(File srcFile) {
        List<File> fileList = new ArrayList();
        fileList.add(srcFile);
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            if (files != null && files.length > 0) {
                LinkedList temFiles = new LinkedList(Arrays.asList(files));

                while(!temFiles.isEmpty()) {
                    File file = (File)temFiles.removeFirst();
                    fileList.add(file);
                    if (file.isDirectory()) {
                        files = file.listFiles();
                        if (files != null && files.length > 0) {
                            temFiles.addAll(0, Arrays.asList(files));
                        }
                    }
                }
            }
        }

        return fileList;
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void saveFile(File file, String content) {
        try {
            FileOutputStream ops = new FileOutputStream(file);
            ops.write(content.getBytes());
            ops.flush();
            ops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeStream(InputStream inputStream, OutputStream outStream) throws IOException {
        byte[] data = new byte[4096];
        int length;
        while ((length = inputStream.read(data)) != -1) {
            outStream.write(data, 0, length);
        }
        outStream.close();
        inputStream.close();
    }

    public static String downLoadSizeFormat(long progress, long total) {
        DecimalFormat df = new DecimalFormat("0.0");
        String fileSizeString;
        String progressString;
        String wrongSize = "0B/0B";
        if (total == 0) {
            return wrongSize;
        }
        if (total < 1024) {
            fileSizeString = df.format((float) total) + "B";
            progressString = df.format((float) progress) + "B";
        } else if (total < 1048576) {
            fileSizeString = df.format((float) total / 1024) + "KB";
            progressString = df.format((float) progress / 1024) + "KB";
        } else if (total < 1073741824) {
            fileSizeString = df.format((float) total / 1048576) + "MB";
            progressString = df.format((float) progress / 1048576) + "MB";
        } else {
            fileSizeString = df.format((float) total / 1073741824) + "GB";
            progressString = df.format((float) progress / 1073741824) + "GB";
        }
        return progressString + "/" + fileSizeString;
    }

}
