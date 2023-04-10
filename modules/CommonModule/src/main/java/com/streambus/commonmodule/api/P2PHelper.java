//package com.streambus.commonmodule.api;
//
//import android.os.Handler;
//import android.text.TextUtils;
//
//import com.bumptech.glide.load.HttpException;
//import com.forcetech.android.ForceTV;
//import com.streambus.basemodule.utils.SLog;
//import com.streambus.requestapi.OkHttpHelper;
//
//import java.io.IOException;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableEmitter;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.annotations.NonNull;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Request;
//import okhttp3.Response;
//import timber.log.Timber;
//
///**
// * @author Victor
// */
//
//public class P2PHelper{
//    private static final String TAG = P2PHelper.class.getSimpleName();
//
//    public static final int MSG_P2P_TRANSFER_INFO = 0x1156;
//
//    private static final String FTV_CMD_STOP_P2P = "http://127.0.0.1:9908/api?func=stop_chan&id=%s";
//    private static final String FTV_CMD_STOP_ALL_P2P = "http://127.0.0.1:9908/api?func=stop_all_chan";
//    public static final String FTV_CMD_GET_P2P_INFO = "http://127.0.0.1:9908/api?func=query_chan_p2p_info&id=";
//    private static final String FTV_CMD_SET_UPLOAD_FLOW = "http://127.0.0.1:9908/api?func=set_up_flow&id=%s&max=%d&avg=%d";
//    public static final String FTV_CMD_GET_CLIENT_INFO = "http://127.0.0.1:9908/api?func=query_process_info";
//
//    public static final String FTV_CMD_DOWNLOAD = "http://127.0.0.1:9908/api?func=start_chan&id=%s&flag=download&path=%s&file=%s&server=%s";
//    public static final String FTV_CMD_PAUSE = "http://127.0.0.1:9908/api?func=pause_chan&id=%s";
//    public static final String FTV_CMD_RESUME = "http://127.0.0.1:9908/api?func=resume_chan&id=%s";
//    public static final String FTV_CMD_GET_DOWNLOAD_PROGRESS = "http://127.0.0.1:9908/api?func=query_chan_data_info&id=%s";
//
//    //private Handler mRequestHandler;
//    //private HandlerThread mRequestHandlerThread;
//    private ForceTV mForceClient;
//
//    private P2PHelper() {
//
//    }
//
//    public static P2PHelper getInstance() {
//        return SingletonInstance.INSTANCE;
//    }
//
//    private static class SingletonInstance {
//        private static final P2PHelper INSTANCE = new P2PHelper();
//    }
//
//    /**
//     * 开启应用时初始化
//     */
//    public void openClient() {
//        Timber.i("openClient, ForceTV.setup");
//        if (mForceClient == null) {
//            mForceClient = new ForceTV();//构造已经启动了p2p
//        }
//    }
//
//    /**
//     * 退出应用关闭
//     */
//    public void closeClient() {
//        if (mForceClient != null) {
//            mForceClient.closeClient();
//            mForceClient = null;
//        }
//    }
//
//
//    /**
//     * 开始P2P
//     *
//     * @param id
//     * @param p2pServer
//     * @return
//     */
//    public Observable<String> requestStartP2P(final String id, final String p2pServer) {
//        SLog.i(TAG, "requestStartP2P...");
//        return Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
//                if (mForceClient == null) {
//                    throw new IllegalStateException("mForceClient == null");
//                }
//                String clientCmd = "http://127.0.0.1:9908/cmd.xml?cmd=switch_chan&id=" + id + "&server=" + p2pServer;
//                SLog.i(TAG, "requestStartP2P mClientCmd=" + clientCmd);
//                Request request = new Request.Builder().url(clientCmd).build();
//                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
//                emitter.setDisposable(new Disposable() {
//                    @Override
//                    public void dispose() {
//                        call.cancel();
//                    }
//
//                    @Override
//                    public boolean isDisposed() {
//                        return false;
//                    }
//                });
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    emitter.onNext(response.body().string());
//                    emitter.onComplete();
//                } else {
//                    emitter.onError(new HttpException(response.code()));
//                }
//            }
//        }).subscribeOn(Schedulers.io());
//    }
//
//    /**
//     * 停止
//     *
//     * @param id
//     * @return
//     */
//    public Observable requestStopP2P(final String id) {
//        return  Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
//                if (mForceClient == null) {
//                    throw new IllegalStateException("mForceClient == null");
//                }
//                String clientCmd = String.format(FTV_CMD_STOP_P2P, id);
//                SLog.i(TAG, "requestStopP2P clientCmd=" + clientCmd);
//                Request request = new Request.Builder().url(clientCmd).build();
//                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
//                emitter.setDisposable(new Disposable() {
//                    @Override
//                    public void dispose() {
//                        call.cancel();
//                    }
//                    @Override
//                    public boolean isDisposed() {
//                        return false;
//                    }
//                });
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    emitter.onNext(response.body().string());
//                    emitter.onComplete();
//                } else {
//                    emitter.onError(new HttpException(response.code()));
//                }
//            }
//        }).subscribeOn(Schedulers.io());
//    }
//
//    /**
//     * 停止所有
//     *
//     * @return
//     */
//    public Observable requestStopAllP2P() {
//        return  Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
//                if (mForceClient == null) {
//                    throw new IllegalStateException("mForceClient == null");
//                }
//                String clientCmd = FTV_CMD_STOP_ALL_P2P;
//                SLog.i(TAG, "requestStopAllP2P clientCmd=" + clientCmd);
//                Request request = new Request.Builder().url(clientCmd).build();
//                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
//                emitter.setDisposable(new Disposable() {
//                    @Override
//                    public void dispose() {
//                        call.cancel();
//                    }
//                    @Override
//                    public boolean isDisposed() {
//                        return false;
//                    }
//                });
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    emitter.onNext(response.body().string());
//                    emitter.onComplete();
//                } else {
//                    emitter.onError(new HttpException(response.code()));
//                }
//            }
//        }).subscribeOn(Schedulers.io());
//    }
//
//    private int requestSetUploadFlow(String id, int max, int avg) {
//        int ret = 0;
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_SET_UPLOAD_FLOW, id, max, avg);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//
//                }
//            });
//
//        }
//
//        return ret;
//    }
//
//    private void requestGetP2PInfo(final Handler h) {
//        //SLog.i(TAG, "requestGetP2PInfo  handler=" + h + "  mForceClient===>>" + (mForceClient == null));
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_GET_P2P_INFO);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    if (e != null) {
//                        SLog.i(TAG, "requestGetP2PInfo  handler=>onFailure" + e.getStackTrace());
//                    } else {
//                        SLog.i(TAG, "requestGetP2PInfo  onFailure e==null");
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        String result = response.body().string();
//                        if (result != null) {
//                            h.obtainMessage(MSG_P2P_TRANSFER_INFO, result).sendToTarget();
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    public Observable<String> queryP2PDownloadSpeed(final String id, final String p2pServer) {
//        SLog.i(TAG, "queryP2PDownloadSpeed");
//        return  Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
//                if (mForceClient == null) {
//                    throw new IllegalStateException("mForceClient == null");
//                }
//                String clientCmd = FTV_CMD_GET_P2P_INFO + id;
//                SLog.i(TAG, "requestStopAllP2P clientCmd=" + clientCmd);
//                Request request = new Request.Builder().url(clientCmd).build();
//                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
//                emitter.setDisposable(new Disposable() {
//                    @Override
//                    public void dispose() {
//                        call.cancel();
//                    }
//                    @Override
//                    public boolean isDisposed() {
//                        return false;
//                    }
//                });
//
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    String result = response.body().string();
//                    if (!TextUtils.isEmpty(result)) {
//                        if (parseP2pMsg(result) > 0) {
//                            //发送消息非 fccs消息
//                            SLog.i(TAG, "linked p2p server " + p2pServer);
//                            count_p2p_download_speed = 0;
//                            emitter.onNext(result);
//                        } else {
//                            if (count_p2p_download_speed > 2) {
//                                count_p2p_download_speed = 0;
//                                //发送fccs消息,并切换p2p server
//                                emitter.onNext("-1");
//                            } else {
//                                count_p2p_download_speed = count_p2p_download_speed + 1;
//                                //callbackHandler.removeMessages(VodIjkVideoView.MSG_P2P_DOWNLOAD_SPEED);
//                                //callbackHandler.sendEmptyMessageDelayed(VodIjkVideoView.MSG_P2P_DOWNLOAD_SPEED, 7 * 1000);
//                                emitter.onNext(String.valueOf(count_p2p_download_speed));
//                            }
//                        }
//                        emitter.onComplete();
//                    } else {
//                        emitter.onError(new HttpException("response.body isEmpty"));
//                    }
//                } else {
//                    emitter.onError(new HttpException(response.code()));
//                }
//            }
//        });
//    }
//
//    int count_p2p_download_speed = 0;
//
//    public int parseP2pMsg(String p2pMsg) {
//        SLog.i(TAG, "parseP2pMsg p2pMsg:" + p2pMsg);
//        int start;
//        int end;
//        String download = null;
//        String upload = null;
//        start = p2pMsg.indexOf("download");
//        if (start != -1) {
//            download = p2pMsg.substring(start);
//            start = download.indexOf("flowkbps=");
//        }
//        if (start != -1) {
//            String s = "flowkbps=";
//            start = start + s.length() + 1;
//            end = start;
//            while (download.charAt(end) != '\"') {
//                end++;
//            }
//            download = download.substring(start, end);
//        }
//        start = p2pMsg.indexOf("upload");
//        if (start != -1) {
//            upload = p2pMsg.substring(start);
//            start = upload.indexOf("flowkbps=");
//        }
//        if (start != -1) {
//            String s = "flowkbps=";
//            start = start + s.length() + 1;
//            end = start;
//            while (upload.charAt(end) != '\"') {
//                end++;
//            }
//        }
//        if (download == null) {
//            download = "0";
//        }
//        Integer number = new Integer(download);
//        int result = number / 8;
//        SLog.i(TAG, "parseP2pMsg  result=" + result);
//        return result;
//    }
//
//
//    /**
//     * 下载视频(至多只能同时下载三个视频,存储空间有限)
//     *
//     * @param id
//     * @param path
//     * @param fileName
//     */
//
//    public void downloadChannel(final String id, final String path, final String fileName, final String p2pServer) {
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_DOWNLOAD, id, path, fileName, p2pServer);
//            SLog.i(TAG, "downloadChannel mClientCmd=" + mClientCmd);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    if (e != null) {
//                        SLog.i(TAG, "downloadChannel onFailure=>" + e.toString());
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response != null) {
//                        String result = response.body().string();
//                        SLog.i(TAG, "downloadChannel =" + result);
//                    }
//                }
//            });
//
//        }
//
//    }
//
//
//    public void pauseDownloadChannel(final String id) {
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_PAUSE, id);
//            SLog.i(TAG, "pauseDownloadChannel mClientCmd=" + mClientCmd);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    if (e != null) {
//                        SLog.i(TAG, "pauseDownloadChannel onFailure=>" + e.toString());
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response != null) {
//                        String result = response.body().string();
//                        SLog.i(TAG, "pauseDownloadChannel =" + result);
//                    }
//                }
//            });
//        }
//    }
//
//    public void resumeDownloadChannel(final String id) {
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_RESUME, id);
//            SLog.i(TAG, "resumeDownloadChannel mClientCmd=" + mClientCmd);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    if (e != null) {
//                        SLog.i(TAG, "resumeDownloadChannel onFailure=>" + e.toString());
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response != null) {
//                        String result = response.body().string();
//                        SLog.i(TAG, "resumeDownloadChannel =" + result);
//                    }
//                }
//            });
//        }
//    }
//
//
//    public void stopDownloadChannel(final String id) {
//        if (mForceClient != null) {
//            String mClientCmd = String.format(FTV_CMD_STOP_P2P, id);
//            SLog.i(TAG, "stopDownloadChannel mClientCmd=" + mClientCmd);
//            asyncGet(mClientCmd, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    if (e != null) {
//                        SLog.i(TAG, "stopDownloadChannel onFailure=>" + e.toString());
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response != null) {
//                        String result = response.body().string();
//                        SLog.i(TAG, "stopDownloadChannel =" + result);
//                    }
//                }
//            });
//        }
//    }
//
//    public Observable<String> getDownloadProgress(final String id) {
//        return Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
//                if (mForceClient == null) {
//                    throw new IllegalStateException("mForceClient == null");
//                }
//                String clientCmd = String.format(FTV_CMD_GET_DOWNLOAD_PROGRESS, id);
//                SLog.i(TAG, "getDownloadProgress clientCmd=" + clientCmd);
//                Request request = new Request.Builder().url(clientCmd).build();
//                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
//                emitter.setDisposable(new Disposable() {
//                    @Override
//                    public void dispose() {
//                        call.cancel();
//                    }
//                    @Override
//                    public boolean isDisposed() {
//                        return false;
//                    }
//                });
//
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    String result = response.body().string();
//                    if (!TextUtils.isEmpty(result)) {
//                        emitter.onNext(result);
//                        emitter.onComplete();
//                    } else {
//                        emitter.onError(new HttpException("response.body isEmpty"));
//                    }
//                } else {
//                    emitter.onError(new HttpException(response.code()));
//                }
//            }
//        });
//    }
//
//    private void asyncGet(String url, Callback callback) {
//        Request request = new Request.Builder().url(url).build();
//        OkHttpHelper.getOkHttpClient().newCall(request).enqueue(callback);
//    }
//}
