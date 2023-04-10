package com.stv.iptv.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.streambus.basemodule.base.BaseActivity;
import com.streambus.basemodule.base.BaseDialogFragment;
import com.streambus.basemodule.base.BaseFragment;
import com.streambus.basemodule.base.ViewModelProviders;
import com.streambus.basemodule.utils.NoDataException;
import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SimpleRequestSubscriber;
import com.streambus.basemodule.widget.LoadingView;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.ColumnBean;
import com.streambus.commonmodule.bean.Trailers;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.usermodule.UserConstants;
import com.streambus.usermodule.module.account.AvatarDialog;
import com.streambus.usermodule.module.account.UserAccountFragment;
import com.streambus.usermodule.module.setting.UserSettingFragment;
import com.streambus.vodmodule.view.download.VodDownloadFragment;
import com.streambus.vodmodule.view.home.HomeBgObserver;
import com.streambus.vodmodule.view.home.VodHomeCommonFragment;
import com.streambus.vodmodule.view.home.VodWatchListFragment;
import com.streambus.vodmodule.view.search.VodSearchFragment;
import com.stv.iptv.app.trailer.TrailerVideoView;

import java.util.EmptyStackException;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/8
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeFragment extends BaseFragment {
    private static final String TAG = "HomeFragment";
    @BindView(R.id.layout_tab)
    LinearLayout mLayoutTab;
    @BindView(R.id.layout_channel)
    FrameLayout mLayoutChannel;
    @BindView(R.id.view_mask)
    View mViewMask;
    @BindView(R.id.tv_item_watch_list)
    TextView mTvItemWatchList;
    @BindView(R.id.iv_settings)
    ImageView iv_settings;

    @BindView(R.id.iv_home_bg)
    ImageView mIvHomeBg;
    @BindView(R.id.trailer_video_view)
    TrailerVideoView mTrailerVideoView;
    @BindView(R.id.iv_user)
    ImageView mIvAccount;

    private HomeViewModel mHomeViewModel;
    List<ColumnBean> mHomeColumnList;
    private LoadingView mLoadingView;
    private int mTryCount;

    @Override
    protected int attachLayoutRes() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initViewModel() {
        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mTrailerVideoView.setViewLifecycle(lifecycle());
        mLoadingView = new LoadingView();
        mLoadingView.setOnBackPressedListener(new BaseDialogFragment.OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                getActivity().onBackPressed();
                return true;
            }
        });
    }

    @Override
    protected void updateViews(boolean isRefresh) {
        Glide.with(this).load(Uri.parse(PreferencesUtils.get(UserConstants.KEY_AVATAR_URI, AvatarDialog.getResourceUri(getContext(), UserConstants.RES_AVATAR[0]))))
                .apply(new RequestOptions().circleCrop())
                .into(mIvAccount);
        if (!isRefresh) {
            tryRequestData(true);
        }
    }


    private void tryRequestData(boolean now) {
        if (mTryCount > 3) {
            mLoadingView.dismiss();
            Toast.makeText(getContext(), R.string.request_data_failed, Toast.LENGTH_LONG).show();
            return;
        }
        if (!now) {
            Toast.makeText(getContext(), R.string.loading_failed, Toast.LENGTH_LONG).show();
        }
        mLoadingView.show(getChildFragmentManager());
        if (!now) {
            MyAppLogin.getInstance().updateLoginAudioState.observeForever(new Observer<Integer>() {
                private boolean currentSuccess = MyAppLogin.getInstance().updateLoginAudioState.getValue() == MyAppLogin.EVENT_TYPE_SUCCESS;
                @Override
                public void onChanged(Integer integer) {
                    if (currentSuccess) {
                        currentSuccess = false;
                        MyAppLogin.getInstance().reTryLogin();
                        return;
                    }
                    if (integer == MyAppLogin.EVENT_TYPE_SUCCESS) {
                        MyAppLogin.getInstance().updateLoginAudioState.removeObserver(this);
                        tryRequestData(true);
                    }
                }
            });
            return;
        }
        mTryCount++;
        mHomeViewModel.subjectRequestData(new SimpleRequestSubscriber<List<ColumnBean>>() {
            @Override
            protected void handleNext(List<ColumnBean> list) {
                SLog.i(TAG, "subjectRequestData handleNext list=>" + list);
                if (list.isEmpty()) {
                    throw new EmptyStackException();
                }
                mHomeColumnList = list;
                mLoadingView.dismiss();
                _initTopTabItem();
            }
            @Override
            protected void handleError(Throwable throwable) {
                SLog.e(TAG, "subjectRequestData handleError", throwable);
                if (throwable instanceof NoDataException) {
                    mLoadingView.dismiss();
                    Toast.makeText(getContext(), R.string.no_data, Toast.LENGTH_LONG).show();
                } else {
                    tryRequestData(false);
                }
            }
        });
    }



    private View mTopItemView;
    private void _initTopTabItem() {
        new View.OnClickListener() {

            private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.callOnClick();
                        mTopItemView.setSelected(false);
                    } else if (mTopItemView != null) {
                        mTopItemView.setSelected(v == mTopItemView);
                    }
                }
            };

            @Override
            public void onClick(View v) {
                if (mTopItemView == v) {
                    return;
                }
                if (mTopItemView != null) {
                    mTopItemView.setSelected(false);
                }
                mTopItemView = v;
                mTopItemView.setSelected(true);
                mTopItemView.requestFocus();

                Fragment fragment;
                if (v == mTvItemWatchList) {
                    backgroundUrl = String.valueOf(R.mipmap.home_bg_default);
                    homeBgObserver.resetBg();
                    fragment = VodWatchListFragment.create(homeBgObserver);
                } else {
                    ColumnBean bean = (ColumnBean) v.getTag();
                    backgroundUrl = bean.getBackground();
                    homeBgObserver.resetBg();
                    fragment = VodHomeCommonFragment.create(bean, homeBgObserver);
                }
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.layout_channel, fragment);
                transaction.commit();
            }

            private String backgroundUrl;
            private HomeBgObserver homeBgObserver = new HomeBgObserver() {
                @Override
                public void changerChannel(String url, List<Trailers> trailers) {
                    if (TextUtils.isEmpty(url)) {
                        Glide.with(HomeFragment.this).load(R.mipmap.home_bg_default).into(mIvHomeBg);
                    } else {
                        Glide.with(HomeFragment.this).load(RequestApi.fileUrl(url)).into(mIvHomeBg);
                    }
                    mViewMask.setBackgroundResource(R.mipmap.mask_action2);
                    mTrailerVideoView.setTrailers(trailers);
                }

                @Override
                public void changerCategory(String url) {
                    mTrailerVideoView.reset();
                    if (TextUtils.isEmpty(url)) {
                        Glide.with(HomeFragment.this).load(R.mipmap.home_bg_default).into(mIvHomeBg);
                    } else {
                        Glide.with(HomeFragment.this).load(RequestApi.fileUrl(url)).into(mIvHomeBg);
                    }
                    mViewMask.setBackgroundResource(R.mipmap.mask_action1);
                }

                @Override
                public void resetBg() {
                    mTrailerVideoView.reset();
                    if (TextUtils.isEmpty(backgroundUrl)) {
                        Glide.with(HomeFragment.this).load(new ColorDrawable(Color.parseColor("#FF000000"))).into(mIvHomeBg);
                    } else if (String.valueOf(R.mipmap.home_bg_default).equals(backgroundUrl)) {
                        Glide.with(HomeFragment.this).load(R.mipmap.home_bg_default).into(mIvHomeBg);
                    } else {
                        Glide.with(HomeFragment.this).load(RequestApi.fileUrl(backgroundUrl)).into(mIvHomeBg);
                    }
                    mViewMask.setBackgroundResource(0);
                }

                @Override
                public void requestFocus() {
                    mTopItemView.requestFocus();
                }
            };

            {/*_initTopTabItem*/
                mLayoutTab.removeViews(1, 5);
                for (int i = 0; i < mHomeColumnList.size(); i++) {
                    ColumnBean bean = mHomeColumnList.get(i);
                    TextView itemView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.item_tab_text, mLayoutTab, false);
                    itemView.setText(bean.getName());
                    itemView.setTag(bean);
                    itemView.setOnFocusChangeListener(onFocusChangeListener);
                    itemView.setOnClickListener(this);
                    List<String> iconList = bean.getIconList();
                    if (iconList != null) {
                        Disposable disposable = RequestApi.requestVodHomeIcon(iconList, getContext())
                                .subscribe(new Consumer<StateListDrawable>() {
                                    @Override
                                    public void accept(StateListDrawable stateListDrawable) throws Exception {
                                        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                                        float rate = getResources().getDisplayMetrics().widthPixels / 1920.0f;
                                        layoutParams.width = (int) (stateListDrawable.getIntrinsicWidth() * rate + 0.5);
                                        layoutParams.height = (int) (stateListDrawable.getIntrinsicHeight() * rate + 0.5);
                                        SLog.d(TAG, "rate =" + rate + "  width=" + layoutParams.width + "  height=" + layoutParams.height);
                                        itemView.setText("");
                                        itemView.setBackground(stateListDrawable);
                                    }
                         });
                    }
                    mLayoutTab.addView(itemView, i + 1);
                }
                mTvItemWatchList.setOnFocusChangeListener(onFocusChangeListener);
                mTvItemWatchList.setOnClickListener(this);
                mLayoutTab.getChildAt(1).callOnClick();
            }
        };
    }

    @OnClick({R.id.iv_search, R.id.iv_download, R.id.iv_user, R.id.iv_settings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                ((BaseActivity) getActivity()).navigateFragment(VodSearchFragment.class);
                break;
            case R.id.iv_download:
                ((BaseActivity) getActivity()).navigateFragment(VodDownloadFragment.class);
                break;
            case R.id.iv_user:
                ((BaseActivity) getActivity()).navigateFragment(UserAccountFragment.class);
                break;
            case R.id.iv_settings:
                ((BaseActivity) getActivity()).navigateFragment(UserSettingFragment.class);
                break;
        }
    }

    @OnFocusChange({R.id.iv_search, R.id.iv_download, R.id.iv_user, R.id.iv_settings})
    public void onViewFocus(View view, boolean hasFocus) {
        if (null != mTopItemView) {
            mTopItemView.setSelected(!hasFocus);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLayoutChannel.removeAllViews();
    }
}
