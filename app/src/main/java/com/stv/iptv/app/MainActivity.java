package com.stv.iptv.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.iptv.aovivo.aovod.CallbackListener;
import com.iptv.aovivo.aovod.InterfaceAidl;
import com.streambus.basemodule.base.BaseActivity;
import com.streambus.basemodule.utils.MyToast;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.CommonApplication;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.bean.ChannelVodBean;
import com.streambus.commonmodule.dialog.RemindManager;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.usermodule.module.account.UserAccountFragment;
import com.streambus.usermodule.module.login.UserLoginFragment;
import com.streambus.usermodule.module.setting.UserSettingFragment;
import com.streambus.vodmodule.view.channel.VodChannelFragment;
import com.streambus.vodmodule.view.detail.VodDetailFragment;
import com.streambus.vodmodule.view.download.VodDownloadFragment;
import com.streambus.vodmodule.view.download.VodLocalPlayFragment;
import com.streambus.vodmodule.view.dseries.VodSeriesMovieFragment;
import com.streambus.vodmodule.view.play.VodPlayFragment;
import com.streambus.vodmodule.view.search.VodSearchFragment;
import com.streambus.vodmodule.view.trailer.VodTrailerPlayFragment;

import org.json.JSONObject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private NavController mNavController;

    @Override
    protected int attachLayoutRes() {
        String s = new String();
        ThreadLocal<String> local = new ThreadLocal<>();
        local.set("jii123");
        String s1 = local.get();
        local.remove();

        CallbackListener li = new CallbackListener.Default(){
            @Override
            public void onServiceConnected() throws RemoteException {

            }
            @Override
            public void sendMsgToClient(String msg) throws RemoteException {

            }
        };

        new ServiceConnection() {
            InterfaceAidl aidl;
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    Messenger messenger = new Messenger(service);
                    Message message = Message.obtain(null, 1);
                    message.setData(new Bundle());

                    Messenger replyTo = new Messenger(new Handler(Looper.getMainLooper(), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            if (msg.what == 1) {
                                msg.getData();
                            }
                            return false;
                        }
                    }));
                    message.replyTo = replyTo;
                    messenger.send(message);

//                    replyTo.getBinder();
//                    message.replyTo.send(Message.obtain(null,1).setData());

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                aidl = InterfaceAidl.Stub.asInterface(service);
                try {
                    service.linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            aidl.asBinder().unlinkToDeath(this, 0);
                            aidl = null;
                            //重新绑定service
                            bindService(intent, this , Context.BIND_AUTO_CREATE);
                        }
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Parcel obtain = Parcel.obtain();
        obtain.writeInterfaceToken("");
        obtain.writeString("");

        Intent intent = new Intent();
        intent.setClassName();
        intent.setComponent(new ComponentName());
        Message message = Message.obtain();
        Messenger messenger = message.replyTo;
        messenger.send();

        message.replyTo = new Messenger(handler);
        message.replyTo = new Messenger();

        if (!CommonApplication.isAFTER_DONE()) {
            //在VIVO 7.1.2系统, 按电源键，会杀掉当前Activity,在ActivityOnDestroy时KillApp;
            //接着AM 新进程重启Activity，由于Application没有初始化,这里也KillApp; -> 新进程重启Activity
            //android.os.Process.killProcess(android.os.Process.myPid());
            return 0;
        }
        setWindowFull();
        return R.layout.activity_main;
    }

    private void setWindowFull() {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                |View.SYSTEM_UI_FLAG_FULLSCREEN//全屏
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;//隐藏导航栏
        if (Build.VERSION.SDK_INT>=19){
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }else{
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //关闭屏保
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setWindowFull();
    }

    @Override
    protected void initViewModel() {
        RemindManager.getInstance().receiverPayAction(new RemindManager.IPayAction() {
            @Override
            public void doPayAction() {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_ACTION, Constants.ACTION_BUY);
                navigateFragment(UserAccountFragment.class, bundle);
            }
        });
        RemindManager.getInstance().updateNetWorkStatus.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean) {
                    RemindManager.getInstance().checkConnectivity(getSupportFragmentManager(), true, new RemindManager.ConnectivityAction() {
                        @Override
                        public void onCancel() {
                        }
                        @Override
                        public void availableConnect() {
                        }
                    });
                }
            }
        });
        MyAppLogin.getInstance().updateLoginAudioState.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Bundle arguments = MyAppLogin.getInstance().getLoginErrorArguments();
                if (arguments != null) {
                    if (!RemindManager.getInstance().checkLoginByDevice(getSupportFragmentManager(), arguments)) {
                        if (mNavController.getCurrentDestination().getId() == R.id.user_login_dest) {
                            return;
                        }
                        navigateFragment(UserLoginFragment.class, arguments);
                    }
                }
            }
        });
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mNavController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.my_nav_host_fragment));
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            long lastTimeMillis;
            @Override
            public void handleOnBackPressed() {
                long uptimeMillis = SystemClock.uptimeMillis();
                if (uptimeMillis - lastTimeMillis > 1500) {
                    lastTimeMillis = uptimeMillis;
                    MyToast.makeText(MainActivity.this, getResources().getString(R.string.press_agin), Toast.LENGTH_SHORT).show();
                } else {
                    remove();
                    onBackPressed();
                }
            }
        });
    }

    @Override
    protected void updateViews(boolean isRefresh) {
        handleUpdate(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mNavController.popBackStack(R.id.home_dest, false);
        handleUpdate(intent);
    }

    private void handleUpdate(Intent intent) {
        String data = intent.getStringExtra("data");
        SLog.i(TAG,"updateViews data=>" + data);
        if (!TextUtils.isEmpty(data)) {
            handleEsData(data);
        }
        if (!MyAppLogin.getInstance().isAutoLogin()) {
            navigateFragment(UserLoginFragment.class, null);
        }
    }

    private void handleEsData(String data) {
        try {
            if (data.startsWith("Launch|")) {
                JSONObject json = new JSONObject(data.substring("Launch|".length()));
                ChannelVodBean bean = new ChannelVodBean();
                bean.setId(json.getString("id"));//电影
                bean.setName(json.getString("name"));
                bean.setImg(json.getString("img"));
                bean.setType(json.getString("type"));
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_ACTION, Constants.ACTION_LAUNCHER_PLAY);
                bundle.putSerializable(Constants.KEY_VOD_CHANNEL, bean);
                if (ChannelVodBean.VIDEO_TYPE_SERIES_MOVIE.equalsIgnoreCase(bean.getType()) || ChannelVodBean.VIDEO_TYPE_TOPIC.equalsIgnoreCase(bean.getType())) {
                    navigateFragment(VodSeriesMovieFragment.class, bundle);
                } else {
                    navigateFragment(VodDetailFragment.class, bundle);
                }
            }
        } catch (Exception e) {
            SLog.e(TAG, "updateViews handle data", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return mNavController.navigateUp();
    }

    public void navigatePopBackStack() {
        mNavController.popBackStack();
    }

    public void navigateFragment(Class<? extends Fragment> clazz, final Bundle args) {
        SLog.i(TAG,"navigateFragment  clazz=" + clazz.getSimpleName());
        if (clazz == VodSearchFragment.class) {
            SLog.i(TAG,"navigateFragment  VodSearchFragment");
            mNavController.navigate(R.id.vod_search_dest, args);
            return;
        }
        if (clazz == VodDownloadFragment.class) {
            SLog.i(TAG,"navigateFragment  VodDownloadFragment");
            mNavController.navigate(R.id.vod_download_dest, args);
            return;
        }
        if (clazz == VodLocalPlayFragment.class) {
            SLog.i(TAG,"navigateFragment  VodLocalPlayFragment");
            mNavController.navigate(R.id.vod_local_play_dest, args);
            return;
        }
        if (clazz == VodChannelFragment.class) {
            mNavController.navigate(R.id.vod_channel_dest, args);
            return;
        }
        if (clazz == VodSeriesMovieFragment.class) {
            boolean b = mNavController.popBackStack(R.id.vod_series_movie_dest, true);
            SLog.d(TAG, "navigateFragment popBackStack VodSeriesMovieFragment b=>" + b);
            mNavController.navigate(R.id.vod_series_movie_dest, args);
            return;
        }
        if (clazz == VodDetailFragment.class) {
            boolean b = mNavController.popBackStack(R.id.vod_detail_dest, true);
            SLog.d(TAG, "navigateFragment popBackStack VodDetailFragment b=>" + b);
            mNavController.navigate(R.id.vod_detail_dest, args);
            return;
        }
        if (clazz == VodTrailerPlayFragment.class) {
            SLog.i(TAG,"navigateFragment  VodTrailerPlayFragment");
            mNavController.navigate(R.id.vod_trailer_play_dest, args);
            return;
        }
        if (clazz == VodPlayFragment.class) {
            final NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out)
                    .build();
            mNavController.navigate(R.id.vod_play_dest, args, navOptions);
            return;
        }
        if (clazz == UserAccountFragment.class) {
            mNavController.navigate(R.id.user_account_dest, args);
            return;
        }
        if (clazz == UserLoginFragment.class) {
            mNavController.navigate(R.id.user_login_dest, args);
            return;
        }
        if (clazz == UserSettingFragment.class) {
            mNavController.navigate(R.id.user_setting_dest, args);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SLog.d(TAG,"onDestroy >> ");
        ((CommonApplication)getApplication()).killApk();
    }
}
