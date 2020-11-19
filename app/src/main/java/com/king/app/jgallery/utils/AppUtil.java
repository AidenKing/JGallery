package com.king.app.jgallery.utils;

import android.content.pm.PackageManager;
import android.os.Build;

import com.king.app.jgallery.JGApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 10:06
 */
public class AppUtil {

    public static String getAppVersionName() {
        try {
            return JGApplication.Companion.getInstance().getPackageManager().getPackageInfo(JGApplication.Companion.getInstance().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * android P (9.0)
     * @return
     */
    public static boolean isAndroidP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return true;
        }
        return false;
    }

    /**
     * android 9.0开始，部分机型如小米，Android P 后谷歌限制了开发者调用非官方公开API 方法或接口，也就是说，
     * 用反射直接调用源码就会有这样的提示弹窗出现，非 SDK 接口指的是 Android 系统内部使用、
     * 并未提供在 SDK 中的接口，开发者可能通过 Java 反射、JNI 等技术来调用这些接口
     * 用此方法去掉该弹框
     */
    public static void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
