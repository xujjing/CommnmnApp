package com.streambus.commonmodule.utils;

import android.text.TextUtils;

import com.streambus.basemodule.utils.SLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlatformUtils {

    private static final String TAG = "PlatformUtils";

    public final static String CODE_SAVE_FLAG = "code0";

    public static final String DEFAULT_CODE = "65286619";

    /**
     * 是自己的平台(不包括mx，rk盒子)
     *
     * @return
     */
    public static boolean isYoostarPlatform() {
        boolean flag = false;
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "s905x platform_id = " + platform_id);
            if ("hx_ott1_s905x".equals(platform_id) || "hx_ott1_s905x3".equals(platform_id) || "hx_ott1_s905".equals(platform_id) ||
                    "hx_ott1_s905d".equals(platform_id) || "hx_ott1_s905w".equals(platform_id) || "hx_ott1_s805".equals(platform_id)
                    || "hx_ott1_rk3229".equals(platform_id)) {
                flag = true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return flag;
    }

    public static boolean is905DISDB() {
        String platform_id = null;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(),
                    "ro.product.firmware");
            SLog.i("is_supertv_Platform", platform_id);
            if (!TextUtils.isEmpty(platform_id)) {
                if (platform_id.contains("supertv_s905disdbt")) {
                    SLog.i("is905DISDB", "=true");
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否是较高系统(10个flag,10个code)
     *
     * @return
     */
    public static boolean isHighSystem() {
        String FLAG = CODE_SAVE_FLAG;
        String nameNode = "";
        if (isHighPlatform()) {
            nameNode = "/sys/class/unifykeys/name";
        } else if (is_S805_Platform() || is_mx_s2_Platform()) {
            nameNode = "/sys/class/aml_keys/aml_keys/key_name";
        }
        try {
            File files = new File(nameNode);
            FileOutputStream out = new FileOutputStream(files);
            byte[] name = FLAG.getBytes();
            out.write(name);
            out.close();

            byte[] codeByte = new byte[10];
            files = new File(nameNode);
            FileInputStream in = new FileInputStream(files);
            in.read(codeByte);
            in.close();
            String code = new String(codeByte);
            if (code != null) {
                code = code.trim();
            }
            if (FLAG.equals(code)) {
                return true;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }


    /**
     * S905X,S905,rk
     *
     * @return
     */
    public static boolean isHighPlatform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "isHighPlatform platform_id = " + platform_id);
            if ("hx_ott1_s905x".equals(platform_id) || "hx_ott1_s905x3".equals(platform_id) || "hx_ott1_s905d".equals(platform_id) || "hx_ott1_s905w".equals(platform_id)
                    || "hx_ott1_s905".equals(platform_id) || "hx_ott1_rk3229".equals(platform_id)) {
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }


    public static boolean is_S905_Platform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "s905 platform_id = " + platform_id);
            if ("hx_ott1_s905".equals(platform_id)) {
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }

    public static boolean is_S905X_Platform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "s905x platform_id = " + platform_id);
            if ("hx_ott1_s905x".equals(platform_id) || "hx_ott1_s905x3".equals(platform_id) || "hx_ott1_s905d".equals(platform_id) || "hx_ott1_s905w".equals(platform_id)) {
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }


    public static boolean is_rk_Platform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "rk platform_id = " + platform_id);
            if ("hx_ott1_rk3229".equals(platform_id)) {
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }


    public static boolean is_S805_Platform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            SLog.i(TAG, "platform_id = " + platform_id);
            if (platform_id.equals("hx_ott1_s805")) {
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }

    public static boolean is_mx_s2_Platform() {
        String platform_id;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(), "ro.product.firmware");
            SLog.i("is_mx_s2_Platform", platform_id);
            if (!TextUtils.isEmpty(platform_id)) {
                if (platform_id.contains("mx_s2") || platform_id.contains("tiger")) {
                    SLog.i("is_mx_s2_Platform", "=true");
                    return true;
                }
            }
        } catch (Exception e) {
            SLog.e(TAG, e.toString());
        }
        return false;
    }


    public static String getID() {
        if (isHighPlatform()) {
            return getS905ID();
        }
        if (is_S805_Platform() || is_mx_s2_Platform()) {
            return getS805ID();
        }
        return null;
    }

    private static String getS805ID() {
        String code;
        try {
            File files = new File("/sys/class/aml_keys/aml_keys/version");
            FileOutputStream out = new FileOutputStream(files);
            FileInputStream in;
            byte[] codeByte = new byte[72];

            byte[] version = "auto3".getBytes();
            out.write(version);
            out.close();
            files = new File("/sys/class/aml_keys/aml_keys/key_name");
            out = new FileOutputStream(files);
            byte[] name = "flag2".getBytes();
            out.write(name);
            out.close();
            files = new File("/sys/class/aml_keys/aml_keys/key_read");
            in = new FileInputStream(files);
            in.read(codeByte);
            in.close();
            code = new String(codeByte);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        if (!TextUtils.isEmpty(code)) {
            code = code.trim();
        }
        return code;
    }


    private static String getS905ID() {
        String code = "";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            code = (String) method.invoke(clazz.newInstance(), "ubootenv.var.flag2");
            SLog.i(TAG, "getS905ID idSerialNumber = " + code);
        } catch (Exception e) {
            SLog.i(TAG, "get id Exception:" + e.toString());
        }
        return TextUtils.isEmpty(code) ? code : code.trim();
    }

    /**
     * 805平台和mx平台一样
     *
     * @return
     */
    public static String get805Code() {
        String code;
        try {
            File files = new File("/sys/class/aml_keys/aml_keys/version");
            FileOutputStream out = new FileOutputStream(files);
            FileInputStream in;
            byte[] codeByte = new byte[12];
            byte[] version = "auto3".getBytes();
            out.write(version);
            out.close();
            files = new File("/sys/class/aml_keys/aml_keys/key_name");
            out = new FileOutputStream(files);
            byte[] name = CODE_SAVE_FLAG.getBytes();
            out.write(name);
            out.close();
            files = new File("/sys/class/aml_keys/aml_keys/key_read");
            in = new FileInputStream(files);
            in.read(codeByte);
            in.close();
            code = new String(codeByte);
        } catch (Exception e) {
            code = "";
            SLog.i(TAG, "get805Code exception:" + e.toString());
        }
        code = dealCode(code);
        return code;
    }

    /**
     * 905平台和905x平台一样
     *
     * @return
     */
    public static String getS905Code() {
        String code;
        try {
            byte[] codeByte = new byte[12];
            File files = new File("/sys/class/unifykeys/name");
            FileOutputStream out = new FileOutputStream(files);
            FileInputStream in;
            byte[] name = CODE_SAVE_FLAG.getBytes();
            out.write(name);
            out.close();
            files = new File("/sys/class/unifykeys/read");
            in = new FileInputStream(files);
            in.read(codeByte);
            in.close();
            code = new String(codeByte);
        } catch (Exception e) {
            code = "";
            SLog.i(TAG, "getS905Code exception:" + e.toString());
        }
        code = dealCode(code);
        return code;
    }

    private static String dealCode(String code) {
        if (!TextUtils.isEmpty(code) && code.length() > 8) {
            code = code.trim();
        } else {
            code = DEFAULT_CODE;
        }
        return code;
    }


    public static boolean is_S905D_Platform() {
        String platform_id = null;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(),
                    "platform_id");
            SLog.i("jing", "is_S905D_Platform platform_id = " + platform_id);
            if ("hx_ott1_s905d".equals(platform_id) || " hx_ott1_s905d".equals(platform_id)) {
                SLog.i("TAG", "!!!!!!!!!!!!!!!!!!!!!2");
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean is_S905W_Platform() {
        String platform_id = null;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(),
                    "platform_id");
            SLog.i("jing", "is_S905W_Platform platform_id = " + platform_id);
            if ("hx_ott1_s905w".equals(platform_id)) {
                SLog.i("TAG", "!!!!!!!!!!!!!!!!!!!!!");
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean is_S905X3_Platform() {
        String platform_id = null;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(),
                    "platform_id");
            SLog.i("jing", "is_S905X3_Platform platform_id = " + platform_id);
            if ("hx_ott1_s905x3".equals(platform_id)) {
                SLog.i("TAG", "!!!!!!!!!!!!!!!!!!!!!");
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否是supertv盒子
     * @return
     */
    public static boolean is_LiveTVBox_Platform() {
        String platform_id = null;
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            platform_id = (String) method.invoke(clazz.newInstance(),
                    "ro.product.firmware");
            SLog.i("is_supertv_Platform", platform_id);
            if (!TextUtils.isEmpty(platform_id)) {
                if (platform_id.contains("supertv")) {
                    SLog.i("is_LiveTVBox_Platform", "=true");
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isYoostarBox() {
        if (is_rk_Platform() ||
                is_S805_Platform() ||
                is_mx_s2_Platform() ||
                is_S905_Platform() ||
                is_S905X_Platform() ||
                is_S905D_Platform() ||
                is_S905W_Platform() ||
                is_S905X3_Platform()) {
            return true;
        }
        return false;
    }
}
