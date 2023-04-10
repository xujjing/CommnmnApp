package com.streambus.commonmodule.umeng;

import java.util.HashMap;
import java.util.Map;

public class UMConstance {
    public static final String UMENG_APPKEY = "UMENG_APPKEY";
    public static final String UMENG_CHANNEL = "UMENG_CHANNEL";
    public static final String MESSAGE_SECRET = "MESSAGE_SECRET";

    public static Map<String, Object> constructMap(String... params) {
        HashMap<String, Object> map = new HashMap();
        for (int i = 0; i < params.length; i += 2) {
            map.put(params[i], params[i+1]);
        }
        return map;
    }

    //登录界面
    public static final String EVENT_LOGIN_ACTIVITY = "event_login_activity";
    //==================================================
    //注册界面
    public static final String EVENT_REGISTER_ACTIVITY = "event_register_activity";
    //注册点击_注册界面
    public static final String EVENT_REGISTER_ONCLICK = "event_register_onclick";
    //注册成功回调_不含邮件激活
    public static final String EVENT_REGISTER_SUCCESS = "event_register_success";
    ///===================================================
    //充值界面
    public static final String EVENT_RECHARGE_ACTIVITY = "event_recharge_activity";
    //------------------------------------------------------
    //web充值界面
    public static final String EVENT_WEB_RECHARGE_ACTIVITY = "event_web_recharge_activity";
    //web端支付成功回调
    public static final String EVENT_WEB_RECHARGE_SUCCESS = "event_web_recharge_success";
    //------------------------------------------------------
    //code充值界面
    public static final String EVENT_CODE_RECHARGE_ACTIVITY = "event_code_recharge_activity";
    //code支付成功回调
    public static final String EVENT_CODE_RECHARGE_SUCCESS = "event_code_recharge_success";
    //------------------------------------------------------
    //盒子端buyonline界面
    public static final String EVENT_BUYONLINE_ACTIVITY =  "event_buyonline_activity";
    //盒子端buyonline界面email确认点击
    public static final String EVENT_BUYONLINE_EMAIL_CONFIRM =  "event_buyonline_email_click";
    //盒子端buyonline购买成功回调
    public static final String EVENT_BUYONLINE_SUCCESS = "event_buyonline_success";
    //======================================================
}
