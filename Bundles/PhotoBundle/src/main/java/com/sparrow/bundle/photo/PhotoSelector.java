package com.sparrow.bundle.photo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sparrow.bundle.photo.camera.build.JCamera;
import com.sparrow.bundle.photo.camera.build.SelectionSpec;
import com.sparrow.bundle.photo.matisse.Matisse;
import com.sparrow.bundle.photo.matisse.MimeType;
import com.sparrow.bundle.photo.matisse.engine.impl.GlideEngine;
import com.sparrow.bundle.photo.matisse.filter.Filter;
import com.sparrow.bundle.photo.matisse.filter.GifSizeFilter;
import com.sparrow.bundle.photo.matisse.internal.entity.CaptureStrategy;
import com.sparrow.bundle.photo.matisse.upload.UploadParams;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.net.URLEncoder;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by WEI on 2017/7/28.
 */

public class PhotoSelector {

    public static final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    public static final int REQUEST_CODE_CHOOSE = 23;
    public static final int REQUEST_CODE_CAMERA = 24;

    private PhotoParams mParams = new PhotoParams();
    private Activity mContext;
    private PopupWindow mPopupWindow;

    public PhotoSelector(Activity context) {
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.pop_select_pic, null);
        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setOutsideTouchable(true);

        TextView tvewCancel = view.findViewById(R.id.tvew_cancel);
        TextView tvewCamera = view.findViewById(R.id.tvew_camera);
        TextView tvewLocal = view.findViewById(R.id.tvew_local_photo);
        tvewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        tvewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                openCamera(mContext, mParams);
            }
        });

        tvewLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                openGlidePhotoSelectorForPermissions(mContext, mParams);
            }
        });
    }

    public PhotoSelector maxSelectable(int maxSelectable) {
        mParams.maxSelectable = maxSelectable;
        return this;
    }

    public PhotoSelector saveDir(String saveDir) {
        mParams.saveDir = saveDir;
        return this;
    }

    public PhotoSelector photoType(SelectionSpec.PhotoType photoType) {
        mParams.photoType = photoType;
        return this;
    }

    public PhotoSelector mulitPhoto(boolean mulitPhoto) {
        mParams.mulitPhoto = mulitPhoto;
        return this;
    }

    public PhotoSelector selectType(SelectType selectType) {
        mParams.selectType = selectType;
        return this;
    }

    public PhotoSelector show() {
        if (mParams.selectType == SelectType.All){
            mPopupWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.BOTTOM | Gravity
                    .CENTER_HORIZONTAL, 0, 0);
        }else if (mParams.selectType == SelectType.CameraOnly){
            mPopupWindow.dismiss();
            openCamera(mContext, mParams);
        }else if (mParams.selectType == SelectType.AlbumOnly){
            mPopupWindow.dismiss();
            openGlidePhotoSelectorForPermissions(mContext, mParams);
        }
        return this;
    }

    private void openCamera(final Activity context, PhotoParams params) {
        RxPermissions rxPermissions = new RxPermissions(context);
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest
                .permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    openJCamera(context, params);
                } else {
                    Toast.makeText(context, R.string.permission_request_denied,
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

    private void openGlidePhotoSelectorForPermissions(final Activity context, PhotoParams params) {
        RxPermissions rxPermissions = new RxPermissions(context);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            openGlidePhotoSelector(context, params);
                        } else {
                            Toast.makeText(context, R.string.permission_request_denied,
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void openGlidePhotoSelector(Activity context, PhotoParams params) {

        String url = "";
        UploadParams uploadParams
                = new UploadParams()
                .url(url)
                .fileKey("imgStr")
                .type(UploadParams.UploadType.Form)
                .quality(1);
        uploadParams.addParams("billId", URLEncoder.encode("hIQe4YL/Qyig2NreylipPVpeyA0="));

        Matisse.from(context)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .capture(false)
                .upload(false)
                .uploadParams(uploadParams)
                .showSelectOriginal(true)
                .captureStrategy(
                        new CaptureStrategy(true, "com.itopview.bundle.photo.fileprovider"))
                .maxSelectable(params.maxSelectable)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                        context.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void openJCamera(Activity context, PhotoParams params) {

//        String url = "";
//        UploadParams uploadParams
//                = new UploadParams()
//                .url(url)
//                .fileKey("imgStr")
//                .type(UploadParams.UploadType.Form)
//                .quality(1);
//        uploadParams.addParams("billId", "4345");

        JCamera.from(context)
                .choose(JCamera.BUTTON_STATE_ONLY_CAPTURE)
                .upload(false)
//                .uploadParams(uploadParams)
                .switchCamera(false)
                .switchFlash(true)
                .mulitPhoto(params.mulitPhoto)
                .photoType(params.photoType)
                .maxSelectable(9)
                .saveDir(params.saveDir)
                .showSelectOriginal(true)
                .forResult(REQUEST_CODE_CAMERA);
    }

    public static class PhotoParams {
        public SelectionSpec.PhotoType photoType = SelectionSpec.PhotoType.Nornaml;
        public boolean mulitPhoto = false;
        public String saveDir = "JCamera/Final";
        public int maxSelectable = 9;
        public SelectType selectType = SelectType.All;
    }

    public enum SelectType {
        All,
        CameraOnly,
        AlbumOnly,
    }
}
