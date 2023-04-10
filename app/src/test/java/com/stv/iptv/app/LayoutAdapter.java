package com.stv.iptv.app;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/26
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LayoutAdapter {

    private List<String> resList = Arrays.asList(
            "D:\\RichTV\\RichTV\\modules\\BaseModule\\src\\main\\res",
            "D:\\RichTV\\RichTV\\modules\\CommonModule\\src\\main\\res",
            "D:\\RichTV\\RichTV\\modules\\LiveModule\\src\\main\\res",
            "D:\\RichTV\\RichTV\\modules\\UserModule2\\src\\main\\res",
            "D:\\RichTV\\RichTV\\modules\\VodModule\\src\\main\\res",
            "D:\\RichTV\\RichTV\\app\\src\\main\\res"
    );

    @Test
    public void dimens() throws Exception {
        for (String res : resList) {
            File file = new File(res);
            if (file.isDirectory()) {
                listDir(file);
            }
        }
    }


    public void listDir(File resDir) throws Exception{
        File[] files = resDir.listFiles();
        for (File file  : files) {
            if (file.isDirectory()) {
                listDir(file);
            } else if (file.isFile() && file.getName().endsWith(".xml")) {
                toDimens(file);
            }
        }
    }

    public void toDimens(File file) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        File newFile = new File(file.getAbsolutePath().replace(".xml", ".xmll"));
        BufferedOutputStream opt = new BufferedOutputStream(new FileOutputStream(newFile));
        String s;
        boolean isChanger = false;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
            Matcher matcher = Pattern.compile("\"([0-9]+)(dp|sp|px)\"").matcher(s);////="10dp"
            Matcher matcher1 = Pattern.compile("\"([0-9]+)dp\"").matcher(s);////="10dp"
            Matcher matcher2 = Pattern.compile("\"([0-9]+)sp\"").matcher(s);////="10dp"
            Matcher matcher3 = Pattern.compile("\"([0-9]+)px\"").matcher(s);////="10dp"
            StringBuffer buffer = new StringBuffer();
            int start = 0;
            while (matcher.find()) {
                int big = matcher.start();
                int end = matcher.end();

                int big1 = matcher.start(1);
                int end1 = matcher.end(1);
                buffer.append(String.format("%s\"@dimen/d%s\"", s.substring(start, big), s.substring(big1, end1)));
                start = end;
                isChanger = true;
            }
            if (start != s.length()) {
                buffer.append(s.substring(start));
            }
            System.out.println(buffer);
            opt.write(buffer.toString().getBytes());
            opt.write('\n');
        }
        opt.flush();
        opt.close();
        br.close();
        if (isChanger) {
            file.delete();
            newFile.renameTo(file);
        } else {
            newFile.delete();
        }
    }
}
