package com.sparrow.bundle.photo.matisse.upload;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.sparrow.bundle.photo.matisse.internal.entity.Item;

import java.io.File;
import java.util.List;

import okhttp3.Response;

/**
 * Created by WEI on 2017/7/27.
 */

public abstract class UploadTask extends AsyncTask<List<Item>, Integer, List<String>> {

    private UploadParams mUploadParams = null;
    private Context mContext;
    private UploadFile mUploadFile;
    private UploadFile.ReqCallBack mUpLoadCallback;

    public UploadTask(Context context, UploadParams params, UploadFile.ReqCallBack reqCallBack) {
        mContext = context;
        mUploadParams = params;
        mUpLoadCallback = reqCallBack;
        mUploadFile = new UploadFile(mContext);
    }

    @Override
    protected List<String> doInBackground(List<Item>... params) {
        List<Item> itemList = params[0];
        for (int i = 0; i < itemList.size(); i++) {
            if (!isCancelled()) {
                Item item = itemList.get(i);
                uploadFile(item);
                publishProgress(getUploadProgress());
            }
        }
        return null;
    }

    private void uploadFile(Item item) {
        mUploadParams.addParams(mUploadParams.fileKey, new File(item.path));
        if (mUploadParams.uploadType == UploadParams.UploadType.Form) {
            Response response = mUploadFile.upLoadPictureByForm(mUploadParams.requestUrl,
                    mUploadParams.params, mUploadParams.original, mUploadParams.quality);
            try {
                if (null != response) {
                    String string = response.body().string();
                    if (response.isSuccessful()) {
                        Log.e(UploadService.UPLOAD_TAG, "response ----->" + string);
                        mUpLoadCallback.onSuccess(string, item);
                    } else {
                        mUpLoadCallback.onFailed(string, item);
                    }
                } else {
                    mUpLoadCallback.onFailed("上传失败", item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            mUploadFile.upLoadFileMultipart(item, mUploadParams.requestUrl, mUploadParams.params,
                    mUpLoadCallback);
        }
    }



    public abstract int getUploadProgress();

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (null != mUploadFile) {
            mUploadFile.cancel();
        }
    }
}
