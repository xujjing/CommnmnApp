package com.streambus.commonmodule.dialog;

import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.streambus.basemodule.base.BaseDialogFragment;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.R;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.login.PlatformUtil;
import com.streambus.requestapi.LoginManager;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/28
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RemindManager {

    private static RemindManager INSTANCE;
    private static final String TAG = "RemindManager";
    public MutableLiveData<Boolean> updateNetWorkStatus = new MutableLiveData<>(false);
    private Application mContext;
    private IPayAction mPayAction;

    private RemindManager(Application context) {
        mContext = context;
        receiverConnectivity(context);
    }

    private void receiverConnectivity(Application context) {
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();
                    if (info != null && info.isAvailable()) {
                        updateNetWorkStatus.setValue(true);
                    } else {
                        updateNetWorkStatus.setValue(false);
                    }
                }
            }
        }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private boolean mIsShowCheckConnectivity;
    public boolean checkConnectivity(FragmentManager fragmentManager, boolean cancelable, ConnectivityAction action){
        SLog.d(TAG, "checkConnectivity updateNetWorkStatus= " + updateNetWorkStatus.getValue() + "  mIsShowCheckConnectivity=" +mIsShowCheckConnectivity);
        if (updateNetWorkStatus.getValue()) {
            return true;
        }
        if (mIsShowCheckConnectivity) return false;
        RemindDialog remindDialog = RemindDialog.newInstance(new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }, new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                dialog.dismiss();
                action.onCancel();
            }
        });
        remindDialog.setCancelable(cancelable);
        remindDialog.setOnDismissListener(new BaseDialogFragment.OnDismissListener() {
            private Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        remindDialog.dismiss();
                        action.availableConnect();
                    }
                }
            };

            {
                updateNetWorkStatus.observeForever(observer);
            }

            @Override
            public void onDismiss(DialogFragment dialogFragment) {
                updateNetWorkStatus.removeObserver(observer);
                mIsShowCheckConnectivity = false;
            }

        });
        SLog.d(TAG,"checkConnectivity remindDialog show");
        remindDialog.show(mContext.getString(R.string.dialog_network_invalid), fragmentManager);
        mIsShowCheckConnectivity = true;
        return false;
    }


    private boolean mIsShowCheckLoginByDevice;
    public boolean checkLoginByDevice(FragmentManager fragmentManager, Bundle arguments) {
        int loginType = arguments.getInt(Constants.KEY_LOGIN_METHOD_TYPE, 0);
        if (loginType != LoginManager.LOGIN_TYPE_loginByDeviceSN) {
            return false;
        }
        if (mIsShowCheckLoginByDevice) return true;
        RemindDialog remindDialog = RemindDialog.newInstance(new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                if (!checkConnectivity(fragmentManager, false, new ConnectivityAction() {
                    @Override public void onCancel() {}
                    @Override
                    public void availableConnect() {
                        onClick(dialog, v);
                    }
                })){return;}
                dialog.dismiss();
                MyAppLogin.getInstance().startLoginByProxy(MyAppLogin.getInstance().loginByDeviceSn(PlatformUtil.getIdCipher()));
            }
        });
        remindDialog.setCancelable(false);
        remindDialog.setOnDismissListener(dialog -> mIsShowCheckLoginByDevice = false);
        String errorInfo = "";
        String action = arguments.getString(Constants.KEY_ACTION);
        switch (action) {
            case MyAppLogin.ACTION_LOGIN_FAILED:
                Exception e = (Exception) arguments.getSerializable(Constants.KEY_DATA);
                errorInfo = mContext.getString(R.string.dialog_connect_server_failed, e.getMessage());
                break;
            case MyAppLogin.ACTION_LOGIN_ERROR:
                int result = arguments.getInt(Constants.KEY_DATA);
                errorInfo = mContext.getString(R.string.dialog_connect_server_failed, "e|" + result);
                break;
            case MyAppLogin.ACTION_ExitByServer:
                int cmd = arguments.getInt(Constants.KEY_DATA);
                errorInfo = mContext.getString(R.string.dialog_connect_server_failed, "s|" + cmd);
                break;
            default:
                break;
        }
        remindDialog.show(errorInfo, fragmentManager);
        mIsShowCheckLoginByDevice = true;
        return true;
    }

    public interface ConnectivityAction{
        public void onCancel();
        public  void availableConnect();
    }

    public void receiverPayAction(IPayAction payAction){
        mPayAction = payAction;
    }

    public static void setup(Application context) {
        INSTANCE = new RemindManager(context);
    }

    public static RemindManager getInstance() {
        return INSTANCE;
    }

    private boolean mIsShowCheckValidity;

    public boolean isValidity() {
        if (LoginManager.LOGIN_TYPE_loginByDeviceSN == Constants.SUBJECT_LOGIN_TYPE.getValue() ||
                LoginManager.LOGIN_TYPE_loginByQrcodeToken == Constants.SUBJECT_LOGIN_TYPE.getValue()) {
            return true;
        }
        Map.Entry<Integer, Integer> entry = MyAppLogin.getInstance().getValidityDay();
        boolean hasAuth = (entry.getKey() == MyAppLogin.AUTHORITY_TYPE_Vod || entry.getKey() == MyAppLogin.AUTHORITY_TYPE_LiveAndVod);
        if (hasAuth && entry.getValue() > 0) {
            return true;
        }
        return false;
    }

    public boolean checkValidity(FragmentManager fragmentManager) {
        if (LoginManager.LOGIN_TYPE_loginByDeviceSN == Constants.SUBJECT_LOGIN_TYPE.getValue() ||
            LoginManager.LOGIN_TYPE_loginByQrcodeToken == Constants.SUBJECT_LOGIN_TYPE.getValue()) {
            return true;
        }
        Map.Entry<Integer, Integer> entry = MyAppLogin.getInstance().getValidityDay();
        boolean hasAuth = (entry.getKey() == MyAppLogin.AUTHORITY_TYPE_Vod || entry.getKey() == MyAppLogin.AUTHORITY_TYPE_LiveAndVod);
        if (hasAuth && entry.getValue() > 0) {
            return true;
        }
        if (mIsShowCheckValidity) return false;
        RemindDialog remindDialog = RemindDialog.newInstance(new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                dialog.dismiss();
                mPayAction.doPayAction();
            }
        }, new RemindDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, View v) {
                dialog.dismiss();
            }
        });
        remindDialog.setCancelable(true);
        remindDialog.setOnDismissListener(dialog -> mIsShowCheckValidity = false);
        remindDialog.show(mContext.getString(hasAuth ? R.string.dialog_server_expired : R.string.dialog_server_invalid_vod), fragmentManager);
        mIsShowCheckValidity = true;
        return false;
    }

    public interface IPayAction{
        void doPayAction();
    }

}
