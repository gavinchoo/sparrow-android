package com.sparrow.bundle.photo.matisse.upload;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CircleProgressBar;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CircleProgressBar;

/**
 * Created by WEI on 2017/7/27.
 */

public class UploadDialog extends Dialog {

    private TextView tv;
    private CircleProgressBar mProgress;

    public UploadDialog(Context context) {
        super(context, com.sparrow.bundle.photo.R.style.LoadingDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sparrow.bundle.photo.R.layout.dialog_loading_dialog);
        tv = findViewById(com.sparrow.bundle.photo.R.id.tv);
        tv.setText("正在上传.....");
        mProgress = findViewById(com.sparrow.bundle.photo.R.id.progress);
        this.setCanceledOnTouchOutside(false);
    }

    private Handler mHandler = new Handler();
    private int dotCount = 1;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            dotCount++;
            if (dotCount >= 6) {
                dotCount = 0;
            }
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i <= dotCount; i++) {
                buffer.append(".");
            }
            tv.setText("正在上传" + buffer);
            mHandler.postDelayed(runnable, 300);
        }
    };

    @Override
    public void show() {
        if (!isShowing()) {
            mHandler.postDelayed(runnable, 300);
        }
        super.show();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(runnable);
    }

    public void setProgress(int progress) {
        mProgress.setProgress(progress);
    }
}
