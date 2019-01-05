package com.sparrow.bundle.framework.jsbridge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.WebView;
import android.widget.Toast;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.bundle.PhotoBundle;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxEventObject;
import com.sparrow.bundle.framework.qrcode.QrCode;
import com.sparrow.bundle.framework.utils.NetworkUtil;
import com.sparrow.bundle.framework.utils.SPUtils;
import com.sparrow.bundle.framework.utils.SPUtils;
import com.sparrow.bundle.framework.utils.ToastUtils;

import org.json.JSONObject;

import java.util.Iterator;

public class JSInterface implements IInject {

    public static final String IMAGE_UNSPECIFIED = "image/*";//随意图片类型
    public static final int PHOTOZOOM = 2; // 缩放
    public static final int REQUEST_QR_CODE = 100;

    /**
     * 培训H5获取用户信息
     *
     */
    public static void userInfo(WebView webView, JSONObject param, final JsCallback callback) {
        if (null != callback) {
            try {
                JSONObject object = new JSONObject(SPUtils.getInstance().getString("train_userInfo"));
                JSBridge.getInstance().invokeJSCallback(callback, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取网络状态
     */
    public static void netWorkState(WebView webView, JSONObject param, final JsCallback callback) {
        if (null != callback) {
            try {
                JSONObject object = new JSONObject("{'state':"+ NetworkUtil.getNetworkState(webView.getContext()) +"}");
                JSBridge.getInstance().invokeJSCallback(callback, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * toast
     *
     * @param webView 浏览器
     * @param param   提示信息
     */
    public static void toast(WebView webView, JSONObject param, final JsCallback callback) {
        RxEventObject eventObject = new RxEventObject();
        eventObject.setEvent("webviewToast");
        RxBus.getDefault().post(eventObject);
        String message = param.optString("message");
        int isShowLong = param.optInt("isShowLong");
        Toast.makeText(webView.getContext(), message, isShowLong == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
        if (null != callback) {
            try {
                JSONObject object = new JSONObject();
                object.put("result", true);
                JSBridge.getInstance().invokeJSCallback(callback, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showLoading(WebView webView, JSONObject param, final JsCallback callback) {
        RxEventObject object = new RxEventObject();
        object.setEvent("webviewShowLoading");
        RxBus.getDefault().post(object);
    }

    public static void closeLoading(WebView webView, JSONObject param, final JsCallback callback) {
        RxEventObject object = new RxEventObject();
        object.setEvent("webviewCloseLoading");
        RxBus.getDefault().post(object);
    }

    public static void chooseFile(WebView webView, JSONObject param, final JsCallback callback) {
        JSBridge.getInstance().addJsCallback(callback);

        // 调用系统的相冊
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGE_UNSPECIFIED);
        // 调用剪切功能
        ((Activity) webView.getContext()).startActivityForResult(intent, PHOTOZOOM);
    }

    public static void choosePhoto(WebView webView, JSONObject param, final JsCallback callback) {
        JSBridge.getInstance().addJsCallback(callback);
        if (null != param) {
            String photoType = param.optString("photoType");
            String selectType = param.optString("selectType");
            boolean mulitPhoto = param.optBoolean("mulitPhoto");
            boolean selectOriginal = param.optBoolean("selectOriginal");
            int maxSelectable = param.optInt("maxSelectable");
            new PhotoBundle((Activity) webView.getContext())
                    .mulitPhoto(mulitPhoto)
                    .maxSelectable(maxSelectable)
                    .showSelectOriginal(selectOriginal)
                    .selectType(PhotoBundle.SelectType.getValue(selectType))
                    .photoType(PhotoBundle.PhotoType.getValue(photoType))
                    .show();
        } else {
            new PhotoBundle((Activity) webView.getContext())
                    .show();
        }
    }

    public static void openQrcode(WebView webView, JSONObject param, final JsCallback callback) {
        JSBridge.getInstance().addJsCallback(callback);
        QrCode.open(((Activity) webView.getContext()), REQUEST_QR_CODE, webView.getContext().getResources().getString(com.sparrow.bundle.framework.R.string.scan_business_license_remind));
    }

    public static void openNativeView(WebView webView, JSONObject param, final JsCallback callback) {
        try {
            String scheme = param.getString("scheme");
            String host = param.getString("host");
            String url = String.format("%s://%s", scheme, host);
            JSONObject params = param.getJSONObject("params");
            Iterator iterator = params.keys();
            Bundle bundle = new Bundle();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = params.getString(key);
                bundle.putString(key, value);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.putExtras(bundle);
            webView.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeNativeView(WebView webView, JSONObject param, final JsCallback callback) {
        ((Activity) webView.getContext()).finish();
    }
}