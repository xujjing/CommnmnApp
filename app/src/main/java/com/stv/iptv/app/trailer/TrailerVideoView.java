package com.stv.iptv.app.trailer;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SimpleObserver;
import com.streambus.commonmodule.bean.Trailers;
import com.streambus.commonmodule.utils.YoutubeParseUtils;
import com.stv.iptv.app.R;
import com.trello.rxlifecycle3.android.FragmentEvent;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import tv.danmaku.ijk.media.widget.media.IMediaPlayer;
import tv.danmaku.ijk.media.widget.media.IRenderView;
import tv.danmaku.ijk.media.widget.media.IjkVideoView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/5/24
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class TrailerVideoView extends FrameLayout {
    private static final String TAG = "TrailerVideoView";
    private IjkVideoView mIjkVideoView;
    private Subscription mSubscription;
    private Observable<FragmentEvent> mLifecycle;
    private View mIvHomeBg;
    private Disposable mYoutubeRemoveDisable;

    public TrailerVideoView(Context context) {
        this(context, null);
    }

    public TrailerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIvHomeBg = findViewById(R.id.iv_home_bg);
        mIjkVideoView = findViewById(R.id.ijk_video_view);
        mIjkVideoView.setPlayerType(IjkVideoView.PLAYER_IjkExoMediaPlayer);
        mIjkVideoView.setVideoSize(IRenderView.AR_ASPECT_FILL_PARENT);
        mIjkVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                SLog.i(TAG,"IMediaPlayer onPrepared");
                mIvHomeBg.setVisibility(GONE);
                // iMediaPlayer.start(); 根据IjkVideoView.mTargetState来自行决定
            }
        });
        mIjkVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (i == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    mIvHomeBg.setVisibility(GONE);
                }
                return false;
            }
        });
        mIjkVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                mYoutubeRemoveDisable = Observable.just("").subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        YoutubeParseUtils.removeYoutubeUrl(mIjkVideoView.getVideoURI().toString());
                    }
                }).subscribe();

                if (mSubscription != null) {
                    mSubscription.request(1);
                }
                return false;
            }
        });
        mIjkVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                if (mSubscription != null) {
                    mSubscription.request(1);
                }
            }
        });
    }

    public void setTrailers(List<Trailers> trailers) {

        SLog.d(TAG, "setTrailers   trailers=>" + trailers);
        reset();
        if (trailers != null && !trailers.isEmpty()) {
            requestTrailers(trailers);
        }
    }

    private void requestTrailers(List<Trailers> trailers) {
        SLog.d(TAG, "requestTrailers trailers=>" + trailers);
        Flowable.just(trailers)
                .flatMap(new Function<List<Trailers>, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(List<Trailers> trailers) throws Exception {
                        SLog.i(TAG,"requestTrailers  Publisher<String> apply(List<Trailers> trailers");
                        ArrayList<String> normalVideoList = new ArrayList<>();
                        HashSet<String> youtubeVideoSet = new HashSet<>();
                        for (Trailers trailer : trailers) {
                            String url = trailer.getAddress();
                            String path = url.toLowerCase().split("\\?")[0];
                            if (path.endsWith(".mp4") || path.endsWith(".m3u8") || path.endsWith(".ts")) {
                                normalVideoList.add(url);
                                continue;
                            }

                            String videoId = YoutubeParseUtils.parseIDfromVideoUrl(url);
                            if (!TextUtils.isEmpty(videoId)) {
                                youtubeVideoSet.add(videoId);
                                continue;
                            }
                            normalVideoList.add(url);
                        }
                        Flowable<String> normalVideoFlowable = null;
                        if (!normalVideoList.isEmpty()) {
                            normalVideoFlowable = Flowable.fromIterable(normalVideoList);
                        }
                        Flowable<String> youtubeVideoFlowable = null;
                        if (!youtubeVideoSet.isEmpty()) {
                            youtubeVideoFlowable = Flowable.fromIterable(youtubeVideoSet)
                                    .flatMapMaybe(new Function<String, MaybeSource<? extends String>>() {
                                        private long lastTime = SystemClock.uptimeMillis();
                                        @Override
                                        public MaybeSource<? extends String> apply(String videoId) throws Exception {
                                            long uptimeMillis = SystemClock.uptimeMillis();
                                            long delayMillis = 3000 + lastTime - uptimeMillis;
                                            //SLog.i(TAG, "youtubeVideoFlowable delayMillis=>" + delayMillis + " ,Thread.name=" + Thread.currentThread().getName());
                                            if (delayMillis > 0) {
                                                Thread.sleep(delayMillis);
                                            }
                                            lastTime = SystemClock.uptimeMillis();
//                                            return Maybe.just("http://flv2.bn.netease.com/videolib3/1505/29/DCNOo7461/SD/DCNOo7461-mobile.mp4");
                                            return YoutubeParseUtils.parseUrl(videoId, false, "720p").subscribeOn(Schedulers.io())
                                                    .onErrorResumeNext(new Function<Throwable, MaybeSource<? extends String>>() {
                                                        @Override
                                                        public MaybeSource<? extends String> apply(Throwable throwable) throws Exception {
                                                            SLog.w(TAG, "requestTrailers requestYoutubeUrl throwable", throwable);
                                                            return Maybe.just("");
                                                        }
                                                    });
                                        }
                                    }, false,1);

                        }
                        if (normalVideoFlowable != null && youtubeVideoFlowable != null) {
                            return Flowable.concat(normalVideoFlowable, youtubeVideoFlowable);
                        }
                        if (normalVideoFlowable != null) {
                            return normalVideoFlowable;
                        }
                        if (youtubeVideoFlowable != null) {
                            return youtubeVideoFlowable;
                        }
                        return Flowable.empty();
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String url) throws Exception {
                        SLog.d(TAG, "filter test url=>" + url);
                        return !TextUtils.isEmpty(url);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), false, 1)
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        SLog.i(TAG,"request Trailers doFinally");
                    }
                })
                .subscribe(new FlowableSubscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        SLog.i(TAG, "request Trailers onSubscribe");
                        mSubscription = new Subscription() {
                            @Override
                            public void request(long n) {
                                SLog.d(TAG, "onSubscribe request");
                                s.request(n);
                            }
                            @Override
                            public void cancel() {
                                SLog.d(TAG, "onSubscribe cancel");
                                s.cancel();
                            }
                        };
                        mSubscription.request(1);
                    }
                    @Override
                    public void onNext(String url) {
                        mLifecycle.subscribe(new SimpleObserver<FragmentEvent>() {
                            @Override
                            public void onNext(FragmentEvent event) {
                                SLog.i(TAG, "request Trailers onNext currentFragmentEvent= " + event + " url=>" + url);
                                if (event == FragmentEvent.RESUME) {
                                    disposable.dispose();
                                    mIjkVideoView.setVideoPath(url);
                                    mIjkVideoView.start();
                                } else if (event == FragmentEvent.DESTROY) {
                                    disposable.dispose();
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(Throwable t) {
                        SLog.e(TAG, "request Trailers onError", t);
                    }
                    @Override
                    public void onComplete() {
                        SLog.i(TAG, "request Trailers onComplete");
                    }
                });
    }

    public void reset() {
        mIvHomeBg.setVisibility(VISIBLE);
        onDestroy();
        mIjkVideoView.refreshRender();
    }

    private void onDestroy() {
        if (mSubscription != null) {
            mSubscription.cancel();
            mSubscription = null;
        }
        if (null != mYoutubeRemoveDisable) {
            mYoutubeRemoveDisable.dispose();
            mYoutubeRemoveDisable = null;
        }
        mIjkVideoView.stopPlayback();
    }


    public void setViewLifecycle(Observable<FragmentEvent> lifecycle) {
        mLifecycle = lifecycle;
        Disposable subscribe = mLifecycle.subscribe(new Consumer<FragmentEvent>() {
            @Override
            public void accept(FragmentEvent event) throws Exception {
                if (event == FragmentEvent.START) {
                    mIjkVideoView.start();
                } else if (event == FragmentEvent.STOP) {
                    mIjkVideoView.pause();
                } else if (event == FragmentEvent.DESTROY) {
                    onDestroy();
                }
            }
        });
    }
}
