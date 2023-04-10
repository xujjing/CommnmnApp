//package com.streambus.commonmodule.utils;
//
//import android.content.Context;
//import android.text.TextUtils;
//
//import com.streambus.basemodule.utils.PreferencesUtils;
//import com.streambus.basemodule.utils.SLog;
//import com.streambus.commonmodule.Constants;
//
//import java.io.File;
//import java.io.FileOutputStream;
//
//
///**
// * Created by Administrator on 2017/8/31.
// */
//
//public class LoginUtil {
//
//    public static final String ID_OPEN_WIFI = "OPEN_WIFI";
//    public static final String ID_ERROR = "ID_ERROR";
//
//
//    public static final String DEFAULT_CODE = "65286619";
//
//    private static final String TAG = "LoginUtil";
//
//    /**
//     * 先首先获取规定ID(自产盒子sn,rk平台有线 mac),后获取wifi mac
//     *
//     * @return
//     */
//    public static String getDeviceId(Context context) {
//        String mac = PreferencesUtils.get(Constants.KEY_LOGIN_MAC_ID, "");
//        if (!TextUtils.isEmpty(mac)) {
//            SLog.i(TAG, "getDeviceId: DEVICEID=>" + mac);
//            return mac;
//        }
//        if (PlatformUtils.isYoostarPlatform() || PlatformUtils.is_mx_s2_Platform()) {
//            String id = PlatformUtils.getID();
//            if (id != null) {
//                mac = id.trim();
//            }
//            if (!TextUtils.isEmpty(mac) && !"null".equalsIgnoreCase(mac) && mac.length() >= 20) {
//                try {
//                    CRCSNMAC csm = new CRCSNMAC(mac);
//                    if (csm != null) {
//                        csm.IsCrcOK();
//                        mac = csm.getmacString();
//                    }
//                } catch (Exception e) {
//                    mac = "";
//                }
//                SLog.i(TAG, "yoostar box getDeviceId: " + mac);
//            }
//            if (TextUtils.isEmpty(mac) || "null".equalsIgnoreCase(mac)) {
//                mac = ID_ERROR;
//                SLog.i(TAG, "yoostar box get id error: ");
//            }
//        } else {//第三方平台
//            mac = getThirdMacId(context);
//            SLog.i(TAG, "other box getDeviceId: " + mac);
//        }
//
//        if (!TextUtils.isEmpty(mac) && !ID_OPEN_WIFI.equals(mac) && !ID_ERROR.equals(mac)) {
//            mac = mac.toUpperCase();
//        }
//        SLog.i(TAG, "finally getDeviceId: " + mac);
//        return mac;
//    }
//
//
//    private static String getThirdMacId(Context context) {
//        String mac = SystemUtil.getWifiMacAddress(context);
//        mac = dealMacStr(mac);
//        if (TextUtils.isEmpty(mac)) {
//            mac = ID_OPEN_WIFI;
//        }
//        return mac;
//    }
//
//
//    public static String dealMacStr(String mac) {
//        if (mac != null && !mac.equals("") && mac.length() == 17) {
//            if (mac.contains(":")) {
//                mac = mac.replace(":", "");
//            } else if (mac.contains(".")) {
//                mac = mac.replace(".", "");
//            }
//            long lmac = Long.parseLong(mac, 16);
//            String bit28 = NumberChange.toCustomNumericString(lmac, 28);
//            String reMac = String.valueOf(bit28);
//            if (reMac.length() >= 10) {
//                mac = reMac.substring(0, 10);
//            } else {
//                while (reMac.length() < 10) {
//                    reMac = reMac + "0";
//                }
//                mac = reMac;
//            }
//            mac = mac.toUpperCase();
//        }
//        return mac;
//    }
//
//    public static String getLoginToken() {
//        return PreferencesUtils.get(Constants.KEY_LOGIN_TOKEN, "");
//    }
//
//    public static String getLoginCode() {
//        String code = PreferencesUtils.get(Constants.KEY_LOGIN_CODE, "");
//        if (code != null) {
//            SLog.i(TAG, "getLoginCode: Code=>" + code);
//            return code;
//        }
//        if (PlatformUtils.isHighSystem()) {
//            //南美系统，先获取系统code,然后获取应用内Shared的code
//            if (PlatformUtils.isHighPlatform()) {
//                code = PlatformUtils.getS905Code();
//                //系统没有code,读应用内保存的code
//                if (TextUtils.isEmpty(code)) {
//                    code = DEFAULT_CODE;
//                }
//            } else if (PlatformUtils.is_S805_Platform() || PlatformUtils.is_mx_s2_Platform()) {
//                code = PlatformUtils.get805Code();
//                if (TextUtils.isEmpty(code)) {
//                    code = DEFAULT_CODE;
//                }
//            }
//            SLog.i(TAG, "SuperTV System");
//        }
//        if (!TextUtils.isEmpty(code)) {
//            code = code.toUpperCase();
//        }
//        SLog.i(TAG, "finally getLoginCode: " + code);
//        return code;
//    }
//
//    public static void saveLogin(String mac, String code, String token) {
//        PreferencesUtils.put(Constants.KEY_LOGIN_MAC_ID, mac);
//        PreferencesUtils.put(Constants.KEY_LOGIN_CODE, code);
//        PreferencesUtils.put(Constants.KEY_LOGIN_TOKEN, token);
//        if (PlatformUtils.isHighPlatform()) {
//            saveS905Code(code);
//        } else if (PlatformUtils.is_S805_Platform() || PlatformUtils.is_mx_s2_Platform()) {
//            saveS805Code(code);
//        }
//    }
//
//    private static void saveS805Code(String storeCode) {
//        try {
//            File files = new File("/sys/class/aml_keys/aml_keys/version");
//            FileOutputStream out = new FileOutputStream(files);
//            byte[] version = "auto3".getBytes();
//            out.write(version);
//            out.close();
//            files = new File("/sys/class/aml_keys/aml_keys/key_name");
//            out = new FileOutputStream(files);
//            byte[] name = (PlatformUtils.CODE_SAVE_FLAG).getBytes();
//            out.write(name);
//            out.close();
//            files = new File("/sys/class/aml_keys/aml_keys/key_write");
//            byte[] key_str = storeCode.getBytes();
//            out = new FileOutputStream(files);
//            out.write(key_str);
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void saveS905Code(String code) {
//        try {
//            File files = new File("/sys/class/unifykeys/name");
//            FileOutputStream out = new FileOutputStream(files);
//            byte[] name = (PlatformUtils.CODE_SAVE_FLAG).getBytes();
//            out.write(name);
//            out.close();
//            files = new File("/sys/class/unifykeys/write");
//            byte[] key_str = code.getBytes();
//            out = new FileOutputStream(files);
//            out.write(key_str);
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
