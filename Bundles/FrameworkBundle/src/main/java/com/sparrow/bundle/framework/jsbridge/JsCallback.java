package com.sparrow.bundle.framework.jsbridge;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;

import com.sparrow.bundle.framework.utils.KLog;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class JsCallback {
    private static final String CALLBACK_JS_FORMAT = "javascript:JSBridge.onComplete('%s', %s);";

    private WeakReference<WebView> mWebViewRef;
    private String mSid;
    private boolean mCouldGoOn = true;
    private int mIsPermanent;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public JsCallback(WebView webView, String sid) {
        mSid = sid;
        mWebViewRef = new WeakReference<>(webView);
        setPermanent(true);
    }

    public String getSid() {
        return mSid;
    }

    public void apply(boolean isSuccess, String message, JSONObject object) {
        if (null == mWebViewRef || null == mWebViewRef.get()) {
            KLog.e("The WebView related to the JsCallback has been recycled");
            return;
        }
        if (!mCouldGoOn) {
            KLog.i("The JsCallback isn't permanent,cannot be called more than once");
        }
        JSONObject result = new JSONObject();

        try {
            JSONObject code = new JSONObject();
            code.put("code", isSuccess ? 0 : 1);
            if (!isSuccess && !TextUtils.isEmpty(message)) {
                code.putOpt("msg", message);
            }
            if (isSuccess) {
                code.putOpt("msg", TextUtils.isEmpty(message) ? "SUCCESS" : message);
            }
            result.putOpt("status", code);
            if (null != object) {
                result.putOpt("data", object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String jsFunc = String.format(CALLBACK_JS_FORMAT, mSid, String.valueOf(result));

        if (mWebViewRef != null && mWebViewRef.get() != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebViewRef.get().loadUrl(jsFunc);
                }
            });
        }
        mCouldGoOn = mIsPermanent > 0;
    }

    public void setPermanent(boolean value) {
        mIsPermanent = value ? 1 : 0;
    }

    public static class JsCallbackException extends Exception {
        public JsCallbackException(String message) {
            super(message);
        }
    }
}
