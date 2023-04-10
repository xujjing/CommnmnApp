package com.streambus.commonmodule.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.streambus.basemodule.utils.SLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huanglu on 2017/5/23.
 */

public class SystemUtil {
    private static final String TAG = "SystemParamUtil";
    private static Class gPropertyClass = null;
    private static Method gPropertyGet = null;
    private static Method gPropertySet = null;

    public static Class[] getParamTypes(Class cls, String mName) {
        Class[] cs = null;

        Method[] mtd = cls.getDeclaredMethods();
        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }

            cs = mtd[i].getParameterTypes();
            if (cs != null && cs.length == 1)
                break;
        }
        return cs;
    }

    public static void getPropertyClass() {
        try {
            gPropertyClass = Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getPropertyGetMethod() {
        if (gPropertyClass != null) {
            Class[] parameterTypes = getParamTypes(gPropertyClass, "get");
            try {
                gPropertyGet = gPropertyClass.getMethod("get", parameterTypes);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getPropertySetMethod() {
        if (gPropertyClass != null) {
            Class[] parameterTypes = getParamTypes(gPropertyClass, "set");
            try {
                gPropertyGet = gPropertyClass.getMethod("set", parameterTypes);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取有线 mac 地址
     *
     * @return 有线 mac 地址 or null
     */
    public static String getEth0MacAddress() {
        String macSerial = "";
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/eth0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return macSerial;

    }


    public static String getLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage() + "-" + locale.getCountry();
        String ret = null;
        if (language.endsWith("zh-CN")) {
            ret = "zh";
        } else if (language.endsWith("zh-TW")) {
            ret = "zh_hk";
        } else if (language.endsWith("zh-HK")) {
            ret = "zh_hk";
        } else if (language.startsWith("en")) {
            ret = "en";
        } else {
            ret = "zh_hk";
        }
        return ret;
    }

    public static String getOwnBoxWifiMac() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            String mac = (String) method.invoke(clazz.newInstance(), "ubootenv.var.flag1");
            if (!TextUtils.isEmpty(mac)) {
                return mac;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    public static String getWifiMacAddress(Context context) {
        String macAddress = "";
        if (Build.VERSION.SDK_INT >= 23) {
            Enumeration<NetworkInterface> interfaces;
            try {
                interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iF = interfaces.nextElement();
                    if ("wlan0".equals(iF.getName())) {
                        byte[] addr = iF.getHardwareAddress();
                        if (addr == null || addr.length == 0) {
                            continue;
                        }
                        StringBuilder buf = new StringBuilder();
                        for (byte b : addr) {
                            buf.append(String.format("%02X:", b));
                        }
                        if (buf.length() > 0) {
                            buf.deleteCharAt(buf.length() - 1);
                        }
                        macAddress = buf.toString();
                        return macAddress;
                    }
                }
            } catch (SocketException e) {
                return macAddress;
            }
        } else {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
            if (null != info) {
                macAddress = info.getMacAddress();
            }
        }
        return macAddress;
    }

    public static String dealMacStr(String mac) {
        if (mac != null && !mac.equals("") && mac.length() == 17) {
            if (mac.contains(":")) {
                mac = mac.replace(":", "");
            } else if (mac.contains(".")) {
                mac = mac.replace(".", "");
            }
            long lmac = Long.parseLong(mac, 16);
            String bit28 = NumberChange.toCustomNumericString(lmac, 28);
            String reMac = String.valueOf(bit28);
            if (reMac.length() >= 10) {
                mac = reMac.substring(0, 10);
            } else {
                while (reMac.length() < 10) {
                    reMac = reMac + "0";
                }
                mac = reMac;
            }
            mac = mac.toUpperCase();
        }
        return mac;
    }

    /**
     * 获取平台RAM大小
     *
     * @return 返回大小单位是兆(M)
     */
    public static String getRamMemroy() {
        String str1 = "/proc/meminfo";
        String str2 = "";
        BufferedReader localBufferedReader = null;
        try {
            FileReader fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            localBufferedReader.close();
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str2);
            str2 = m.replaceAll("").trim();
        } catch (IOException e) {
            try {
                if (localBufferedReader != null) {
                    localBufferedReader.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return Integer.parseInt(str2) / 1024 + " M";// Integer.parseInt(str2)/1024;
    }

    /**
     * 获取平台分辨率
     *
     * @return 返回：宽 * 高
     */
    public static String getDisplay(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        return screenWidth + "*" + screenHeight;
    }

    /**
     * 获取平台虚拟机最大内存控制
     *
     * @return 返回大小单位是兆（m）
     */
    public static String getLargeMemoryClass(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        int heapSize = manager.getLargeMemoryClass();
        return heapSize + "";
    }

    /**
     * dp2px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px2dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * true 表示app运行在TV
     *
     * @return
     */
    public static boolean isTVDevice() {  //更多支持盒子
        if (!hasNetworkEth()) {//手机端没有以太网端口
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasNetworkEth() {
        boolean hasEth = false;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                SLog.i(TAG, "networkInterfaces--->" + networkInterface);
                if (networkInterface.getName().toLowerCase().startsWith("eth")) {
                    hasEth = true;
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hasEth;
    }

    /**
     *
     * @return
     */
    public static boolean isSupportCamera() {
        boolean hasCamera=false;
        hasCamera = Camera.getNumberOfCameras() > 0;
        return hasCamera;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null && mActivityManager.getRunningAppProcesses() != null && mActivityManager.getRunningAppProcesses().size() > 0) {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return null;
    }
}
