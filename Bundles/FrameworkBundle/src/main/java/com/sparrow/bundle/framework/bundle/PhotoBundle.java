package com.sparrow.bundle.framework.bundle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.medical.bundle.photo.camera.build.JCamera;
import com.medical.bundle.photo.camera.build.SelectionSpec;
import com.medical.bundle.photo.camera.util.FileUtil;
import com.medical.bundle.photo.matisse.Matisse;
import com.medical.bundle.photo.matisse.MimeType;
import com.medical.bundle.photo.matisse.engine.impl.GlideEngine;
import com.medical.bundle.photo.matisse.filter.Filter;
import com.medical.bundle.photo.matisse.filter.GifSizeFilter;
import com.medical.bundle.photo.matisse.internal.entity.CaptureStrategy;
import com.medical.bundle.photo.matisse.internal.entity.Item;
import com.medical.bundle.photo.matisse.internal.model.SelectedItemCollection;
import com.medical.bundle.photo.matisse.ui.MatisseActivity;
import com.medical.bundle.photo.matisse.upload.UploadParams;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.bither.util.NativeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * Created by WEI on 2017/7/28.
 */

public class PhotoBundle {

    public static final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    public static final int REQUEST_CODE_CHOOSE = 23;
    public static final int REQUEST_CODE_CAMERA = 24;
    public static final int REQUEST_CODE_PREVIEW = 25;

    public static final String IMAGE_TEMP_DIR = "Medical/FoodSecurity/.ImageTemp";

    public static final String IMAGE_TEMP_DIR_FULL = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Medical/FoodSecurity/.ImageTemp";

    private PhotoParams mParams = new PhotoParams();
    private Activity mContext;
    private PopupWindow mPopupWindow;

    public PhotoBundle(Activity context) {
        mContext = context;
        init();
    }

    public static List<Item> obtainPathResult(Intent data) {
        return data.getParcelableArrayListExtra(MatisseActivity.EXTRA_RESULT_SELECTION);
    }


    public static boolean isPathResult(int resultCode, int requestCode) {
        return resultCode == RESULT_OK &&
                (requestCode == PhotoBundle.REQUEST_CODE_CHOOSE || requestCode == PhotoBundle
                        .REQUEST_CODE_CAMERA);
    }

    private void init() {
        View view = View.inflate(mContext, com.medical.bundle.photo.R.layout.pop_select_pic, null);
        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x50000000));

        TextView tvewCancel = view.findViewById(com.medical.bundle.photo.R.id.tvew_cancel);
        TextView tvewCamera = view.findViewById(com.medical.bundle.photo.R.id.tvew_camera);
        TextView tvewLocal = view.findViewById(com.medical.bundle.photo.R.id.tvew_local_photo);
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

    public PhotoBundle maxSelectable(int maxSelectable) {
        mParams.maxSelectable = maxSelectable;
        return this;
    }

    public PhotoBundle maxPhotoable(int maxPhotoable) {
        mParams.maxPhotoable = maxPhotoable;
        return this;
    }

    public PhotoBundle saveDir(String saveDir) {
        mParams.saveDir = saveDir;
        return this;
    }

    public PhotoBundle photoType(PhotoType photoType) {
        if (photoType == PhotoType.Nornaml) {
            mParams.photoType = SelectionSpec.PhotoType.Nornaml;
        } else if (photoType == PhotoType.Certificate) {
            mParams.photoType = SelectionSpec.PhotoType.Certificate;
        }
        return this;
    }

    public PhotoBundle mulitPhoto(boolean mulitPhoto) {
        mParams.mulitPhoto = mulitPhoto;
        return this;
    }

    public PhotoBundle selectType(SelectType selectType) {
        mParams.selectType = selectType;
        return this;
    }

    public PhotoBundle switchCamera(boolean switchCamera) {
        mParams.switchCamera = switchCamera;
        return this;
    }

    public PhotoBundle defaultFront(boolean defaultFront) {
        mParams.defaultFront = defaultFront;
        return this;
    }

    public PhotoBundle selectItems(ArrayList<Item> selectItems) {
        mParams.selectItems = selectItems;
        return this;
    }

    public PhotoBundle showSelectOriginal(boolean selectOriginal) {
        mParams.selectOriginal = selectOriginal;
        return this;
    }

    public PhotoBundle show() {
        if (mParams.selectType == SelectType.All) {
            mPopupWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.BOTTOM | Gravity
                    .CENTER_HORIZONTAL, 0, 0);
        } else if (mParams.selectType == SelectType.CameraOnly) {
            mPopupWindow.dismiss();
            openCamera(mContext, mParams);
        } else if (mParams.selectType == SelectType.AlbumOnly) {
            mPopupWindow.dismiss();
            openGlidePhotoSelectorForPermissions(mContext, mParams);
        }
        return this;
    }

    public static void previewTakePhotos(Activity context, List<Item> data, int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SelectedItemCollection.STATE_SELECTION, new ArrayList<>(data));
        bundle.putInt(SelectedItemCollection.STATE_SELECT_POSITION, position);
        Matisse.from(context)
                .choose(MimeType.ofImage(), false)
                .upload(false)
                .showSelectOriginal(false)
                .forResultPreview(REQUEST_CODE_PREVIEW, bundle);
    }

    public static void previewTakeSinglePhoto(Activity context, String path) {
        List<Item> data = new ArrayList<>();
        data.add(new Item(path));
        previewTakePhotos(context, data, 0);
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
                    Toast.makeText(context, "需要相应的权限",
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
                            Toast.makeText(context, com.medical.bundle.photo.R.string.permission_request_denied,
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
                .saveDir(IMAGE_TEMP_DIR)
                .uploadParams(uploadParams)
                .showSelectOriginal(mParams.selectOriginal)
                .captureStrategy(
                        new CaptureStrategy(true, "com.sparrow.bundle.photo.fileprovider"))
                .maxSelectable(params.maxSelectable)
                .maxPhotoable(params.maxPhotoable)
                .selectItems(null == params.selectItems ? new ArrayList<>() : params.selectItems)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                        context.getResources().getDimensionPixelSize(com.medical.bundle.photo.R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void openJCamera(Activity context, PhotoParams params) {

        JCamera.from(context)
                .choose(JCamera.BUTTON_STATE_ONLY_CAPTURE)
                .upload(false)
                .switchFlash(true)
                .switchCamera(params.switchCamera)
                .mulitPhoto(params.mulitPhoto)
                .maxPhotoable(params.maxPhotoable)
                .maxSelectable(params.maxSelectable)
                .photoType(params.photoType)
                .defaultFront(params.defaultFront)
                .saveDir(params.saveDir)
                .showSelectOriginal(mParams.selectOriginal)
                .forResult(REQUEST_CODE_CAMERA);
    }

    public static String compressBitmap(String path) {
        String tempPath = FileUtil.getSavePath(IMAGE_TEMP_DIR);
        NativeUtil.compressBitmap(path, tempPath);
        return tempPath;
    }

    public static String imageToBase64(String path) {
        return fileToBase64(compressBitmap(path));
    }

    public static String fileToBase64(String path) {
        FileInputStream inputStream = null;
        try {
            File file = new File(path);
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputStream.read(buffer);
            return Base64.encodeToString(buffer, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static class PhotoParams {
        public SelectionSpec.PhotoType photoType = SelectionSpec.PhotoType.Nornaml;
        public boolean mulitPhoto = false;
        public String saveDir = IMAGE_TEMP_DIR;
        public ArrayList<Item> selectItems;
        public int maxSelectable = 9;
        public int maxPhotoable = 9;
        public boolean selectOriginal;
        public boolean switchCamera = false;
        public boolean defaultFront = false;
        public SelectType selectType = SelectType.All;
    }

    /**
     * 拍照类型
     */
    public enum PhotoType {
        Nornaml,      // 普通
        Certificate;  // 证件

        public static PhotoType getValue(String type) {
            PhotoType[] types = PhotoType.values();
            for (int i = 0; i < types.length; i++) {
                if (types[i].toString().equals(type)) {
                    return types[i];
                }
            }
            return null;
        }
    }

    public enum SelectType {
        All,
        CameraOnly,
        AlbumOnly;

        public static SelectType getValue(String type) {
            SelectType[] types = SelectType.values();
            for (int i = 0; i < types.length; i++) {
                if (types[i].toString().equals(type)) {
                    return types[i];
                }
            }
            return null;
        }
    }
}
