/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streambus.tinkerlib.util;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.streambus.tinkerlib.bean.UploadInfo;
import com.streambus.tinkerlib.crash.SampleUncaughtExceptionHandler;
import com.streambus.tinkerlib.reporter.SampleLoadReporter;
import com.streambus.tinkerlib.reporter.SamplePatchListener;
import com.streambus.tinkerlib.reporter.SamplePatchReporter;
import com.streambus.tinkerlib.service.SampleResultService;
import com.streambus.tinkerlib.update.UpgradeService;
import com.tencent.tinker.entry.ApplicationLike;
import com.tencent.tinker.lib.library.TinkerLoadLibrary;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.UpgradePatchRetry;

import okhttp3.OkHttpClient;

/**
 * Created by zhangshaowen on 16/7/3.
 */
public class TinkerManager {
    public static boolean IS_FORCE_RESTART = false;
    private static final String TAG = "Tinker.TinkerManager";
    public static final String IS_TINKER_PATCH_SUCCESS = "is_tinker_patch_success";
    public static final String PATCHVERSION_NEW = "patchversion_new";
    public static final String PATCHVERSION_OLD = "patchversion_old";

    private static ApplicationLike applicationLike;
    private static SampleUncaughtExceptionHandler uncaughtExceptionHandler;
    private static boolean isInstalled = false;
    private static Application application;

    public static void setTinkerApplicationLike(ApplicationLike appLike) {
        applicationLike = appLike;
        application = applicationLike.getApplication();
    }

    public static ApplicationLike getTinkerApplicationLike() {
        return applicationLike;
    }

    public static void initFastCrashProtect() {
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = new SampleUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
    }

    public static void setUpgradeRetryEnable(boolean enable) {
        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
    }


    /**
     * all use default class, simply Tinker install method
     */
    public static void sampleInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        TinkerInstaller.install(appLike);
        isInstalled = true;

    }

    /**
     * you can specify all class you want.
     * sometimes, you can only install tinker in some process you want!
     *
     * @param appLike
     */
    public static void installTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        IS_FORCE_RESTART = false;
        //or you can just use DefaultLoadReporter
        LoadReporter loadReporter = new SampleLoadReporter(appLike.getApplication());
        //or you can just use DefaultPatchReporter
        PatchReporter patchReporter = new SamplePatchReporter(appLike.getApplication());
        //or you can just use DefaultPatchListener
        PatchListener patchListener = new SamplePatchListener(appLike.getApplication());
        //you can set your own upgrade patch if you need
        AbstractPatch upgradePatchProcessor = new UpgradePatch();

        TinkerInstaller.install(appLike,
                loadReporter, patchReporter, patchListener,
                SampleResultService.class, upgradePatchProcessor);
        isInstalled = true;
        onFinishedInstall(appLike);

    }

    private static void onFinishedInstall(ApplicationLike appLike) {
        TinkerLoadLibrary.installNavitveLibraryABI(appLike.getApplication(), android.os.Build.CPU_ABI);
    }

    public static void onReceiveUpgradePatch(String savePath, String patchVersion) {
        Utils.put(application, PATCHVERSION_OLD, getPatchVersion());
        Utils.put(application, PATCHVERSION_NEW, patchVersion);
        setTinkerPatchSuccess(false);
        TinkerInstaller.onReceiveUpgradePatch(application, savePath);
    }

    static boolean isTinkerPatchSuccess(Context context) {
        return Utils.get(context, IS_TINKER_PATCH_SUCCESS, false);
    }

    public static void setTinkerPatchSuccess(boolean isSuccess) {
        Utils.put(application, IS_TINKER_PATCH_SUCCESS, isSuccess);
    }

    public static void checkUpdate(Activity activity, OkHttpClient client) {
        UpgradeService.start(activity, client);
    }

    public static UploadInfo getUploadData() {
        PackageManager packageManager= application.getPackageManager();
        PackageInfo packageInfo;
        String packageName = "";
        String versionCode = "";
        String patchVersion = "";
        try {
            packageInfo=packageManager.getPackageInfo(application.getPackageName(),0);
            packageName = packageInfo.packageName;
            versionCode = packageInfo.versionCode + "";
            patchVersion = getPatchVersion();
            TinkerLog.d(TAG, "getUploadData  packageName=" + packageName + " versionCode=" +versionCode + " patchVersion" + patchVersion);
        } catch (Exception e) {
            TinkerLog.e(TAG, e.toString());
        } finally {
            return new UploadInfo(packageName, versionCode, patchVersion);
        }
    }

    static String getPatchVersion() {
        if (! Tinker.with(application).isTinkerLoaded()) {//没有打过补丁（存在某次打补丁失败，回退到基本本的情况）
            Utils.put(application, PATCHVERSION_OLD, "");
            Utils.put(application, PATCHVERSION_NEW, "");
            setTinkerPatchSuccess(false);
        }
        String patchVersion;
        if (TinkerManager.isTinkerPatchSuccess(application)) {
            patchVersion = Utils.get(application, PATCHVERSION_NEW, "");
        } else {//保证发给服务器的补丁版本一定是安装成功的
            patchVersion = Utils.get(application, PATCHVERSION_OLD, "");
        }
        return patchVersion;
    }

    public static void close(Context context) {
        context.stopService(new Intent(context, UpgradeService.class));
    }

}
