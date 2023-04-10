package com.yoostar.fileloggingutil;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FileUtils {
    public static final void deleteExpiredFile(String dir, long expired) {
        List<File> files = fileList(new File(dir));
        long currentTimeMillis = System.currentTimeMillis();
        long lastModified;
        for (File file : files) {
            lastModified = file.lastModified();
            if ((currentTimeMillis - lastModified) > expired) {
                file.delete();
            }
        }
    }

    public static void deleteFiles(File srcFile) {
        if (srcFile.isDirectory()) {
            LinkedList<File> dirList = new LinkedList<>();
            dirList.add(srcFile);
            while (!dirList.isEmpty()) {
                srcFile = dirList.getFirst();
                File[] files = srcFile.listFiles();
                List<File> temDirs = new ArrayList<>();
                if (files != null && files.length > 0) {
                    for (File file : files) {
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
                    dirList.removeFirst().delete();
                }
            }
        } else {
            srcFile.delete();
        }

    }


    public static List<File> fileList(File srcFile) {
        List<File> fileList = new ArrayList<>();
        fileList.add(srcFile);
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            if (files != null && files.length > 0) {
                LinkedList<File> temFiles = new LinkedList<>(Arrays.asList(files));
                while (!temFiles.isEmpty()) {
                    File file = temFiles.removeFirst();
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

    public static File zipFile(File file) throws Exception{
        String name = file.getName();
        int end = name.lastIndexOf(".");
        if (end == -1) end = name.length();
        File zipFile = new File(file.getParent(), name.substring(0, end) + ".zip");
        ZipOutputStream zipOpt = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOpt.putNextEntry(new ZipEntry(name));
        FileInputStream ips = new FileInputStream(file);
        writeStream(ips, zipOpt);
        zipOpt.close();
        return zipFile;
    }

    public static ZipOutputStream copyZipFile(File zipFile) throws Exception{
        File temFile = new File(zipFile.getParent(), zipFile.getName() + "_tem");
        if (temFile.exists()) {
            deleteFiles(temFile);
        }
        if (!zipFile.renameTo(temFile)) {
            throw new Exception("zipFile.renameTo(temFile)");
        }

        ZipFile fileZip = new ZipFile(temFile);
        Enumeration<? extends ZipEntry> entries = fileZip.entries();
        ZipOutputStream zipOpt = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            zipOpt.putNextEntry(entry);
            writeStream(fileZip.getInputStream(entry), zipOpt);
        }
        temFile.delete();
        return zipOpt;
    }


    public static long getDirectoryAvailSize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        return directory.getFreeSpace();
    }


    public static void zipMergeFile(File collectZipFile, LinkedList<File> files) throws Exception {
        if (collectZipFile.exists()) {
            deleteFiles(collectZipFile);
        }
        ZipOutputStream zipOpt = new ZipOutputStream(new FileOutputStream(collectZipFile));
        for (File file  : files) {
            Log.d("TAG","long >> "+file.length());
            Log.d("TAG","file is name >> "+file.getName());
            zipOpt.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream ips = new FileInputStream(file);
            writeStream(ips, zipOpt);
        }
        zipOpt.close();
    }

    public static void writeStream(InputStream inputStream, OutputStream outStream) throws IOException {
        byte[] data = new byte[4096];
        int length;
        while ((length = inputStream.read(data)) != -1) {
            outStream.write(data, 0, length);
        }
        inputStream.close();
    }

    public static File unZipFile(File zipFile) throws Exception{
        String outPathString = zipFile.getParent();
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                // 获取文件的输出流
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[4096];
                // 读取（字节）字节到缓冲区
                while ((len = inZip.read(buffer)) != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
        return new File(outPathString);
    }
}
