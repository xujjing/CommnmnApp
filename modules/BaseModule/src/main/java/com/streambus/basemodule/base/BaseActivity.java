package com.streambus.basemodule.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.streambus.basemodule.utils.SLog;
import com.trello.rxlifecycle3.LifecycleTransformer;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import butterknife.ButterKnife;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2017/10/14
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class BaseActivity extends RxAppCompatActivity implements Handler.Callback{

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(attachLayoutRes());
        ButterKnife.bind(this);
        initViewModel();
        initViews(savedInstanceState);
        updateViews(false);
    }

    protected void superOnCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 绑定布局文件
     * @return
     */
    @LayoutRes
    protected abstract int attachLayoutRes();

    /**
     * 获取ViewModel实例
     */
    protected abstract void initViewModel();

    /**
     * 初始化视图
     * @param savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * 接下来开始为更新视图进行从操作，如开始请求网络数据
     * @param isRefresh
     */
    protected abstract void updateViews(boolean isRefresh);

    public <T> LifecycleTransformer<T> bindToLife() {
        return this.<T>bindToLifecycle();
    }

    /**
     * 初始化 toolbar
     * homeAsUpEnables为true时，如果toolbar没有设置navigationIcon属性，则默认显示系统默认返回按钮
     * @param toolbar
     * @param homeAsUpEnable
     * @param title
     */
    protected void initToolbar(Toolbar toolbar, boolean homeAsUpEnable, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnable);
    }

    /**
     * android.R.id.home 就是 navigation·home
     * 参考getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnable);
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /************************************* - Handler-Helper - ****************************************/
    private Handler mHandler;

    protected final Handler handler(){
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper(),this);
        }
        return mHandler;
    }


    protected final boolean sendEmptyMessage(int what) {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper(),this);
        }
        return mHandler.sendEmptyMessage(what);
    }

    protected final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper(),this);
        }
        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper(),this);
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

    public void navigatePopBackStack() {}

    public final void navigateFragment(Class<? extends Fragment> clazz) {
        navigateFragment(clazz, null);
    }

    public void navigateFragment(Class<? extends Fragment> clazz, Bundle args){}

    final ArrayDeque<KeyEvent.Callback> mOnKeyEventCallbacks = new ArrayDeque<>();
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Iterator<KeyEvent.Callback> iterator =
                mOnKeyEventCallbacks.descendingIterator();
        while (iterator.hasNext()) {
            KeyEvent.Callback callback = iterator.next();
            if (callback.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        SLog.i(TAG, "onKeyUp keyCode=" + keyCode);
        Iterator<KeyEvent.Callback> iterator =
                mOnKeyEventCallbacks.descendingIterator();
        while (iterator.hasNext()) {
            KeyEvent.Callback callback = iterator.next();
            if (callback.onKeyUp(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Iterator<KeyEvent.Callback> iterator =
                mOnKeyEventCallbacks.descendingIterator();
        while (iterator.hasNext()) {
            KeyEvent.Callback callback = iterator.next();
            if (callback.onKeyLongPress(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }
    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        Iterator<KeyEvent.Callback> iterator =
                mOnKeyEventCallbacks.descendingIterator();
        while (iterator.hasNext()) {
            KeyEvent.Callback callback = iterator.next();
            if (callback.onKeyMultiple(keyCode, count, event)) {
                return true;
            }
        }
        return super.onKeyMultiple(keyCode, count, event);
    }

    @MainThread
    public void addOnKeyListener(@NonNull LifecycleOwner owner, OnKeyListener listener) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        listener.addCancellable(new LifecycleOnKeyListener(lifecycle, listener));
    }



    public static abstract class OnKeyListener implements KeyEvent.Callback{
        public boolean onKeyDown(int keyCode, KeyEvent event){
            return false;
        }
        public boolean onKeyLongPress(int keyCode, KeyEvent event){
            return false;
        }
        public boolean onKeyUp(int keyCode, KeyEvent event){
            return false;
        }
        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event){
            return false;
        }
        @MainThread
        public final void remove() {
            for (Cancellable cancellable: mCancellables) {
                cancellable.cancel();
            }
        }
        private CopyOnWriteArrayList<Cancellable> mCancellables = new CopyOnWriteArrayList<>();
        void addCancellable(@NonNull Cancellable cancellable) {
            mCancellables.add(cancellable);
        }
        void removeCancellable(@NonNull Cancellable cancellable) {
            mCancellables.remove(cancellable);
        }

        public interface Cancellable {
            void cancel();
        }
    }

    class LifecycleOnKeyListener implements LifecycleEventObserver, OnKeyListener.Cancellable {
        private final Lifecycle mLifecycle;
        private final OnKeyListener mOnKeyListener;
        public LifecycleOnKeyListener(Lifecycle lifecycle, OnKeyListener listener) {
            mLifecycle = lifecycle;
            mOnKeyListener = listener;
            lifecycle.addObserver(this);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source,
                                   @NonNull Lifecycle.Event event) {
            SLog.i(TAG, "LifecycleOnKeyListener onStateChanged event=" + event);
            if (event == Lifecycle.Event.ON_START) {
                mOnKeyEventCallbacks.add(mOnKeyListener);
            } else if (event == Lifecycle.Event.ON_STOP) {
                mOnKeyEventCallbacks.remove(mOnKeyListener);
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                cancel();
            }
        }

        public void cancel() {
            mLifecycle.removeObserver(this);
            mOnKeyEventCallbacks.remove(mOnKeyListener);
            mOnKeyListener.removeCancellable(this);
        }
    }

    /******************** ****************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SLog.d(TAG, "[onActivityResult]requestCode=" + requestCode + " ,resultCode=" + resultCode);
        Iterator<PreferenceManager.OnActivityResultListener> iterator = onActivityResultListeners.descendingIterator();
        while (iterator.hasNext()) {
            PreferenceManager.OnActivityResultListener callback = iterator.next();
            if (callback.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    @MainThread
    public void addOnActivityResultListener(@NonNull LifecycleOwner owner, PreferenceManager.OnActivityResultListener listener) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        new LifecycleOnActivityResult(lifecycle, listener);
    }

    private final ArrayDeque<PreferenceManager.OnActivityResultListener> onActivityResultListeners = new ArrayDeque<PreferenceManager.OnActivityResultListener>();

    private class LifecycleOnActivityResult implements LifecycleEventObserver {
        private final Lifecycle mLifecycle;
        private final PreferenceManager.OnActivityResultListener onActivityResultListener;

        public LifecycleOnActivityResult(Lifecycle lifecycle, PreferenceManager.OnActivityResultListener listener) {
            mLifecycle = lifecycle;
            onActivityResultListener = listener;
            lifecycle.addObserver(this);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source,
                                   @NonNull Lifecycle.Event event) {
            SLog.i(TAG, "LifecycleOnActivityResult onStateChanged event=" + event);
            if (event == Lifecycle.Event.ON_CREATE) {
                onActivityResultListeners.add(onActivityResultListener);
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                cancel();
            }
        }

        public void cancel() {
            mLifecycle.removeObserver(this);
            onActivityResultListeners.remove(onActivityResultListener);
        }
    }
}
