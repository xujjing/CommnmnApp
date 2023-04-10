package com.streambus.commonmodule.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.streambus.basemodule.utils.SLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class LifecycleUtils {
    private static final String TAG = "LifecycleUtils";
    private static int sVisibleCount;
    private static LinkedList<Activity> sActivityList = new LinkedList<>();

    public static Application.ActivityLifecycleCallbacks register(){
        sActivityList.clear();
        return REGISTER;
    }

    private static Application.ActivityLifecycleCallbacks REGISTER = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            sActivityList.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            ++sVisibleCount;
           SLog.d(TAG, "onActivityStarted sVisibleCount=>" + sVisibleCount);
            for (ActivityObserver observer  : sActivitySubjectList) {
                observer.accept(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            --sVisibleCount;
           SLog.d(TAG, "onActivityStopped sVisibleCount=>" + sVisibleCount);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            sActivityList.remove(activity);
        }
    };

    public static Activity getCurrentActivity() {
        if (sActivityList.isEmpty()) {
            return null;
        }
        return sActivityList.getLast();
    }

    public static boolean isVisible() {
       SLog.d(TAG, "isVisible sVisibleCount=>" + sVisibleCount);
        return sVisibleCount > 0;
    }

    public static void finishAll() {
        ArrayList<Activity> activities = new ArrayList<>(sActivityList);
       SLog.d(TAG, "finishAll activities=>" + activities);
        for (Activity activity  : activities) {
            activity.finish();
        }
    }

    private static List<ActivityObserver> sActivitySubjectList = new ArrayList<>();
    public static void subjectActivityStart(ActivityObserver observer) {
        sActivitySubjectList.add(observer);
        if (isVisible()) {
            observer.accept(getCurrentActivity());
        }
    }

    public static void unSubjectActivityStart(ActivityObserver observer) {
        sActivitySubjectList.remove(observer);
    }

    public interface ActivityObserver{
        void accept(Activity activity);
    }

    //
//    class A{
//        void test(){
//            System.out.println("Base A");
//        }
//    }
//
//    class B extends A{
//        void test(){
//            System.out.println("B extends A");
//        }

//          void superTest(){
//              super.test();
//          }
//    }
//
//    class M{
//        public static void main() {
//            B b = new B();
//            //怎么调用B对象父类的test方法，即：b->super.test()
//            b.superTest();
//        }
//    }
//

}
