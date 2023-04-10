package com.streambus.basemodule.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.streambus.basemodule.networkmonitoring.annotation.NetType;
import com.streambus.basemodule.networkmonitoring.annotation.NetworkListener;
import com.streambus.basemodule.networkmonitoring.manager.NetworkManager;
import com.streambus.basemodule.networkmonitoring.utils.NetWorkUtils;
import com.streambus.basemodule.utils.SLog;
import com.trello.rxlifecycle3.LifecycleTransformer;
import com.trello.rxlifecycle3.components.support.RxFragment;

import org.simple.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2017/11/11
 * 描    述: 被后台回收后，重建时将从走所有步骤
 * 警    告：由于没有对页面还原进行设置，所以该 BaseFragment 不太适用于有还原需要的Fragment，今后有时间再考虑加入
 * 修订历史：
 * ================================================
 */
public abstract class BaseFragment extends RxFragment implements Handler.Callback {

    private static final String TAG = "BaseFragment";

    protected Context mContext;
    protected View mRootView;
    private boolean mIsUpdateLoaded;
    private ImageView imageView;

    /**
     * 绑定布局文件
     *
     * @return 布局文件Id
     */
    protected abstract int attachLayoutRes();

    /**
     * Dagger 注入
     */
    protected abstract void initViewModel();

    /**
     * 初始化视图控件
     *
     * @param savedInstanceState
     */
    protected abstract void initViews(@Nullable Bundle savedInstanceState);

    /**
     * 更新视图控件
     *
     * @param isRefresh
     */
    protected abstract void updateViews(boolean isRefresh);


    /**
     * EventBus注册 当需要使用时请返回true
     *
     * @return
     */
    public boolean useEventBus() {
        return false;
    }

    /**
     * 设置网络显示状态
     *
     * @param imageView View
     */
    public void netStatus(ImageView imageView) {
        this.imageView = imageView;
        NetWorkUtils.mainNetStatusBg(NetWorkUtils.getNetState(), imageView);
    }

    /**
     * 网络连接上监听
     */
    public void netStatueAuto() {
        if (imageView != null) netStatus(imageView);
    }

    /**
     * 网络断开监听
     */
    public void netStatueNon() {
        if (imageView != null) netStatus(imageView);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.w(TAG, "onAttach =>" + getClass() + ":" + hashCode());
        super.onAttach(context);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        Log.w(TAG, "onAttachFragment =>" + getClass() + ":" + hashCode());
        super.onAttachFragment(childFragment);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate =>" + getClass() + ":" + hashCode());
        super.onCreate(savedInstanceState);
        mContext = getContext();
        Log.i(TAG, "onCreate End=>" + getClass() + ":" + hashCode());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView =>" + getClass() + ":" + hashCode());
        if (mRootView == null) {
            mRootView = inflater.inflate(attachLayoutRes(), null);
            NetworkManager.getDefault().registerObserver(this);
            ButterKnife.bind(this, mRootView);
            if (useEventBus()) {
                EventBus.getDefault().register(this);
            }
            initViewModel();
            initViews(savedInstanceState);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated =>" + getClass() + ":" + hashCode());
        super.onActivityCreated(savedInstanceState);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated =>" + getClass() + ":" + hashCode());
    }


    @Override
    public void onStart() {
        Log.i(TAG, "onStart =>" + getClass() + ":" + hashCode());
        super.onStart();
        updateViews(mIsUpdateLoaded);
        mIsUpdateLoaded = true;
    }

    public void onResume() {
        Log.i(TAG, "onResume =>" + getClass() + ":" + hashCode());
        super.onResume();
    }

    public void onPause() {
        Log.i(TAG, "onPause =>" + getClass() + ":" + hashCode());
        super.onPause();
    }

    public void onStop() {
        Log.i(TAG, "onStop =>" + getClass() + ":" + hashCode());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.w(TAG, "onDestroyView =>" + getClass() + ":" + hashCode());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
       SLog.e(TAG, "onDestroy =>" + getClass() + ":" + hashCode());
        super.onDestroy();
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        imageView = null;
        NetworkManager.getDefault().unRegisterObserver(this);
    }

    @Override
    public void onDetach() {
       SLog.e(TAG, "onDetach =>" + getClass() + ":" + hashCode());
        super.onDetach();
    }

    public <T> LifecycleTransformer<T> bindToLife() {
        return this.<T>bindToLifecycle();
    }

    /**
     * 初始化 toolbar
     *
     * @param toolbar
     * @param homeAsUpEnable
     * @param title
     */
    protected void initToolbar(Toolbar toolbar, boolean homeAsUpEnable, String title) {
        ((BaseActivity) getActivity()).initToolbar(toolbar, homeAsUpEnable, title);
    }


    /**
     * 网络监听
     */
    @NetworkListener(type = NetType.AUTO)
    public void network(@NetType String type) {
        switch (type) {
            case NetType.AUTO:
                SLog.d(TAG, "网络链接上 mNetWorkChanfeReceiver=>true");
                netStatueAuto();
                break;
            case NetType.NONE:
                SLog.d(TAG, "网络断开mNetWorkChanfeReceiver=>false");
                netStatueNon();
                break;
            default:
                break;
        }
    }

    /************************************* - Handler-Helper - ****************************************/
    private Handler mHandler;

    protected final Handler handler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        return mHandler;
    }

    protected final boolean sendMessageDelayed(int what, int arg1, int arg2, @Nullable Object obj, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        Message msg = mHandler.obtainMessage(what);
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    protected final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    protected final boolean sendEmptyMessage(int what) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        return mHandler.sendEmptyMessage(what);
    }

    protected final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), this);
        }
        return mHandler.sendEmptyMessageAtTime(what, uptimeMillis);
    }

    protected final void removeMessages(int what) {
        if (mHandler != null) {
            mHandler.removeMessages(what);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


}
