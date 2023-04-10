package com.streambus.commonmodule.umeng;

import android.content.Context;

import com.streambus.basemodule.utils.SLog;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PushHelper {
    private static final String TAG = PushHelper.class.getSimpleName();
    private static PushHelper pushHelper;
    public boolean isInit;
    private PushAgent pushAgent;
    private ConcurrentHashMap<String, IMessageCallback> messageCallback = new ConcurrentHashMap<>();

    public static PushHelper getInstance() {
        synchronized (TAG) {
            if (null == pushHelper) {
                pushHelper = new PushHelper();
            }
        }
        return pushHelper;
    }

    private PushHelper() {
    }

    /**
     * 初始化UMPush.考虑冷启动速度等，在子线程做初始化
     * @param mContext
     */
    public void preInit(Context mContext) {
        SLog.i(TAG, "preInit, isInit=" + isInit);
        if (!isInit) {
            init(mContext);
        }
    }

    private void init(Context mContext) {
        pushAgent = PushAgent.getInstance(mContext.getApplicationContext());
        pushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                //长度为44位，不能定制和修改。同一台设备上不同应用对应的deviceToken不一样。获取deviceToken的值后，可进行消息推送测试！
                SLog.e(TAG, "register, onSuccess: deviceToken=" + deviceToken);
                isInit = true;
            }

            @Override
            public void onFailure(String s, String s1) {
                SLog.e(TAG, "register, onFailure: s=" + s + " ,s1=" + s1);
            }
        });
        pushAgent.setMessageHandler(messageHandler);
    }

    private UmengMessageHandler messageHandler = new UmengMessageHandler() {
        @Override
        public void handleMessage(Context context, UMessage uMessage) {
            SLog.e(TAG, "handleMessage, uMessage=" + uMessage);
            if (uMessage.builder_id == 1) {
                //自定义消息通知
                handleIMessage(uMessage);
            } else {
                //默认为0，若填写的builder_id并不存在，也使用默认。
                super.handleMessage(context, uMessage);
            }
        }
    };

    /**
     * 在所有的Activity 的onCreate 方法或在应用的BaseActivity的onCreate方法中添加：
     * @return
     */
    public boolean onAppStart() {
        if (null != pushAgent) {
            pushAgent.onAppStart();
            return true;
        } else {
            return false;
        }
    }

    public void addMessageCallback(String priority, IMessageCallback callback) {
        messageCallback.put(priority, callback);
    }

    /**
     * 自定义处理umeng push消息
     * @param uMessage
     */
    private void handleIMessage(UMessage uMessage) {
        for (Map.Entry<String, IMessageCallback> entry : messageCallback.entrySet()) {
            if (entry.getValue().handleIMessage(uMessage)) {
                break;
            }
        }
    }

    public void release() {
        if (null != pushAgent) {
            messageCallback.clear();
            pushAgent = null;
            isInit = false;
            pushHelper = null;
        }
    }
}
