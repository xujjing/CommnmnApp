package com.streambus.commonmodule.logs;

import android.os.Build;
import android.text.TextUtils;

import com.streambus.basemodule.BaseApplication;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.utils.AESUtil;
import com.streambus.commonmodule.utils.AppUtil;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.requestapi.OkHttpHelper;
import com.streambus.requestapi.SystemInfoUtils;
import com.yoostar.fileloggingutil.FileTreeIo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogCollectionUtils {
    public static final String TAG = LogCollectionUtils.class.getSimpleName();
    private static final String LOG_AES_KEY = "streambus_iptv";
    /**
     *
     * 问题的描述&用户的联系信息&用户的选择的图片
     * @return
     */
    public static Observable<Boolean> doUploadLog(String desc, String userName, String userContact, ArrayList<String> imgs){
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                FileTreeIo.getInstance().getCollectFile(new FileTreeIo.OnFileCollectListener() {
                    @Override
                    public void onNext(File file) {
                        emitter.onNext(file);
                        emitter.onComplete();
                    }
                    @Override
                    public void onError(String s) {
                        emitter.onError(new IllegalStateException(s));
                    }
                });
            }
        }).map(new Function<File, Boolean>() {
            @Override
            public Boolean apply(File file) throws Exception {
                CollectionInfo collectionInfo = new CollectionInfo(CollectionInfo.COLLECTION_TYPE_LOG);
                collectionInfo.setException_desc(desc);
                collectionInfo.setUser_name(userName);
                collectionInfo.setUser_contact(userContact);
                collectionInfo.setMac(Constants.VALUE_LOGIN_ACCOUNT_ID);
                collectionInfo.setCode(Constants.VALUE_LOGIN_ACCOUNT_NAME);
                collectionInfo.setModel(Build.MODEL);
                collectionInfo.setHardware(Build.HARDWARE);
                collectionInfo.setScreen(SystemInfoUtils.getDisplaySize(BaseApplication.getInstance()));
                collectionInfo.setOs_version(Build.VERSION.RELEASE);
                collectionInfo.setSdk_number(Build.VERSION.RELEASE);
                collectionInfo.setRam_memroy(SystemInfoUtils.getTotalMemory(BaseApplication.getInstance()));
                collectionInfo.setVm_size(SystemInfoUtils.getInternalToatalSpace(BaseApplication.getInstance()));
                collectionInfo.setApp_package(AppUtil.getAppPackageName(BaseApplication.getInstance()));
                collectionInfo.setApp_version(AppUtil.getAppVersionName(BaseApplication.getInstance()));
//                collectionInfo.setApp_patch_Version(TinkerManager.getPatchVersion());
                collectionInfo.setId(Constants.VALUE_LOGIN_ACCOUNT_ID);
                collectionInfo.setValidity(MyAppLogin.getInstance().getValidityDay().getValue() + "");
                collectionInfo.setLog_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(MyAppLogin.getInstance().getUtcTime())));
                collectionInfo.setException_type("");
                collectionInfo.setCrash_dump("");
                String logCollectorXml = GsonHelper.toJson(collectionInfo);
                //////////////////////////////////////////////////////////////////////
                SLog.i(TAG, "logCollectorXml " + logCollectorXml);
                String buildXml = AESUtil.encryptHex(LOG_AES_KEY, logCollectorXml);
                Response response = OkHttpHelper.getOkHttpClient().newCall(getRequest(buildXml, imgs, file)).execute();
                if (response.isSuccessful()) {
                    return true;
                } else {
                    String error = "upload_file http_code=>" + response.code();
                    throw new IllegalStateException(error);
                }
            }
        });
    }

    private static Request getRequest(String buildXml, List<String> pictures, File file) {
        Request.Builder builder = new Request.Builder();
        //builder.url("http://192.168.1.106:8088/ai-collector/collect/fbcollector_v2.do") //测试
        builder.url("http://feedback.dajljp29sd.com/collect/fbcollector_v2.do") //正式
                .addHeader("NoEncryption", "NoEncryption")
                .post(createRequestBody(buildXml, pictures, file));
        return builder.build();
    }

    public static RequestBody createRequestBody(String buildXml, List<String> pictures, File file) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        //基本信息
        if (!TextUtils.isEmpty(buildXml)) {
            SLog.e(TAG, "[createRequestBody]buildXml==" + buildXml);
            String contentType = "application/octet-stream";
            builder.addFormDataPart("params", buildXml);
        }
        //图片
        if (null != pictures && pictures.size() > 0) {
            SLog.e(TAG, "[createRequestBody]pictures==" + pictures);
            for (String picture : pictures) {
                File pFile = new File(picture);
                String contentType = "image/jpeg";
                builder.addFormDataPart("file", pFile.getName(), RequestBody.create(MediaType.parse(contentType), pFile));
            }
        }
        //日志文件
        if (file != null) {
            SLog.e(TAG, "[createRequestBody]logFile==" + file);
            String contentType = "application/octet-stream";
            builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse(contentType), file));
        }
        SLog.e(TAG, "[createRequestBody]builder==" + pictures);
        return builder.build();
    }


    public static boolean doUpCrashLog(String excepType, String exception, File file) throws Exception {
        CollectionInfo collectionInfo = new CollectionInfo(CollectionInfo.COLLECTION_TYPE_CRASH);
        try {
            collectionInfo.setMac(Constants.VALUE_LOGIN_ACCOUNT_ID);
            collectionInfo.setCode(Constants.VALUE_LOGIN_ACCOUNT_NAME);
            collectionInfo.setModel(Build.MODEL);
            collectionInfo.setHardware(Build.HARDWARE);
            collectionInfo.setScreen(SystemInfoUtils.getDisplaySize(BaseApplication.getInstance()));
            collectionInfo.setOs_version(Build.VERSION.RELEASE);
            collectionInfo.setSdk_number(Build.VERSION.RELEASE);
            collectionInfo.setRam_memroy(SystemInfoUtils.getTotalMemory(BaseApplication.getInstance()));
            collectionInfo.setVm_size(SystemInfoUtils.getInternalToatalSpace(BaseApplication.getInstance()));
            collectionInfo.setApp_package(AppUtil.getAppPackageName(BaseApplication.getInstance()));
            collectionInfo.setApp_version(AppUtil.getAppVersionName(BaseApplication.getInstance()));
            //                collectionInfo.setApp_patch_Version(TinkerManager.getPatchVersion());
            collectionInfo.setId(Constants.VALUE_LOGIN_ACCOUNT_ID);
            collectionInfo.setException_type(excepType);
            collectionInfo.setCrash_dump(exception);
            collectionInfo.setValidity(MyAppLogin.getInstance().getValidityDay().getValue() + "");
            collectionInfo.setLog_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(MyAppLogin.getInstance().getUtcTime())));
        }catch (Exception e){
            SLog.e(TAG, "collectionInfo setData", e);
            e.printStackTrace();
        }
        String logCollectorXml = GsonHelper.toJson(collectionInfo);
        //////////////////////////////////////////////////////////////////////
        SLog.i(TAG, "logCollectorXml " + logCollectorXml);
        String buildXml = AESUtil.encryptHex(LOG_AES_KEY, logCollectorXml);
        Response response =OkHttpHelper.getOkHttpClient().newCall(getRequest(buildXml, null, file)).execute();
        if (response.isSuccessful()) {
            return true;
        } else {
            String error = "upload_file http_code=>" + response.code();
            throw new IllegalStateException(error);
        }
    }
}
