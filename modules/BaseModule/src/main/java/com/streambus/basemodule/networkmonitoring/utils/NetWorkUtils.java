package com.streambus.basemodule.networkmonitoring.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;


import com.streambus.basemodule.BaseApplication;
import com.streambus.basemodule.R;
import com.streambus.basemodule.networkmonitoring.annotation.NetType;
import com.streambus.basemodule.networkmonitoring.manager.NetworkManager;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *获取网络状态的工具类
 */
public class NetWorkUtils {
    private final static String TAG = "NetworkUtil";
    // 网络连接状态
    public static final int NETWORK_NONE = 0; // 未连接
    public static final int NETWORK_WIFI = 1; // 无线连接
    public static final int NETWORK_ETH = 2; // 有线连接


    /**
     * @return 是否有网络
     */
    public static boolean isNetWorkAvailable(){
        ConnectivityManager manager = (ConnectivityManager) NetworkManager.getDefault().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager == null){
            return  false;
        }
        NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
        if(networkInfos != null){
            for(NetworkInfo info:networkInfos){
                if(info.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return @NetType
     */
    public static @NetType
    String getNetworkType(){
        ConnectivityManager manager = (ConnectivityManager) NetworkManager.getDefault().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager == null){
            return  NetType.NONE;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo == null){
            return  NetType.NONE;
        }
        int type = networkInfo.getType();
        if(type == ConnectivityManager.TYPE_MOBILE){
            if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){
                return NetType.CMNET;
            }else{
                return NetType.CMWAP;
            }
        }else if(type == ConnectivityManager.TYPE_WIFI){
            return NetType.WIFI;
        }
        return NetType.AUTO;
    }

    /**
     * 打开网络设置界面
     * @param context
     * @param requestCode 请求跳转
     */
    public static void openNetSetting(Context context, int requestCode){
        Intent intent = new Intent("/");
        ComponentName cn = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
        intent.setComponent(cn);
        intent.setAction("android.intent.action.VIEW");
        ((Activity)context).startActivityForResult(intent,requestCode);
    }

    /**
     * 截取域名
     * @param sDomain
     * @return
     */
    public static String getDomainName(String sDomain) {
        Pattern r = Pattern.compile("(?<=://).+?(?=/)");
        Matcher m = r.matcher(sDomain);
        if (m.find())
        {
            return m.group();
        }
        else
        {
            return sDomain;
        }
    }

    /**
     * pingIP地址
     *      (notice:会阻塞线程)
     * @param ipAddress
     * @param count ping次数
     * @param time 时间 unit s
     * @return
     */
    public static boolean pingIpAddressBlock(String ipAddress, int count, int time) {
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append("/system/bin/ping")
                    .append(" -c ")
                    .append(count)
                    .append(" -w ")
                    .append(time)
                    .append(" ")
                    .append(ipAddress);
            Process process = Runtime.getRuntime().exec(cmd.toString());
            int status = process.waitFor();
            if (status == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前网络连接状态
     *
     * @return 返回当前网络连接状态, 取值范围为： NETWORK_NONE = 0 // 未连接 ; NETWORK_WIFI = 1// 无线连接 ;NETWORK_ETH = 2 // 有线连接;
     */
    public static int getNetState() {
        if (BaseApplication.getInstance()!= null) {
            ConnectivityManager mConnMgr = (ConnectivityManager)BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr != null) {
                NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
                if (aActiveInfo != null) {
                    int type = aActiveInfo.getType();
                    Log.i(TAG, "NetStatusReceiver, type = " + type);
                    if (type == ConnectivityManager.TYPE_ETHERNET) {
                        return NETWORK_ETH;
                    } else if (type == ConnectivityManager.TYPE_WIFI) {
                        return NETWORK_WIFI;
                    } else {
                        return NETWORK_ETH;
                    }
                } else {
                    return NETWORK_NONE;
                }
            }
        }
        return NETWORK_NONE;
    }

    public static String getWifiMacAddress(Context context) {
        String macAddress = null;
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


    public static void mainNetStatusBg(int status, ImageView imageView){
        if (NETWORK_ETH == status) {
            imageView.setBackgroundResource(R.mipmap.network);
        } else if (NETWORK_WIFI == status) {
            imageView.setBackgroundResource(R.mipmap.wifi_on);
        } else {
            imageView.setBackgroundResource(R.mipmap.wifi_off);
        }
    }
}
