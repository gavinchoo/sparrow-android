package com.sparrow.bundle.network.exception;

import android.text.TextUtils;
import android.util.Pair;

import com.sparrow.bundle.network.utils.MessageUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;


/**
 * @author zhangshaopeng
 * @date 2016/8/31 0031
 * @description 异常统计处理类
 */
public class AppException {
    /**
     * 应用异常
     */
    @Deprecated
    public static final int APP_ERROR = -1;
    /**
     * 网络异常
     */
    public static final int NETWORK_ERROR = 1;
    public static final String NETWORK_ERROR_TEXT = "网络连接失败，请检查网络设置";
    /**
     * 网络超时
     */
    public static final String NETWORK_TIMEOUT = "2";
    public static final String NETWORK_TIMEOUT_TEXT = "请求超时,请稍后重试";

    /**
     * 404
     */
    public static final String NETWORK_NOT_FOUND = "404";
    public static final String NETWORK_NOT_FOUND_TEXT = "未找到访问接口";

    /**
     * 网络超时
     */
    public static final int NETWORK_NO = 4;
    public static final String NETWORK_NO_TEXT = "没有发现可用的网络,请检查您的网络连接!";

    /**
     * 数据解析异常
     */
    public static final int DATA_ERROR = 4;

    public static final String DATA_ERROR_TEXT = "服务器出错啦，请稍后重试...";


    /**
     * 登陆超时
     */
    public static final String LOGIN_TIMEOUT = "5";
    public static final String LOGIN_TIMEOUT_TEXT = "登录超时，请重新登录";

    /**
     * 环境错误_environment
     */
    public static final String ENVIRONMENT_ERROR = "6";
    public static final String ENVIRONMENT_ERROR_TEXT = "网络环境错误，请确认你的网络环境";

    /**
     * 返回数据为空
     */
    public static final String RESULT_NULL_ERROR = "100";
    public static final String RESULT_NULL_ERROR_TEXT = "暂无数据";


    public static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    /**
     * 处理服务器返回的异常
     *
     * @param code 错误代码
     * @param msg  错误消息
     */
    public static boolean loadError(MessageUtils messageUtils, String code, String msg) {
        if (messageUtils == null) {
            return false;
        }
        try {
            if (!TextUtils.isEmpty(msg)) {
                messageUtils.showMsg(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (code) {

        }
        return true;
    }


    public static void loadServerErrorByMessage(MessageUtils messageUtils, Throwable e) {
        if (e instanceof ServerException) {
            AppException.loadError(messageUtils, ((ServerException) e).getCode(), e.getMessage());
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) {
            AppException.loadError(messageUtils, AppException.ENVIRONMENT_ERROR, AppException.ENVIRONMENT_ERROR_TEXT);
        } else if (e instanceof SocketTimeoutException) {
            AppException.loadError(messageUtils, AppException.NETWORK_TIMEOUT, AppException.NETWORK_TIMEOUT_TEXT);
        }
    }

    public static Pair<String, String> getErrorContent(Throwable e) {
        if (e instanceof ServerException) {
            return new Pair<>(e.getMessage(), ((ServerException) e).getCode());
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return new Pair<>(AppException.ENVIRONMENT_ERROR_TEXT, AppException.ENVIRONMENT_ERROR);
        } else if (e instanceof SocketTimeoutException) {
            return new Pair<>(AppException.NETWORK_TIMEOUT_TEXT, AppException.NETWORK_TIMEOUT);
        } else if (e instanceof HttpException) {
            HttpException exception = (HttpException) e;
            String errorMsg = "";
            if (exception.code() == 502) {
                errorMsg = "网络连接失败";
            } else if (exception.code() == 404) {
                errorMsg = AppException.NETWORK_NOT_FOUND_TEXT;
            } else if (exception.code() == 504) {
                errorMsg = AppException.NETWORK_ERROR_TEXT;
            } else {
                errorMsg = AppException.ENVIRONMENT_ERROR_TEXT;
            }
            return new Pair<>(errorMsg, "" + exception.code());
        } else {
            return new Pair<>(e.getMessage(), "0");
        }
    }
}
