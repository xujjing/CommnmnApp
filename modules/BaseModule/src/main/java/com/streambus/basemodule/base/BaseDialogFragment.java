package com.streambus.basemodule.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.streambus.basemodule.R;
import com.streambus.basemodule.networkmonitoring.annotation.NetType;
import com.streambus.basemodule.networkmonitoring.annotation.NetworkListener;
import com.streambus.basemodule.networkmonitoring.manager.NetworkManager;
import com.streambus.basemodule.networkmonitoring.utils.NetWorkUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.widget.FullDialog;
import com.streambus.basemodule.widget.LoadingView;
import com.trello.rxlifecycle3.LifecycleTransformer;
import com.trello.rxlifecycle3.components.support.RxAppCompatDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import butterknife.ButterKnife;

import static android.service.controls.ControlsProviderService.TAG;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/4/12
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class BaseDialogFragment extends RxAppCompatDialogFragment implements Handler.Callback {

    protected Context mContext;
    protected ViewGroup mRootView;
    protected Dialog mDialog;
    private ImageView imageView;
    private OnShowListener mOnShowListener;
    private OnDismissListener mOnDismissListener;
    private OnBackPressedListener mOnBackPressedListener;

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
     * 设置网络显示状态
     * @param imageView View
     */
    public void netStatus(ImageView imageView){
        this.imageView=imageView;
        NetWorkUtils.mainNetStatusBg(NetWorkUtils.getNetState(),imageView);
    }

    /**
     * 网络连接上监听
     */
    public void netStatueAuto(){
        if(imageView!=null)netStatus(imageView);
    }

    /**
     * 网络断开监听
     */
    public void netStatueNon(){
        if(imageView!=null)netStatus(imageView);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (ViewGroup) inflater.inflate(attachLayoutRes(), null);
            ButterKnife.bind(this, mRootView);
            NetworkManager.getDefault().registerObserver(this);
            initViewModel();
            initViews(savedInstanceState);
            SLog.d(TAG,"baseDialogFragment 执行了 attachLayoutRes");
        }
        return mRootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDialog == null) {
            mDialog = new FullDialog(getActivity(), R.style.Dialog_Fullscreen){
                @Override
                public void onBackPressed() {
                    if (mOnBackPressedListener == null || !mOnBackPressedListener.onBackPressed()) {
                        super.onBackPressed();
                    }
                }
            };
        }
        return mDialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateViews(false);
        if (mOnShowListener != null) {
            mOnShowListener.onShow(BaseDialogFragment.this);
        }
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(BaseDialogFragment.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ViewGroup)mRootView.getParent()).removeView(mRootView);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        imageView=null;
        NetworkManager.getDefault().unRegisterObserver(this);
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

    protected final Handler hander() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(),this);
        }
        return mHandler;
    }

    protected final boolean sendEmptyMessage(int what) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(),this);
        }
        return mHandler.sendEmptyMessage(what);
    }

    protected final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(),this);
        }
        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(),this);
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


    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (mDialog == null || !mDialog.isShowing()) {
            super.show(manager, tag);
        }
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        mOnShowListener = onShowListener;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        mOnBackPressedListener = onBackPressedListener;
    }

    public interface OnShowListener{
        void onShow(DialogFragment dialogFragment);
    }

    public interface OnDismissListener{
        void onDismiss(DialogFragment dialogFragment);
    }

    public interface OnBackPressedListener{
        boolean onBackPressed();
    }
}