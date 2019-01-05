package com.sparrow.bundle.framework.base.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.ToolbarUtil;
import com.sparrow.bundle.framework.base.ui.fragment.WebviewFragment;
import com.sparrow.bundle.framework.qrcode.QrCode;
import com.sparrow.bundle.framework.base.ui.fragment.WebviewFragment;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class WebviewActivity extends SwipeBackActivity {

    private ToolbarUtil toolbarUtil;
    private WebviewFragment webviewFragment;
    private String mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sparrow.bundle.framework.R.layout.activity_webview);
        //QrCode.open(WebviewActivity.this);
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},0);
        }
        webviewFragment = new WebviewFragment();
        webviewFragment.setChromeClientListener(title -> {
            if (TextUtils.isEmpty(mTitle)) {
                getToolbar().setTitle(title);
            }
            initToolBar();
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.sparrow.bundle.framework.R.id.webview_fragment, webviewFragment)
                .commit();
        getToolbar().setTitle("");

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        if (!TextUtils.isEmpty(mTitle)) {
            getToolbar().setTitle(mTitle);
        }

        boolean showTitleBar = intent.getBooleanExtra("showTitleBar", true);
        if (showTitleBar){
            initToolBar();
        }else {
            getToolbar().hideToolBar();
        }
    }

    private void initToolBar() {
        getToolbar().showLeftIndicator();
        getToolbar().setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webviewFragment.isActionBarShow()) {
                    finish();
                } else {
                    if (webviewFragment.canGoBack()) {
                        webviewFragment.goBack();
                    } else {
                        finish();
                    }
                }
            }
        });
    }

    public ToolbarUtil getToolbar() {
        if (null == toolbarUtil) {
            toolbarUtil = new ToolbarUtil(this);
        }
        return toolbarUtil;
    }

    public static void openAddCookie(Activity context, String url, String cookie, boolean showActionBar) {
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("cookie", cookie);
        intent.putExtra("showActionBar", showActionBar);
        context.startActivity(intent);
    }

    public static void open(Activity context, String url, String title, boolean showActionBar, boolean showTitleBar) {
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.putExtra("showActionBar", showActionBar);
        intent.putExtra("showTitleBar", showTitleBar);
        context.startActivity(intent);
    }

    public static void open(Activity context, String url, boolean showActionBar) {
        open(context, url, null, showActionBar, true);
    }

    public static void open(Activity context, String url, String title) {
        open(context, url, title, false, true);
    }

    public static void open(Activity context, String url) {
        open(context, url, null, false, true);
    }

    public static void openData(Activity context, String data) {
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("data", data);
        context.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        webviewFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webviewFragment.canGoBack()) {
                webviewFragment.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }


//    /**
//     * 申请相机权限
//     *
//     * @param context
//     * @param photoFromCamera  拍照保存图片路径
//     *
//     *
//     * @see {https://github.com/yanzhenjie/AndPermission}
//     * */
//
//    public static void requestCameraPermission(final Context context, final String photoFromCamera){
//        //API <23
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//            UIRouter.JumpToCameraActivity(context,photoFromCamera);
//        }else {
//            //API >=23
//            AndPermission.with(context)
//                    .requestCode(PERMISSION_MEDIA_REQUEST_CODE)
//                    .permission(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
//                    .rationale(new RationaleListener() {
//                        @Override
//                        public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
//                            // 此对话框可以自定义，调用rationale.resume()就可以继续申请。
//                            AndPermission.rationaleDialog(context, rationale).show();
//                        }
//                    })
//                    .callback(new PermissionListener() {
//                        @Override
//                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
//                            // 权限申请成功回调。
//                            if(requestCode == .PERMISSION_MEDIA_REQUEST_CODE) {
//                                UIRouter.JumpToCameraActivity(context,photoFromCamera);
//                            }
//                        }
//
//                        @Override
//                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
//                            // 权限申请失败回调。
//                            if(requestCode ==PERMISSION_MEDIA_REQUEST_CODE) {
//                                ToastView.showToast(context,"拒绝授权");
//                            }
//                        }
//                    })
//                    .start();
//        }
//    }
}
