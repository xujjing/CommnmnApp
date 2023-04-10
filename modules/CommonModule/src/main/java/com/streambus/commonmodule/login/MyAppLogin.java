package com.streambus.commonmodule.login;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SecurePreferences;
import com.streambus.commonmodule.AppBuildConfig;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.CardRechargeBean;
import com.streambus.commonmodule.bean.EmailCodeBean;
import com.streambus.commonmodule.bean.QRCodeBean;
import com.streambus.commonmodule.bean.ResultBean;
import com.streambus.commonmodule.bean.VerifyBean;
import com.streambus.commonmodule.umeng.UMengManager;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.requestapi.LoginManager;
import com.streambus.requestapi.LoginModule;
import com.streambus.requestapi.PortalCall;
import com.streambus.requestapi.bean.HeadInfo;
import com.streambus.requestapi.bean.LoginInfo;
import com.streambus.requestapi.bean.UserInfo;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/5/27
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class MyAppLogin extends LoginModule {

    public static final String ACTION_LOGIN_FAILED = "LoginFailed";
    public static final String ACTION_LOGIN_ERROR = "LoginError";
    public static final String ACTION_ExitByServer = "ExitByServer";

    public static final int AUTHORITY_TYPE_Vod = 1;
    public static final int AUTHORITY_TYPE_Live = 2;
    public static final int AUTHORITY_TYPE_LiveAndVod = 3;

    private static final String TAG = "MyAppLogin";
    private static MyAppLogin sAppLogin;

    private LoginInfo mLoginInfo;
    private boolean mIsCacheCmsUrl;
    private String mLastCmsUrl;
    private boolean mIsCacheFileUrl;
    private String mLastFileUrl;
    private boolean mIsCacheEpgUrl;
    private String mLastEpgUrl;
    private boolean mIsAutoLogin;


    private MyAppLogin(boolean isTest, Context context) {
        super(isTest, LoginManager.AppType.ATV, context, Constants.VALUE_LOGIN_ACCOUNT_ID, RequestApi.LANGUAGE, AppBuildConfig.FLAVOR);
    }

    public static MyAppLogin getInstance() {
        return sAppLogin;
    }

    public static void init(Context context, boolean isTest) {
        sAppLogin = new MyAppLogin(isTest, context);
    }

    public UserInfo getUserInfo() {
        return mLoginManager.getUserInfo();
    }

    public LoginInfo getLoginInfo() {
        return mLoginInfo;
    }

    public Map.Entry<Integer, Integer> getValidityDay() {
        try {
            if (mLoginInfo != null) {
                return new AbstractMap.SimpleEntry<>(mLoginInfo.getAuthority(), mLoginInfo.getDays());
            } else {
                String validity = SecurePreferences.get(Constants.KEY_OVERAGE_VALIDITY, "");
                if (!TextUtils.isEmpty(validity)) {
                    String[] split = validity.split("_");
                    if (split.length == 3) {
                        int authority = Integer.parseInt(split[0]);
                        int validityDay = Integer.parseInt(split[1]) - Math.max((int) ((getUtcTime() - Long.parseLong(split[2])) / (24 * 3600 * 1000)), 0);
                        return new AbstractMap.SimpleEntry<>(authority, validityDay);
                    }
                }
            }
        } catch (Exception e) {
            SLog.e(TAG,"getValidityDay Exception", e);
        }
        return new AbstractMap.SimpleEntry<>(0, 0);
    }

    public void autoLogin() {
        SLog.i(TAG, "autoLogin VALUE_LOGIN_TOKEN=" + Constants.VALUE_LOGIN_TOKEN);
        if (!TextUtils.isEmpty(Constants.VALUE_LOGIN_TOKEN)) {
            startLoginByToken(Constants.SUBJECT_LOGIN_TYPE.getValue(), Constants.VALUE_LOGIN_ACCOUNT_ID, Constants.VALUE_LOGIN_ACCOUNT_NAME, Constants.VALUE_LOGIN_TOKEN);
            mIsAutoLogin = true;
            return;
        }
        if (PlatformUtil.is_ATV_Platform()) {
            Constants.SUBJECT_LOGIN_TYPE.postValue(LoginManager.LOGIN_TYPE_loginByDeviceSN);
            startLoginByProxy(loginByDeviceSn(PlatformUtil.getIdCipher()));
            mIsAutoLogin = true;
            return;
        }
    }

    private boolean reStartLogin() {
        if (Constants.SUBJECT_LOGIN_TYPE.getValue() == LoginManager.LOGIN_TYPE_loginByDeviceSN){
            if (PlatformUtil.is_ATV_Platform()) {
                startLoginByProxy(loginByDeviceSn(PlatformUtil.getIdCipher()));
                return true;
            }
            SLog.e(TAG, "reLogin LOGIN_TYPE_loginByDeviceSN  is_ATV_Platform= false");
            return false;
        }
        if (Constants.SUBJECT_LOGIN_TYPE.getValue() == LoginManager.LOGIN_TYPE_loginByAccount) {
            String password = PreferencesUtils.get(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, "");
            if (!TextUtils.isEmpty(password)) {
                startLoginByProxy(loginByAccount(Constants.VALUE_LOGIN_ACCOUNT_NAME, password));
                return true;
            }
            return false;
        }
        if (Constants.SUBJECT_LOGIN_TYPE.getValue() == LoginManager.LOGIN_TYPE_loginByThirdParty) {
            String uid = SecurePreferences.get(Constants.KEY_LOGIN_THIRD_UID, "");
            if (!TextUtils.isEmpty(uid)) {
                startLoginByProxy(loginByThirdParty(UserInfo.checkThirdParty(SecurePreferences.get(Constants.KEY_LOGIN_THIRD_PARTY, "")), uid,
                        SecurePreferences.get(Constants.KEY_LOGIN_THIRD_DISPLAYNAME, ""), SecurePreferences.get(Constants.KEY_LOGIN_THIRD_EMAIL, "")));
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean isAutoLogin() {
        return mIsAutoLogin;
    }

    public void logoutAccount(boolean cleanName) {
        logout();
        mIsAutoLogin = false;
        mLoginInfo = null;
        Constants.VALUE_LOGIN_ACCOUNT_ID = "";
        Constants.VALUE_LOGIN_ACCOUNT_NAME = "";
        Constants.VALUE_LOGIN_TOKEN = "";
        PreferencesUtils.put(Constants.KEY_LOGIN_METHOD_TYPE, (Serializable)null);
        SecurePreferences.put(Constants.KEY_LOGIN_ACCOUNT_ID, (Serializable)null);
        if (cleanName){
            PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_NAME, (Serializable)null);
            PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, (Serializable)null);
        }
        SecurePreferences.put(Constants.KEY_LOGIN_TOKEN, (Serializable)null);
        SecurePreferences.put(Constants.KEY_LOGIN_SERVICE_NAME, (Serializable)null);
        SecurePreferences.put(Constants.KEY_LOGIN_AUTO_RENEW, (Serializable)null);
        SecurePreferences.put(Constants.KEY_OVERAGE_VALIDITY,  (Serializable)null);
        UMengManager.onProfileSignOff();
    }

    public Bundle keepValues() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_LOGIN_METHOD_TYPE, PreferencesUtils.get(Constants.KEY_LOGIN_METHOD_TYPE, 0));
        bundle.putSerializable(Constants.KEY_LOGIN_ACCOUNT_ID, SecurePreferences.get(Constants.KEY_LOGIN_ACCOUNT_ID, ""));
        bundle.putSerializable(Constants.KEY_LOGIN_ACCOUNT_NAME, PreferencesUtils.get(Constants.KEY_LOGIN_ACCOUNT_NAME, ""));
        bundle.putSerializable(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, PreferencesUtils.get(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, ""));
        bundle.putSerializable(Constants.KEY_LOGIN_TOKEN, SecurePreferences.get(Constants.KEY_LOGIN_TOKEN, ""));
        bundle.putSerializable(Constants.KEY_LOGIN_SERVICE_NAME, SecurePreferences.get(Constants.KEY_LOGIN_SERVICE_NAME, ""));
        bundle.putSerializable(Constants.KEY_LOGIN_AUTO_RENEW, SecurePreferences.get(Constants.KEY_LOGIN_AUTO_RENEW, false));
        bundle.putSerializable(Constants.KEY_OVERAGE_VALIDITY, SecurePreferences.get(Constants.KEY_OVERAGE_VALIDITY, ""));
        bundle.putSerializable(Constants.KEY_LAST_CMS_URL, SecurePreferences.get(Constants.KEY_LAST_CMS_URL, ""));
        bundle.putSerializable(Constants.KEY_LAST_EPG_URL, SecurePreferences.get(Constants.KEY_LAST_EPG_URL, ""));
        bundle.putSerializable(Constants.KEY_LAST_FILE_URL, SecurePreferences.get(Constants.KEY_LAST_FILE_URL, ""));
        return bundle;
    }

    public void reKeepValues(Bundle bundle) {
        PreferencesUtils.put(Constants.KEY_LOGIN_METHOD_TYPE, bundle.getSerializable(Constants.KEY_LOGIN_METHOD_TYPE));
        SecurePreferences.put(Constants.KEY_LOGIN_ACCOUNT_ID, bundle.getSerializable(Constants.KEY_LOGIN_ACCOUNT_ID));
        PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_NAME, bundle.getSerializable(Constants.KEY_LOGIN_ACCOUNT_NAME));
        PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, bundle.getSerializable(Constants.KEY_LOGIN_ACCOUNT_PASSWORD));
        SecurePreferences.put(Constants.KEY_LOGIN_TOKEN, bundle.getSerializable(Constants.KEY_LOGIN_TOKEN));
        SecurePreferences.put(Constants.KEY_LOGIN_SERVICE_NAME, bundle.getSerializable(Constants.KEY_LOGIN_SERVICE_NAME));
        SecurePreferences.put(Constants.KEY_LOGIN_AUTO_RENEW, bundle.getSerializable(Constants.KEY_LOGIN_AUTO_RENEW));
        SecurePreferences.put(Constants.KEY_OVERAGE_VALIDITY,  bundle.getSerializable(Constants.KEY_OVERAGE_VALIDITY));
        SecurePreferences.get(Constants.KEY_LAST_CMS_URL, bundle.getSerializable(Constants.KEY_LAST_CMS_URL));
        SecurePreferences.get(Constants.KEY_LAST_EPG_URL, bundle.getSerializable(Constants.KEY_LAST_EPG_URL));
        SecurePreferences.get(Constants.KEY_LAST_FILE_URL, bundle.getSerializable(Constants.KEY_LAST_FILE_URL));
    }

    private Bundle mLoginErrorArguments;
    @Override
    protected void handleLoginSuccess(int loginType, UserInfo userInfo, LoginInfo loginInfo, String json) {
        SLog.d(TAG, "handleLoginSuccess loginType=" + loginType + "  loginInfo=>" + loginInfo);
        mLoginErrorArguments = null;
        mLoginInfo = loginInfo;
        getCmsUrl(true); getFileUrl(true); getEpgUrl(true);

        Constants.SUBJECT_LOGIN_TYPE.postValue(loginType);
        Constants.VALUE_LOGIN_ACCOUNT_ID = loginInfo.getAccountId();
        Constants.VALUE_LOGIN_ACCOUNT_NAME = loginInfo.getAccount();
        Constants.VALUE_LOGIN_TOKEN = loginInfo.getToken();


        if (loginType == LoginManager.LOGIN_TYPE_loginByAccount) {
            if (!TextUtils.isEmpty(userInfo.getPassword())) {
                PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_PASSWORD, userInfo.getPassword());
            }
        } else if (loginType == LoginManager.LOGIN_TYPE_loginByThirdParty) {
            if (!TextUtils.isEmpty(userInfo.getUid())) {
                SecurePreferences.put(Constants.KEY_LOGIN_THIRD_PARTY, userInfo.getThird_party());
                SecurePreferences.put(Constants.KEY_LOGIN_THIRD_UID, userInfo.getUid());
                SecurePreferences.put(Constants.KEY_LOGIN_THIRD_DISPLAYNAME, userInfo.getDisplay_name());
                SecurePreferences.put(Constants.KEY_LOGIN_THIRD_EMAIL, userInfo.getEmail());
            }
        }
        PreferencesUtils.put(Constants.KEY_LOGIN_METHOD_TYPE, loginType);
        SecurePreferences.put(Constants.KEY_LOGIN_ACCOUNT_ID, Constants.VALUE_LOGIN_ACCOUNT_ID);
        PreferencesUtils.put(Constants.KEY_LOGIN_ACCOUNT_NAME, Constants.VALUE_LOGIN_ACCOUNT_NAME);
        SecurePreferences.put(Constants.KEY_LOGIN_TOKEN, Constants.VALUE_LOGIN_TOKEN);
        SecurePreferences.put(Constants.KEY_LOGIN_SERVICE_NAME, loginInfo.getServiceName());
        SecurePreferences.put(Constants.KEY_LOGIN_AUTO_RENEW, loginInfo.isAutoRenew());
        SecurePreferences.put(Constants.KEY_OVERAGE_VALIDITY,  loginInfo.getAuthority()+ "_" + loginInfo.getDays()+ "_" + getUtcTime());
        UMengManager.onProfileSignIn(Constants.VALUE_LOGIN_ACCOUNT_NAME);
    }

    @Override
    protected void handleAutoLoginFailed(int loginType, List<Exception> exceptions) {
        SLog.e(TAG, "handleAutoLoginFailed loginType=" + loginType + "  exceptions=>" + exceptions);
        //录失效，跳到登录页面，要求重新登录（输入框填好之前的帐号和密码，用户只要点击登录就行）
        //用户按返回，允许离线状态回到App首页继续使用
        logoutAccount(false);
        mLoginErrorArguments = new Bundle();
        mLoginErrorArguments.putInt(Constants.KEY_LOGIN_METHOD_TYPE, loginType);
        mLoginErrorArguments.putString(Constants.KEY_ACTION, ACTION_LOGIN_FAILED);
        mLoginErrorArguments.putSerializable(Constants.KEY_DATA, exceptions.isEmpty() ? new UnknownError() : exceptions.get(exceptions.size() - 1));
    }


    @Override
    protected void handleAutoLoginError(int loginType, LoginInfo loginInfo) {
        SLog.e(TAG, "handleAutoLoginError loginType=" + loginInfo + "  loginInfo=>" + loginInfo);
        int result = loginInfo.getResult();
        if (result == -309 && reStartLogin()) {// Token过期，重新登录
            SLog.i(TAG, "handleAutoLoginError token expired, reStartLogin");
            return;
        }
        if (result == -302 || result == -303){// 账号不正确或者被暂停
            SLog.i(TAG, "handleAutoLoginError account error or suspended");
            logoutAccount(false);
        }
        //帐号错误，跳到登录页面
        // 用户按返回，需要根据登录结果明确处理，如：帐号非法，返回退出APP，清除登录accountId、token 帐号和密码，下次进入使用设备登录
        mLoginErrorArguments = new Bundle();
        mLoginErrorArguments.putInt(Constants.KEY_LOGIN_METHOD_TYPE, loginType);
        mLoginErrorArguments.putString(Constants.KEY_ACTION, ACTION_LOGIN_ERROR);
        mLoginErrorArguments.putInt(Constants.KEY_DATA, result);
    }

    @Override
    protected void handleExitByServer(int loginType, HeadInfo headInfo, String json) {
        SLog.e(TAG, "handleExitByServer loginType" + loginType + "   headInfo=>" + headInfo);
        logoutAccount(false);
        // 被服务Kiss，跳到登录页面
        // 用户按返回，需要根据心跳指令明确处理，如：帐号禁用，返回退出APP，清除登录accountId、token 帐号和密码，下次进入使用设备登录
        mLoginErrorArguments = new Bundle();
        mLoginErrorArguments.putInt(Constants.KEY_LOGIN_METHOD_TYPE, loginType);
        mLoginErrorArguments.putString(Constants.KEY_ACTION, ACTION_ExitByServer);
        mLoginErrorArguments.putInt(Constants.KEY_DATA, headInfo.getCmd());
    }

    public Bundle getLoginErrorArguments() {
        Bundle temBundle = mLoginErrorArguments;
        mLoginErrorArguments = null;
        return temBundle;
    }

    public String getCmsUrl(boolean update) {
        if (mLoginInfo != null && mLoginInfo.getCmsUrls() != null) {
            if ((mIsCacheCmsUrl || mLastCmsUrl == null) && !mLoginInfo.getCmsUrls().isEmpty()) {
                int index = new Random().nextInt(mLoginInfo.getCmsUrls().size()) % mLoginInfo.getCmsUrls().size();
                String cmdHost = mLoginInfo.getCmsUrls().get(index);
                if (cmdHost.endsWith("/")) {
                    cmdHost = cmdHost.substring(0, cmdHost.length() - 1);
                }
                mIsCacheCmsUrl = false;
                mLastCmsUrl = cmdHost;
                SecurePreferences.put(Constants.KEY_LAST_CMS_URL, mLastCmsUrl);
            } else if (update && !mLoginInfo.getCmsUrls().isEmpty()) {
                int index = mLoginInfo.getCmsUrls().indexOf(mLastCmsUrl);// index为-1也没问题
                String cmdHost = mLoginInfo.getCmsUrls().get(++index % mLoginInfo.getCmsUrls().size());
                if (cmdHost.endsWith("/")) {
                    cmdHost = cmdHost.substring(0, cmdHost.length() - 1);
                }
                mLastCmsUrl = cmdHost;
                SecurePreferences.put(Constants.KEY_LAST_CMS_URL, mLastCmsUrl);
            }
            return mLastCmsUrl;
        } else if (mLastCmsUrl == null) {
            mIsCacheCmsUrl = true;
            mLastCmsUrl = SecurePreferences.get(Constants.KEY_LAST_CMS_URL, "");
        }
        return mLastCmsUrl;
    }

    public String getFileUrl(boolean update) {
        if (mLoginInfo != null) {
            String fileUrl = mLoginInfo.getFileUrl();
            if (TextUtils.isEmpty(fileUrl)) {
                return getCmsUrl(update);
            }
            if (update || mIsCacheFileUrl || mLastFileUrl == null) {
                mIsCacheFileUrl = true;
                if (fileUrl.endsWith("/")) {
                    fileUrl = fileUrl.substring(0, fileUrl.length() - 1);
                }
                mLastFileUrl = fileUrl;
                SecurePreferences.put(Constants.KEY_LAST_FILE_URL, mLastFileUrl);
            }
            return mLastFileUrl;
        } else if (mLastFileUrl == null) {
            mIsCacheFileUrl = true;
            mLastFileUrl = SecurePreferences.get(Constants.KEY_LAST_FILE_URL, "");
            if (TextUtils.isEmpty(mLastFileUrl)) {
                mLastFileUrl = getCmsUrl(update);
            }
        }
        return mLastFileUrl;
    }

    public String getEpgUrl(boolean update) {
        if (mLoginInfo != null) {
            if (update || mIsCacheEpgUrl || mLastEpgUrl == null) {
                String epgUrl = mLoginInfo.getEpgUrl();
                if (epgUrl.endsWith("/")) {
                    epgUrl = epgUrl.substring(0, epgUrl.length() - 1);
                }
                mLastEpgUrl = epgUrl;
                SecurePreferences.put(Constants.KEY_LAST_EPG_URL, mLastEpgUrl);
            }
            return mLastEpgUrl;
        } else if (mLastEpgUrl == null) {
            mIsCacheEpgUrl = true;
            mLastEpgUrl = SecurePreferences.get(Constants.KEY_LAST_EPG_URL, "");
        }
        return mLastEpgUrl;
    }

    public String getLocalHostIp() {
        return mLoginManager.getLocalCdnIp();
    }

    public long getUtcTime() {
        return mLoginManager.getUtcTime();
    }


    /************************************* - RequestAPI - ****************************************/
    /************************************* - RequestAPI - ****************************************/
    // 邮箱注册
    private static final String REGIST_EMAIL_PATH = "/bs/email/regist";
    // 手机号获取验证码
    private static final String VERIFY_PHONE_PATH = "/bs/phone/getauthcode";
    // 邮箱获取验证码
    private static final String VERIFY_EMAIL_PATH = "/bs/email/getCaptcha";
    // 修改密码
    private static final String REST_PWD_PATH = "/bs/account/reset_pwd";
    //邮箱修改验证码
    private static final String REST_PWD_BY_CAPTCHA_PATH = "/bs/account/updatePwdByCaptcha";
    //设备充值
    private static final String CARD_RECHARGE = "/bs/cardrecharge";

    // 二维码登录令牌获取
    private final String VERIFY_QRCODE_TOKEN = "/bs/getQRCodeToken";

    /**
     * 二维码登录令牌获取，返回结果
     * { "result": 0, "message": "", "QRCodeToken": "adfasddfgooulkj12kjhfasd" // 二维码登录凭证}
     */
    public Observable<QRCodeBean> getQrcodeToken() {
        return Observable.just(mLoginManager.getUserInfo())
                .flatMap(new Function<UserInfo, ObservableSource<QRCodeBean>>() {
                    @Override
                    public ObservableSource<QRCodeBean> apply(UserInfo userInfo) throws Exception {
                        String content = String.format("{“sn”:\"%s\", \"accountId\": \"%s\",\"token\": \"%s\"}", PlatformUtil.getIdCipher(), Constants.VALUE_LOGIN_ACCOUNT_ID, Constants.VALUE_LOGIN_TOKEN);
                        return requestData(VERIFY_QRCODE_TOKEN, content)
                                .map(new Function<String, QRCodeBean>() {
                                    @Override
                                    public QRCodeBean apply(String result) throws Exception {
                                        return GsonHelper.toType(result, QRCodeBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 邮箱注册，返回结果
     * { "result": 0, "message": ""}
     */
    public Observable<ResultBean> registByEmail(final String email, final String password){
        return Observable.just(mLoginManager.getUserInfo())
                .flatMap(new Function<UserInfo, ObservableSource<ResultBean>>() {
                    @Override
                    public ObservableSource<ResultBean> apply(UserInfo userInfo) throws Exception {
                        userInfo.resetAccount();
                        userInfo.setEmail(email);
                        userInfo.setPassword(password);
                        String content = GsonHelper.toJson(userInfo);
                        return requestData(REGIST_EMAIL_PATH, content)
                                .map(new Function<String, ResultBean>() {
                                    @Override
                                    public ResultBean apply(String result) throws Exception {
                                        return GsonHelper.toType(result, ResultBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 密码重置，请求数据
     * { "accountId": "123", "token": "ssssqwwwwwww", "password": "123", "newPassword": "234"}
     * 返回结果:  { "result": 0}
     */
    public final Observable<ResultBean> resetPassword(String oldPassword, String newPassword){
        return Observable.just(0)
                .flatMap(new Function<Integer, ObservableSource<ResultBean>>() {
                    @Override
                    public ObservableSource<ResultBean> apply(Integer integer) throws Exception {
                        String content = new JSONObject()
                                .put("accountId", Constants.VALUE_LOGIN_ACCOUNT_ID)
                                .put("token", Constants.VALUE_LOGIN_TOKEN)
                                .put("password", oldPassword)
                                .put("newPassword", newPassword).toString();
                        return requestData(REST_PWD_PATH, content)
                                .map(new Function<String, ResultBean>() {
                                    @Override
                                    public ResultBean apply(String result) throws Exception {
                                        return GsonHelper.toType(result, ResultBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 获取手机验证码，请求参数
     * {
     *  "phone": "8613212709070", "app_package": "com.iptv.live", "language": "en", "type": 1 } // 1: 注册；2：密码重置；
     * 返回数据：
     *  { "result": 0 }
     */
    public final Observable<String> getPhoneVerifiCode(String content){
        return requestData(VERIFY_PHONE_PATH, content);
    }

    /**
     * 获取邮箱验证码，请求参数
     * {
     *  "email": "123@qq.com", "app_package": "com.iptv.live", "language": "en", "type": 1 } // 1: 注册；2：密码重置；
     * 返回数据：
     *  { "result": 0 }
     */
    public final Observable<EmailCodeBean> getEmailVerifiCode(String email){
        return Observable.just(email)
                .flatMap(new Function<String, ObservableSource<EmailCodeBean>>() {
                    @Override
                    public ObservableSource<EmailCodeBean> apply(String email) throws Exception {
                        VerifyBean requetsBean = new VerifyBean();
                        requetsBean.setEmail(email);
                        requetsBean.setApp_package(getUserInfo().getApp_package());
                        requetsBean.setLanguage(getUserInfo().getLanguage());
                        String paramJson = GsonHelper.toJson(requetsBean);
                        return requestData(VERIFY_EMAIL_PATH, paramJson)
                                .map(new Function<String, EmailCodeBean>() {
                                    @Override
                                    public EmailCodeBean apply(String result) throws Exception {
                                        return GsonHelper.toType(result, EmailCodeBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 通过邮箱重置密码，请求数据
     * { "email": 123@qq.com, "app_package": "com.iptv.live",//软件包名，必传
     * "password":"12345678", "captcha":"213211" }
     * 返回结果:  { "result": 0}
     */
    public final Observable<EmailCodeBean> resetPWDByEmailCaptcha(String email,String password, String captcha){
        return Observable.just(0)
                .flatMap(new Function<Integer, ObservableSource<EmailCodeBean>>() {
                    @Override
                    public ObservableSource<EmailCodeBean> apply(Integer integer) throws Exception {
                        VerifyBean requetsBean = new VerifyBean();
                        requetsBean.setApp_package(getUserInfo().getApp_package());
                        requetsBean.setEmail(email);
                        requetsBean.setCaptcha(captcha);
                        requetsBean.setPassword(password);
                        String paramJson = GsonHelper.toJson(requetsBean);
                        return requestData(REST_PWD_BY_CAPTCHA_PATH, paramJson)
                                .map(new Function<String, EmailCodeBean>() {
                                    @Override
                                    public EmailCodeBean apply(String resultString) throws Exception {
                                        return GsonHelper.toType(resultString, EmailCodeBean.class);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Code充值
     */
    public final Observable<ResultBean> requestCardRecharge(String code) {
        return Observable.just(new CardRechargeBean(Constants.VALUE_LOGIN_ACCOUNT_ID, Constants.VALUE_LOGIN_TOKEN, code))
                .flatMap(new Function<CardRechargeBean, ObservableSource<ResultBean>>() {
                    @Override
                    public ObservableSource<ResultBean> apply(CardRechargeBean cardRechargeBean) throws Exception {
                        return requestData(CARD_RECHARGE, GsonHelper.toJson(cardRechargeBean))
                                .map(new Function<String, ResultBean>() {
                                    @Override
                                    public ResultBean apply(String result) throws Exception {
                                        return GsonHelper.toType(result, ResultBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 请求用户数据
     * @param path  路径
     * @param content json请求数据
     * @param headers 特殊请求头
     * @return json结果数据
     */
    public final Observable<String> requestData(final String path, final String content, final Map.Entry<String, String>... headers){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                final PortalCall<String> portalCall = mLoginManager.requestData(path, content, headers);
                emitter.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        portalCall.cancel();
                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                });
                emitter.onNext(portalCall.execute());
                emitter.onComplete();
            }
        });
    }

    /*----------------------------------- END --------------------------------------------------*/

    /*----------------------------------- END --------------------------------------------------*/

    public boolean verifyEmail(String email) {
        String check = "^[a-z0-9A-Z]+[-|a-z0-9A-Z._]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$";//"^[a-z0-9A-Z]+([-_.][a-z0-9A-Z]+)*@([a-z0-9A-Z]+[-.])+[a-z0-9A-Z]{2,5}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        return matcher.matches();
    }

    public boolean verifyPhone(String telephoneNumber) {
        String check = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(telephoneNumber);
        return matcher.matches();
    }
}
