package com.sparrow.bundle.framework.base.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.ToolbarUtil;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.qrcode.QrCodeResult;
import com.sparrow.bundle.framework.utils.qmui.QMUIStatusBarHelper;
import com.sparrow.bundle.framework.qrcode.QrCodeResult;
import com.sparrow.bundle.framework.utils.qmui.QMUIStatusBarHelper;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;


public class CaptureActivity extends AppCompatActivity {
    public static final String REMIND_STRING_KEY = "remindString";

    private static final int REQUEST_IMAGE = 1000;
    private ToolbarUtil toolbarUtil;
    private CaptureFragment captureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.sparrow.bundle.framework.R.layout.activity_camera);
        QMUIStatusBarHelper.setActivityFullScreen(this);

        captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, com.sparrow.bundle.framework.R.layout.fragment_my_capture);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(com.sparrow.bundle.framework.R.id.fl_zxing_container, captureFragment).commit();

        findViewById(com.sparrow.bundle.framework.R.id.tvew_open_image).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        findViewById(com.sparrow.bundle.framework.R.id.iv_left).setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String remindString = getIntent().getStringExtra(REMIND_STRING_KEY);
        if (!TextUtils.isEmpty(remindString)) {
            View view = captureFragment.getView();
            TextView tvRemind = view.findViewById(com.sparrow.bundle.framework.R.id.tv_remind);
            tvRemind.setText(remindString);
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    String imagePath = getImagePath(uri, null);
                    CodeUtils.analyzeBitmap(imagePath, analyzeCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();

            QrCodeResult result1 = new QrCodeResult();
            result1.reslut = result;
            result1.success = true;
            RxBus.getDefault().post(result1);
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();

            QrCodeResult result1 = new QrCodeResult();
            result1.reslut = "";
            result1.success = true;
            RxBus.getDefault().post(result1);
        }
    };
}
