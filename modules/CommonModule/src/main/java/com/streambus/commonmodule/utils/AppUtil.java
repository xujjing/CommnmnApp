package com.streambus.commonmodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.streambus.basemodule.BaseApplication;
import com.streambus.basemodule.utils.SLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.FileProvider;


public class AppUtil {
    public final static String TAG = "AppUtil";

    //获取名称
    public static String getAppName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
        } catch (Throwable e) {

        }
        return "";
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
           SLog.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        int versionCode = 0;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    // 获取包名
    public static String getAppPackageName(Context context) {
        try {
            String pkName = context.getPackageName();
            return pkName;
        } catch (Exception e) {
        }
        return null;
    }


    /*
    获取设备SN
     */
    static Method systemProperties_get = null;

    public static String getAndroidOsSystemProperties() {
        String ret;
        try {
            String[] propertys = {"ro.boot.serialno", "ro.serialno"};
            systemProperties_get = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            if ((ret = (String) systemProperties_get.invoke(null, propertys[0])) != null)
                return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * @return 设备品牌
     */
    public static String getSystembrand() {
        return Build.BRAND;
    }

    /**
     * @return 设备版本号
     */
    public static String getSystemID() {
        return Build.ID;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    public static String getHardWare() {
        return Build.HARDWARE;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }




    public static String getSdkVersion() {
        String version = Build.VERSION.RELEASE;
        if (TextUtils.isEmpty(version)) {
            version = "";
        }
        return version;
    }

    /**
     * 以太网获取路由id地址
     *
     * @return
     */
    public static String getEthernetRouteIpAddress() {
        String ip = "";
        try {
            Process p = Runtime.getRuntime().exec("ip route show");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ipHave = "";
            String line = null;
            //获取到的可能包含ip的信息如下：
//            default via 192.168.2.1 dev eth0
//            default via 192.168.2.1 dev eth0  metric 206
//               192.168.2.0/24 dev eth0  scope link
//                192.168.2.0/24 dev eth0  proto kernel  scope link  src 192.168.2.21  metric 206
//                192.168.2.0 dev eth0  scope link
            while ((line = in.readLine()) != null
                    && !line.equals("null")) {
                if (line.contains("default via ")) {
                    ipHave += line;
                    break;
                }
            }
            if (!"".equals(ipHave)) {
                //获取网关ip信息
                String patternS = "default via (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) dev eth0 ";
                Pattern p1 = Pattern.compile(patternS);
                Matcher matcher = p1.matcher(ipHave);
                if (matcher.matches())
                    ip = matcher.group(1);
               SLog.d(TAG, "ethernet route ip：" + ip);
                return ip;//拿到ip则返回了
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }



    /**
     * 获取手机的Mac地址，在Wifi未开启或者未连接的情况下也能获取手机Mac地址
     */
    public static String getMacAddress(Context context) {
        String macAddress = null;
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
        }
        return macAddress;
    }

    /**
     * 获取WifiInfo
     */
    public static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        if (null != wifiManager) {
            info = wifiManager.getConnectionInfo();
        }
        return info;
    }

    /**
     * 获取网关IP
     *
     * @return
     */
    public static String getGateWay() {
        String[] arr;
        try {
            Process process = Runtime.getRuntime().exec("ip route list table 0");
            String data = null;
            BufferedReader ie = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string = in.readLine();

            arr = string.split("\\s+");
            return arr[2];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    public static List<Activity> getAllActivitys(Context mContext, String packageName){
        List<Activity> list=new ArrayList<>();
        try {
            Class<?> activityThread=Class.forName("android.app.ActivityThread");
            Method currentActivityThread=activityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            //获取主线程对象
            Object activityThreadObject=currentActivityThread.invoke(null);
            Field mActivitiesField = activityThread.getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Map<Object,Object> mActivities = (Map<Object,Object>) mActivitiesField.get(activityThreadObject);
            for (Map.Entry<Object,Object> entry:mActivities.entrySet()){
                Object value = entry.getValue();
                Class<?> activityClientRecordClass = value.getClass();
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Object o = activityField.get(value);
                Activity mActivity = (Activity) o;
                //SLog.i(TAG, "[getAllActivitys]," + mActivity.getPackageName() + " ," + mActivity.getComponentName().getClassName() + "," +mActivity.getTaskId());
                if (!TextUtils.isEmpty(packageName)) {
                    if (packageName.equals(mContext.getPackageName())) {
                        list.add(mActivity);
                    }
                } else {
                    list.add(mActivity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public static int getStatusBarHeight(Activity mActivity) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return mActivity.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            SLog.d(TAG, "get status bar height fail");
            e1.printStackTrace();
            try {
                Rect frame = new Rect();
                mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                return frame.top;
            } catch (Exception e) {
                e.printStackTrace();
                return 75;
            }
        }
    }

    //true >> 手机
    public static boolean isRunPhone() {
        boolean isphone = false;
        TelephonyManager telephony = (TelephonyManager) BaseApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        if (type == TelephonyManager.PHONE_TYPE_NONE) {
            SLog.i(TAG, "isRunPhone_当前设备非手机");
        } else {
            if (!PlatformUtils.isYoostarBox()) {
                SLog.i(TAG, "isRunPhone_当前设备是手机");
                isphone = true;
            } else {
                SLog.i(TAG, "isRunPhone_当前设备非手机....");
            }
        }
        return isphone;
    }

    public static void showApkInstallPage(Context context, File file) {
        if (android.os.Build.VERSION.SDK_INT < 24) {
            SLog.i(TAG,"showApkInstallPage SDK_INT < 24 file=>" + file.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            SLog.i(TAG,"showApkInstallPage SDK_INT >= 24  file=>" + file.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String authority = context.getPackageName() + ".fileprovider";
//            String authority = "com.iptv.stv.rich.fileprovider";
            SLog.i(TAG, "authority=>" + authority);
            Uri fileUri = FileProvider.getUriForFile(context, authority, file);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}

