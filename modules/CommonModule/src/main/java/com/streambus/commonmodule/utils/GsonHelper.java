package com.streambus.commonmodule.utils;


import com.google.gson.Gson;
import com.streambus.basemodule.utils.SLog;

import java.lang.reflect.Type;


/**
 * <json公共解析库>
 *
 * @author caoyinfei
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class GsonHelper {

    private static String TAG = GsonHelper.class.getName();

    private static Gson gson = new Gson();

    /**
     * 把json string 转化成类对象
     *
     * @param str
     * @param t
     * @return
     */
    public static <T> T toType(String str, Class<T> t) {
        try {
            if (str != null && !"".equals(str.trim())) {
                T res = gson.fromJson(str.trim(), t);
                return res;
            }
        } catch (Exception e) {
            SLog.i(TAG, "exception:" + e.getMessage());
        }
        return null;
    }

    public static <T> T toType(String str, Type typeOfT) {
        try {
            if (str != null && !"".equals(str.trim())) {
                T res = gson.fromJson(str.trim(), typeOfT);
                return res;
            }
        } catch (Exception e) {
            SLog.i(TAG, "exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 把类对象转化成json string
     *
     * @param t
     * @return
     */
    public static <T> String toJson(T t) {
        return gson.toJson(t);
    }

}
