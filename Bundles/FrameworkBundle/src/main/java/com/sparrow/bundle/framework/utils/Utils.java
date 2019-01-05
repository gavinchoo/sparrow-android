package com.sparrow.bundle.framework.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by goldze on 2017/5/14.
 * 常用工具类
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(@NonNull final Context context) {
        if (null != context)
            Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }
        throw new NullPointerException("should be initialized in application");
    }

    public static <T> T objectNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public interface WorkCallBack {
        void success();

        void cancel();
    }


    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime = 0;

    public static boolean isDoubleClick() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            return false;
        }
        return true;
    }
}