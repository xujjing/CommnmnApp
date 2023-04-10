# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#-keep class org.simple.** { *; }
#-keep interface org.simple.** { *; }
#-keepclassmembers class * {
#    @org.simple.eventbus.Subscriber <methods>;
#}
#-keepattributes *Annotation*

#忽略警告，避免打包时某些警告出现
-ignorewarnings

 #指定压缩级别
-optimizationpasses 5

#包明不混合大小写
-dontusemixedcaseclassnames

#不跳过非公共的库的类成员
-dontskipnonpubliclibraryclassmembers

#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#把混淆类中的方法名也混淆了
-useuniqueclassmembernames

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

#将文件来源重命名为“SourceFile”字符串
-renamesourcefileattribute SourceFile

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 避免混淆Annotation、内部类、泛型、匿名类
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable.Creator *;
    public static final android.os.Parcelable.ClassLoaderCreator *;
}
#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
        static final long serialVersionUID;
        private static final java.io.ObjectStreamField[] serialPersistentFields;
        !static !transient <fields>;
        !private <fields>;
        !private <methods>;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
}
#Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
-keep public class * extends androidx.fragment.app.Fragment
# 保留四大组件，自定义的Application等这些类不被混淆
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
## 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# rxjava retrofit
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-keep class com.okhttp3.internal.**{*;}
-dontwarn okio.**

-keep class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
# Gson
-keep class com.google.gson.stream.* { *; }
-keepattributes EnclosingMethod

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*; }
#tinker热更新 begin
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
# tinker混淆规则
-dontwarn com.tencent.tinker.**
-keep class com.tencent.tinker.** { *; }
#tinker热更新 end
#ijkplayer
-keep class tv.danmaku.ijk.media.player.** {*;}
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer{*;}
#-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi{*;}
#登录 login相关
# greendao
-keep class org.greenrobot.greendao.**{*;}
-keep class net.sqlcipher.database.**{*;}

-keep public interface org.greenrobot.greendao.**
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
         public static java.lang.String TABLENAME;
         public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
         public static void createTable(org.greenrobot.greendao.database.Database, boolean);
 }

-keep class **$Properties
-keep class net.sqlcipher.database.**{*;}
-keep public interface net.sqlcipher.database.**
-dontwarn net.sqlcipher.database.**
-dontwarn org.greenrobot.greendao.**

# 去掉与 de.greenrobot.daogenerator jar包相关的
-dontnote de.greenrobot.daogenerator.**
-keep class de.greenrobot.daogenerator.** {*;}
#Glide begin
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#Glide end
# eventbus
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#-keep class com.streambus.requestapi.** {
#    *;
#}

-keep class com.streambus.requestapi.bean.* {
    *;
}

-keep class com.streambus.requestapi.NativeLogin {
    *;
}

#-keep class com.aliyun.security.yunceng.android.sdk.** {
# *;
#}

-keep class com.stv.iptv.app.MyApplication {
    *;
}

-keep class com.streambus.livemodule.event.** { *; }
-keep class com.streambus.livemodule.bean.** { *; }
# EventBus
-keep class org.simple.** { *; }
-keep interface org.simple.** { *; }
-keepclassmembers class * {
    @org.simple.eventbus.Subscriber <methods>;
}
#公共库的bean
-keep class com.streambus.commonmodule.bean.** {
 *;
}
#日志抓取begin
-keep class com.yoostar.fileloggingutil.** {
 *;
}
-keep class com.yoostar.fileloggingutil.FileTreeIo.** {
 *;
}
#日志抓取end
#umeng begin
-keep class com.umeng.** {*;}
-keep class com.uc.** {*;}   #崩溃／ANR类型
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.zui.** {*;}
-keep class com.miui.** {*;}
-keep class com.heytap.** {*;}
-keep class a.** {*;}
-keep class com.vivo.** {*;}

-keep public class com.iptv.mobile.quick.R$*{
public static final int *;
}

#umeng push begin
-dontwarn com.umeng.**
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**
-dontwarn com.meizu.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class com.meizu.** {*;}
-keep class org.apache.thrift.** {*;}

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep public class **.R$*{
   public static final int *;
}
#umeng push end

#去除log输出
-assumenosideeffects class android.util.Log{
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
#umeng end

#===================== Youtube parse begin ====================================

# Add *one* of the following rules to your Proguard configuration file.
# Alternatively, you can annotate classes and class members with @androidx.annotation.Keep

# keep the class and specified members from being removed or renamed
-keep class okhttp3.OkHttpClient$Builder { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.OkHttpClient$Builder { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.OkHttpClient$Builder { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.OkHttpClient$Builder { *; }

# keep the class and specified members from being removed or renamed
-keep class okhttp3.Request$Builder { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.Request$Builder { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.Request$Builder { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.Request$Builder { *; }

# keep the class and specified members from being removed or renamed
-keep class okhttp3.ResponseBody { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.ResponseBody { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.ResponseBody { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.ResponseBody { *; }

# keep the class and specified members from being removed or renamed
-keep class okhttp3.Call { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.Call { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.Call { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.Call { *; }

# keep the class and specified members from being removed or renamed
-keep class okhttp3.Response { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.Response { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.Response { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.Response { *; }
# keep the class and specified members from being removed or renamed
-keep class okhttp3.Request$Builder { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.Request$Builder { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.Request$Builder { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.Request$Builder { *; }
# keep the class and specified members from being removed or renamed
-keep class okhttp3.OkHttpClient { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class okhttp3.OkHttpClient { *; }

# keep the class and specified members from being renamed only
-keepnames class okhttp3.OkHttpClient { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class okhttp3.OkHttpClient { *; }
# keep the class and specified members from being removed or renamed
-keep class android.text.TextUtils { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class android.text.TextUtils { *; }

# keep the class and specified members from being renamed only
-keepnames class android.text.TextUtils { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class android.text.TextUtils { *; }
# keep the class and specified members from being removed or renamed
-keep class org.json.JSONObject { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class org.json.JSONObject { *; }

# keep the class and specified members from being renamed only
-keepnames class org.json.JSONObject { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class org.json.JSONObject { *; }
# keep the class and specified members from being removed or renamed
-keep class org.json.JSONArray { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class org.json.JSONArray { *; }

# keep the class and specified members from being renamed only
-keepnames class org.json.JSONArray { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class org.json.JSONArray { *; }

# keep the class and specified members from being removed or renamed
-keep class java.io.BufferedReader { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.io.BufferedReader { *; }

# keep the class and specified members from being renamed only
-keepnames class java.io.BufferedReader { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.io.BufferedReader { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.List { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.List { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.List { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.List { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.ArrayList { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.ArrayList { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.ArrayList { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.ArrayList { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.Iterator { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.Iterator { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.Iterator { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.Iterator { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.Random { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.Random { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.Random { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.Random { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.concurrent.CancellationException { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.concurrent.CancellationException { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.concurrent.CancellationException { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.concurrent.CancellationException { *; }
# keep the class and specified members from being removed or renamed
-keep class java.util.concurrent.TimeUnit { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.util.concurrent.TimeUnit { *; }

# keep the class and specified members from being renamed only
-keepnames class java.util.concurrent.TimeUnit { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.util.concurrent.TimeUnit { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.String { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.String { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.String { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.String { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.StringBuilder { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.StringBuilder { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.StringBuilder { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.StringBuilder { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.StringBuffer { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.StringBuffer { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.StringBuffer { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.StringBuffer { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.NoSuchMethodError { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.NoSuchMethodError { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.NoSuchMethodError { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.NoSuchMethodError { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.NoSuchMethodError { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.NoSuchMethodError { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.NoSuchMethodError { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.NoSuchMethodError { *; }

# keep the class and specified members from being removed or renamed
-keep class java.lang.RuntimeException { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.RuntimeException { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.RuntimeException { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.RuntimeException { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.IllegalStateException { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.IllegalStateException { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.IllegalStateException { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.IllegalStateException { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.Object { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.Object { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.Object { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.Object { *; }
# keep the class and specified members from being removed or renamed
-keep class java.lang.Character { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class java.lang.Character { *; }

# keep the class and specified members from being renamed only
-keepnames class java.lang.Character { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class java.lang.Character { *; }
#===================== Youtube parse end ====================================
#去掉d、v等级的日志打印
#-assumenosideeffects class android.util.Log {
#   public static *** d(...);
#   public static *** v(...);
#}