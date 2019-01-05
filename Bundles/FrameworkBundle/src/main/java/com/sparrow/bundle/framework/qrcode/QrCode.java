package com.sparrow.bundle.framework.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.sparrow.bundle.framework.base.ui.activity.CaptureActivity;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxSubscriptions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.w3c.dom.Text;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class QrCode {

    public static final int REQUEST_QR_CODE = 100;

    private Disposable mSubscription;

    public void register(Consumer<QrCodeResult> consumer) {
        mSubscription = RxBus.getDefault().toObservable(QrCodeResult.class).subscribe(consumer);
    }

    public void unregister(Context context) {
        RxSubscriptions.remove(mSubscription);
    }

    public static void open(Activity context) {
        open(context, REQUEST_QR_CODE);
    }

    public static void open(Activity context, int requestCode) {
        open(context, requestCode, null);
    }

    public static void open(Activity context, String remindString){
        open(context, REQUEST_QR_CODE, remindString);
    }

    public static void open(Activity context, int requestCode, String remindString) {
        RxPermissions rxPermissions = new RxPermissions(context);
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest
                .permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(context, CaptureActivity.class);
                    if (!TextUtils.isEmpty(remindString)) {
                        intent.putExtra(CaptureActivity.REMIND_STRING_KEY, remindString);
                    }
                    context.startActivityForResult(intent, requestCode);
                } else {
                    Toast.makeText(context, com.medical.bundle.photo.R.string.permission_request_denied,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public static QrCodeResult parseResult(Bundle bundle) {
        QrCodeResult qrCodeResult = new QrCodeResult();
        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
            String result = bundle.getString(CodeUtils.RESULT_STRING);
            qrCodeResult.reslut = result;
            qrCodeResult.success = true;
        } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
            qrCodeResult.reslut = "";
            qrCodeResult.success = false;
        }
        return qrCodeResult;
    }




}
