package com.streambus.tinkerlib.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.streambus.tinkerlib.service.RestartIntentService;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.File;
import java.util.Locale;

import androidx.core.content.FileProvider;

public class AppUtil {
	public final static String TAG = "AppUtil";

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getAppVersionName(Context context) {
	    String versionName = "";
	    try {  
	        // ---get the package info---  
	        PackageManager pm = context.getPackageManager();
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
	        versionName = pi.versionName;  
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {
	       TinkerLog.w("VersionInfo", "Exception", e);
	    }  
	    return versionName;
	}  
	
	public static String getVersionCode(Context context){
		PackageManager packageManager=context.getPackageManager();
		PackageInfo packageInfo;
		String versionCode="";
		try {
			packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
			versionCode=packageInfo.versionCode+"";
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	// 获取系统语言
	public static String getSystemLanguage(Context context) {

		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();

		// Locale l = Locale.getDefault();
		// String language = String.format("%s-%s", l.getLanguage(),
		// l.getCountry());
		return language;
	}


	// 获取包名
	public static String getAppPackageName(Context context) {
		try {
			String pkName = context.getPackageName();
			return pkName;
		} catch (Exception e) {
		}
		return null;
	}

	public static String getAppName(Context context) {
		String appName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			appName = pi.applicationInfo.loadLabel(pm).toString();
			if (appName == null || appName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return appName;
	}

	public static void showApkInstallPage(Context context, File file) {
		if (android.os.Build.VERSION.SDK_INT < 24) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			Uri fileUri = FileProvider.getUriForFile(context, "com.poptv.live.fileprovider", file);
			intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
			context.startActivity(intent);
		}
	}

	public static void restartApplication(Application application) {
		RestartIntentService.restartAPP(application);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
