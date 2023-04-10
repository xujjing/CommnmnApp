package com.streambus.commonmodule.login;

import com.streambus.basemodule.utils.SLog;

import java.lang.reflect.Method;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class PlatformUtil {
    private static final String TAG = "PlatformUtil";

    public static String getIdCipher() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            String idCipher = ((String) method.invoke(clazz.newInstance(), "ubootenv.var.flag2")).trim();
            SLog.i(TAG, "getS905ID idCipher = " + idCipher);
//            testCrc(idCipher);
            return idCipher;
        } catch (Exception e) {
            SLog.e(TAG, "get id Exception:", e);
        }
        return "";
    }


//    public static void testCrc(String idCipher) {
//        CRCSNMAC crcsnmac = new CRCSNMAC(idCipher);
//        if (crcsnmac.IsCrcOK()) { //必须先运算
//            String macId = crcsnmac.getmacString(); //获取macId
//            String sn = crcsnmac.getSnString();  //获取sn
//            SLog.i(TAG, "testCrc macId=" + macId + "   sn=" + sn);
//        }
//    }


    public static boolean is_ATV_Platform() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            String platform_id = (String) method.invoke(clazz.newInstance(), "platform_id");
            String firmware = (String) method.invoke(clazz.newInstance(), "ro.product.firmware");
            SLog.i(TAG, "is_ATV_Platform  platform_id=" + platform_id + "  firmware=" + firmware);
            if (platform_id.equals("hx_ott1_s905x3") && firmware.startsWith("atv_s905x3_ott_atv")) {
                return true;
            }
        } catch (Exception e) {
            SLog.w(TAG, "is_ATV_Platform", e);
        }
        return false;
    }



}
