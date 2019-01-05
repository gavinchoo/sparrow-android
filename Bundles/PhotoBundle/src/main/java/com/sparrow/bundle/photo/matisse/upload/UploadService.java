package com.sparrow.bundle.photo.matisse.upload;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CustomDialog;
import com.sparrow.bundle.photo.matisse.internal.utils.PhotoMetadataUtils;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import static com.sparrow.bundle.photo.matisse.ui.MatisseActivity.EXTRA_RESULT_SELECTION;

/**
 * Created by WEI on 2017/7/27.
 */

public class UploadService implements UploadFile.ReqCallBack {

    public static final String UPLOAD_TAG = UploadService.class.getSimpleName();

    private Context mContext;
    private UploadParams mUploadParams = null;
    private List<Item> mUploadItems;
    private UploadTask mUploadTask;

    private List<Item> mUploadingItems = new ArrayList<>();

    private List<UploadItem> mUploadSuccessItems = new ArrayList<>();

    private List<Item> mUploadFailedItems = new ArrayList<>();

    private UploadDialog mUploadDialog;

    public UploadService(Context context) {
        this.mContext = context;
        mUploadDialog = new UploadDialog(mContext);
        mUploadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mUploadTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mUploadTask.cancel(true);
                }
            }
        });
        mUploadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    if (null == mCustomDialog || !mCustomDialog.isShowing()){
                        if (null != mCallback){
                            mCallback.uploadCancel();
                        }
                    }
                }
                return false;
            }
        });
    }

    public UploadService uploadParams(UploadParams params) {
        mUploadParams = params;
        return this;
    }

    public UploadService uploadData(List<Item> items) {
        mUploadItems = items;
        mUploadingItems = items;
        return this;
    }

    public UploadService uploadCallBack(UploadCallback callback) {
        mCallback = callback;
        return this;
    }

    private static final int MAX_RETRY = 3;

    private int retry;

    public Bundle getDataWithBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_RESULT_SELECTION, getData());
        return bundle;
    }

    public ArrayList<UploadItem> getData() {
        return new ArrayList<>(mUploadSuccessItems);
    }

    public UploadService start() {
        mUploadTask = new UploadTask(mContext, mUploadParams, this) {
            @Override
            protected void onPostExecute(List<String> strings) {
                super.onPostExecute(strings);
                if (mUploadFailedItems.size() > 0) {
                    mUploadingItems = mUploadFailedItems;
                    retry++;
                    if (retry < MAX_RETRY && !isCancelled()) {
                        Log.i(UPLOAD_TAG, "失败重新上传" + retry + "次");
                        start();
                    } else {
                        dismissDialog();
                        showAlertDialog();
                    }
                } else {
                    // 上传完成
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            if (null != mCallback) {
                                mCallback.uploadSuccess();
                            }
                        }
                    }, 800);
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mUploadDialog.setProgress(values[0]);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mUploadDialog.show();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                dismissDialog();
            }

            @Override
            public int getUploadProgress() {
                int successSize = mUploadSuccessItems.size();
                int totalSize = mUploadItems.size();
                return successSize * 100 / totalSize;
            }
        };
        mUploadTask.execute(mUploadingItems);
        return this;
    }

    public void dismissDialog() {
        if (null != mUploadDialog && mUploadDialog.isShowing()) {
            mUploadDialog.dismiss();
        }
    }

    private UploadCallback mCallback;

    public void setUploadSuccessListener(UploadCallback callback) {
        mCallback = callback;
    }

    public interface UploadCallback {
        void uploadSuccess();
        void uploadCancel();
    }

    private CustomDialog mCustomDialog;

    private void showAlertDialog() {
        CustomDialog.Builder customBuilder = new
                CustomDialog.Builder(mContext);
        customBuilder
                .setTitle("提示")
                .setMessage("网络好像开小差了哦，是否还要上传？")
                .setNegativeButton("再试一下", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUploadingItems = mUploadFailedItems;
                        mCustomDialog.dismiss();
                        start();
                    }
                })
                .setPositiveButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (null != mCallback){
                                    mCallback.uploadCancel();
                                }
                            }
                        });
        mCustomDialog = customBuilder.create();
        mCustomDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    if (null == mUploadDialog || !mUploadDialog.isShowing()){
                        if (null != mCallback){
                            mCallback.uploadCancel();
                        }
                    }
                }
                return false;
            }
        });
        mCustomDialog.show();
    }

    public void destory() {
        if (null != mUploadTask && mUploadTask.getStatus() == AsyncTask.Status.RUNNING) {
            mUploadTask.cancel(true);
            mUploadTask = null;
        }
    }

    @Override
    public void onSuccess(String response, Item item) {
        UploadUtils.thumbnailToBase64(mContext, item.path, new UploadUtils.CallBack() {
            @Override
            public void onResource(String thumbnail) {
                UploadItem uploadItem = new UploadItem(item, response, thumbnail);
                mUploadSuccessItems.add(uploadItem);
                mUploadFailedItems.remove(item);
            }
        });
    }

    @Override
    public void onFailed(String response, Item item) {
        if (!mUploadFailedItems.contains(item)) {
            mUploadFailedItems.add(item);
        }
    }

    public static class UploadItem implements Parcelable {
        public static final Creator<UploadItem> CREATOR = new Creator<UploadItem>() {
            @Override
            @Nullable
            public UploadItem createFromParcel(Parcel source) {
                return new UploadItem(source);
            }

            @Override
            public UploadItem[] newArray(int size) {
                return new UploadItem[size];
            }
        };

        public Item item;
        public String response;
        public String thumbnail;

        public UploadItem(Item item, String response, String thumbnail) {
            this.item = item;
            this.response = response;
            this.thumbnail = thumbnail;
        }

        private UploadItem(Parcel source) {
            item = source.readParcelable(Item.class.getClassLoader());
            response = source.readString();
            thumbnail = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(item, 0);
            dest.writeString(response);
            dest.writeString(thumbnail);
        }
    }
}
