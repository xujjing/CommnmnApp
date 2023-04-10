package com.streambus.basemodule.networkmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.streambus.basemodule.networkmonitoring.annotation.NetType;
import com.streambus.basemodule.networkmonitoring.annotation.NetworkListener;
import com.streambus.basemodule.networkmonitoring.manager.MethodManager;
import com.streambus.basemodule.networkmonitoring.manager.NetworkManager;
import com.streambus.basemodule.networkmonitoring.utils.Constants;
import com.streambus.basemodule.networkmonitoring.utils.NetWorkUtils;
import com.streambus.basemodule.utils.SLog;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class NetStateReceiver extends BroadcastReceiver {

    Map<Object, List<MethodManager>> mNetworkList = new HashMap<>();
    List<MethodManager> mMethodList = new ArrayList<>();
    @NetType
    private String type;


    public NetStateReceiver() {
        this.type = NetType.NONE;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            SLog.e(Constants.TAG, "广播异常了");
            return;
        }
        if (intent.getAction().equalsIgnoreCase(Constants.ANDROID_NET_CHANGE_ACTION)) {
            SLog.i(Constants.TAG, "网络状态变化了");
            if (!NetWorkUtils.isNetWorkAvailable()) {
                post(NetType.NONE);
                SLog.i(Constants.TAG, "网络断开");
            } else {
                SLog.i(Constants.TAG, "网络链接");
                post(NetType.AUTO);
            }
        }
    }

    /**
     * @param register
     */
    public void registerObserver(Object register) {
        //获取当前Activity or Fragment中所有的网络监听注解方法
        mMethodList = mNetworkList.get(register);
        if (mMethodList == null) {//说明已经注册过了
            mMethodList = findAnnotationMethod(register);
            mNetworkList.put(register, mMethodList);
        }
    }

    /**
     * @param netType
     */
    private void post(@NetType String netType) {
        Set<Object> set = mNetworkList.keySet();
        for (Object o : set) {
            List<MethodManager> methodManagerList = mNetworkList.get(o);
            for (MethodManager manager : methodManagerList) {
                if (manager.getType().isAssignableFrom(netType.getClass())) {//如果注解上的参数和当前网络状态相同

                    switch (manager.getNetType()) {
                        case NetType.AUTO:
                            invoke(manager, o, netType);//反射运行方法
                            break;
                        case NetType.CMNET:
                            if (netType == NetType.CMNET || netType == NetType.NONE) {
                                invoke(manager, o, netType);
                            }
                            break;
                        case NetType.CMWAP:
                            if (netType == NetType.CMWAP || netType == NetType.NONE) {
                                invoke(manager, o, netType);
                            }
                            break;
                        case NetType.WIFI:
                            if (netType == NetType.WIFI || netType == NetType.NONE) {
                                invoke(manager, o, netType);
                            }
                            break;
                        case NetType.NONE:
                            invoke(manager, o, netType);
                            break;
                    }
                }
            }
        }

    }

    /**
     * @param manager 方法管理类
     * @param o       方法所有者（activity/Fragment）
     * @param netType 网络类型参数
     */
    private void invoke(MethodManager manager, Object o, String netType) {
        Method executeMethod = manager.getMethod();
        try {
            executeMethod.invoke(o, netType);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param register
     * @return MethodList 网络监听注解方法数组
     */
    private List<MethodManager> findAnnotationMethod(Object register) {
        List<MethodManager> methodList = new ArrayList<>();
        Class<?> clazz = register.getClass();
        Method[] method = clazz.getMethods();

        for (Method m : method) {//遍历方法
            //找出所有注解方法
            NetworkListener annotation = m.getAnnotation(NetworkListener.class);
            if (annotation == null) {
                continue;
            }
            //判断返回类型
            Type genericReturnType = m.getGenericReturnType();
            if (!"void".equals(genericReturnType.toString())) {
                throw new RuntimeException(m.getName() + "返回类型必须是void");
            }

            //参数校验
            Class<?>[] parameterTypes = m.getParameterTypes();
            SLog.i("m,name", m.getParameterTypes().length + "");
            if (parameterTypes.length != 1) {
                throw new RuntimeException(m.getName() + "返回参数只有一个");
            }

            MethodManager methodManager = new MethodManager(parameterTypes[0], annotation.type(), m);
            methodList.add(methodManager);

        }
        return methodList;
    }

    /**
     * @param register
     */
    public void unRegisterObserver(Object register) {
        if (!mNetworkList.isEmpty()) {//说明有广播被注册过
            mNetworkList.remove(register);
        }
        SLog.i(Constants.TAG, register.getClass().getName() + "注销成功了");
    }

    public void unRegisterAllObserver() {
        if (!mNetworkList.isEmpty()) {//说明有广播被注册过
            mNetworkList.clear();
        }
        NetworkManager.getDefault().logout();//注销
    }
}
