package com.sparrow.bundle.framework.base.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxEventObject;
import com.sparrow.bundle.framework.bus.RxSubscriptions;
import com.sparrow.bundle.framework.jsbridge.JSBridge;
import com.sparrow.bundle.framework.jsbridge.JSInterface;
import com.sparrow.bundle.framework.jsbridge.JsCallJava;
import com.sparrow.bundle.framework.jsbridge.JsCallback;
import com.sparrow.bundle.framework.qrcode.QrCode;
import com.sparrow.bundle.framework.qrcode.QrCodeResult;
import com.sparrow.bundle.framework.utils.IOUtil;
import com.sparrow.bundle.framework.utils.KLog;
import com.sparrow.bundle.framework.utils.NetworkUtil;

import org.json.JSONObject;

import java.io.InputStream;

import io.reactivex.disposables.Disposable;

import static android.webkit.WebViewClient.ERROR_HOST_LOOKUP;

public class WebviewFragment extends com.trello.rxlifecycle2.components.support.RxFragment {

    private WebView webView;
    private RelativeLayout rlAction;
    private View viewError;
    private TextView tvError;
    private ImageView imgVewError;

    private boolean loadError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(com.sparrow.bundle.framework.R.layout.fragment_webview, null);
        webView = rootView.findViewById(com.sparrow.bundle.framework.R.id.webview);
        rlAction = rootView.findViewById(com.sparrow.bundle.framework.R.id.rlAction);
        viewError = rootView.findViewById(com.sparrow.bundle.framework.R.id.view_error);
        tvError = rootView.findViewById(com.sparrow.bundle.framework.R.id.errorTextView);
        imgVewError = rootView.findViewById(com.sparrow.bundle.framework.R.id.errorImageView);
        imgVewError.setImageResource(com.sparrow.bundle.framework.R.drawable.icon_error);
        WebSettings ws = webView.getSettings();
        String ua = ws.getUserAgentString();
        ws.setUserAgentString(ua + ";nativeApp");
        ws.setAppCacheEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setJavaScriptEnabled(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setDefaultTextEncodingName("GBK");
        webView.setWebChromeClient(new InjectedChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    loadError(error.getErrorCode());
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    loadError(errorCode);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!loadError) {
                    viewError.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

        rootView.findViewById(com.sparrow.bundle.framework.R.id.error_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load();
            }
        });
        rootView.findViewById(com.sparrow.bundle.framework.R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });

        rootView.findViewById(com.sparrow.bundle.framework.R.id.forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        rootView.findViewById(com.sparrow.bundle.framework.R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load();
            }
        });

        load();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != onViewCreatedListener) {
            onViewCreatedListener.onViewCreated();
        }
    }

    private OnViewCreatedListener onViewCreatedListener;

    public interface OnViewCreatedListener {
        void onViewCreated();
    }

    public void setOnViewCreatedListener(OnViewCreatedListener onViewCreatedListener) {
        this.onViewCreatedListener = onViewCreatedListener;
    }

    private void loadError(int errorCode) {
        webView.setVisibility(View.GONE);
        loadError = true;
        if (!NetworkUtil.isNetworkAvailable(getContext())
                || errorCode == ERROR_HOST_LOOKUP) {
            tvError.setText(com.sparrow.bundle.framework.R.string.network_error);
            viewError.setVisibility(View.VISIBLE);
        }
    }

    private void load() {
        loadError = false;
        Bundle bundle = getArguments();
        if (null != bundle) {
            load(bundle);
        } else {
            Intent intent = getActivity().getIntent();
            load(intent);
        }
    }

    private void load(Bundle bundle) {
        String url = bundle.getString("url");
        String cookie = bundle.getString("cookie");
        String htmlData = bundle.getString("data");
        boolean showActionBar = bundle.getBoolean("showActionBar", false);
        load(url, htmlData, cookie, showActionBar);
    }

    private void load(Intent intent) {
        String url = intent.getStringExtra("url");
        String cookie = intent.getStringExtra("cookie");
        String htmlData = intent.getStringExtra("data");
        boolean showActionBar = intent.getBooleanExtra("showActionBar", false);
        load(url, htmlData, cookie, showActionBar);
    }

    private void load(String url, String htmlData, String cookie, boolean showActionBar) {
        if (!TextUtils.isEmpty(cookie)) {
            //创建CookieSyncManager
            CookieSyncManager.createInstance(getActivity());
            //得到CookieManager
            CookieManager cookieManager = CookieManager.getInstance();

            //使用cookieManager..setCookie()向URL中添加Cookie
            cookieManager.setCookie(url, cookie);
            CookieSyncManager.getInstance().sync();
        }

        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
            showDialog();
        } else {
            if (!TextUtils.isEmpty(htmlData)) {
                loadData(htmlData);
            }
        }
        setActionBarVisibility(showActionBar ? View.VISIBLE : View.GONE);
    }

    public void setActionBarVisibility(int visibility) {
        if (null != rlAction) {
            rlAction.setVisibility(visibility);
        }
    }

    public boolean isActionBarShow() {
        return rlAction.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribeEvent();
    }

    private Disposable mSubscription;

    private void subscribeEvent() {
        mSubscription = RxBus.getDefault().toObservable(RxEventObject.class)
                .compose(bindToLifecycle())
                .subscribe(eventObject -> onSubscribeEvent(eventObject));
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
    }

    /**
     * 取消订阅，防止内存泄漏
     */
    private void unsubscribeEvent() {
        RxSubscriptions.remove(mSubscription);
    }

    public void onSubscribeEvent(RxEventObject eventObject) {
        if ("webviewShowLoading".equals(eventObject.getEvent())) {
            showDialog();
        } else if ("webviewCloseLoading".equals(eventObject.getEvent())) {
            dismissDialog();
        }
    }

    public void loadData(String htmlData) {
        if (null != webView) {
            webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
        } else {
            KLog.i("Open webview is null");
        }
    }

    public void load(String url) {
        if (null != webView) {
            webView.loadUrl(url);
        }
    }

    private KProgressHUD dialog;

    public void showDialog() {
        dialog = KProgressHUD.create(getActivity())
                .setCancellable(true)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
        unsubscribeEvent();
        JSBridge.getInstance().clearJsCallback();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case JSInterface.REQUEST_QR_CODE: {
                    QrCodeResult qrCodeResult = QrCode.parseResult(data.getExtras());
                    JSONObject object = new JSONObject();
                    object.put("result", qrCodeResult.reslut);
                    JSBridge.getInstance().invokeLaskJSCallback(object);
                    break;
                }
                case JSInterface.PHOTOZOOM: {
                    Uri uri = data.getData();
                    InputStream stream = getActivity().getContentResolver().openInputStream(uri);
                    String imageContent = IOUtil.streamToString(stream);
                    JSONObject object = new JSONObject();
                    object.put("result", imageContent);
                    JSBridge.getInstance().invokeLaskJSCallback(object);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class InjectedChromeClient extends WebChromeClient {
        private static final String TAG = "InjectedChromeClient";

        private JsCallJava mJsCallJava;

        public InjectedChromeClient() {
            mJsCallJava = new JsCallJava();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                dismissDialog();
            }
            Log.i("zjw", "newProgress --> " + newProgress);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            JsCallJava.JsCallResult callResult = mJsCallJava.call(view, message);
            if (callResult.code == JsCallJava.RESULT_FAIL) {
                if (null != intercept) {
                    callResult.code = JsCallJava.RESULT_SUCCESS;
                    callResult.message = "call success";

                    Uri uri = Uri.parse(message);
                    String param = uri.getQuery();

                    String path = uri.getPath();
                    String methodName = "";
                    if (!TextUtils.isEmpty(path)) {
                        methodName = path.replace("/", "");
                    }

                    JSBridge.getInstance().addJsCallback(new JsCallback(view, mJsCallJava.getPort(message)));

                    try {
                        intercept.call(methodName, new JSONObject(param));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            result.confirm(new Gson().toJson(callResult));
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (null != listener && !TextUtils.isEmpty(title)
                    && !title.startsWith("http") && !title.startsWith("www")) {
                listener.onReceivedTitle(title);
            }
        }

        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
            showCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }
    }

    /** 视频全屏参数 */
    protected static final FrameLayout.LayoutParams
            COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    /** 视频播放全屏 **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        getActivity().getWindow().getDecorView();

        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getActivity());
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
    }

    /** 隐藏视频全屏 */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webView.setVisibility(View.VISIBLE);
    }

    /** 全屏容器界面 */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    private CallIntercept intercept;

    public void addIntercept(CallIntercept intercept) {
        this.intercept = intercept;
    }

    public interface CallIntercept {
        void call(String methodName, JSONObject param);
    }

    private ChromeClientListener listener;

    public interface ChromeClientListener {
        void onReceivedTitle(String title);
    }

    public void setChromeClientListener(ChromeClientListener listener) {
        this.listener = listener;
    }
}
