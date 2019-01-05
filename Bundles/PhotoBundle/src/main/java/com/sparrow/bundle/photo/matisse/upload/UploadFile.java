package com.sparrow.bundle.photo.matisse.upload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static com.sparrow.bundle.photo.matisse.upload.UploadService.UPLOAD_TAG;

/**
 * Created by WEI on 2017/7/25.
 */

public class UploadFile {

    private static final String TAG = UploadFile.class.getSimpleName();

    private static final MediaType MEDIA_OBJECT_STREAM = MediaType.parse
            ("application/octet-stream");

    private OkHttpClient mOkHttpClient;
    private Handler okHttpHandler;
    private Context mContext;
    private Call mCall;

    public UploadFile(Context context) {
        mContext = context;
        okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        mOkHttpClient = httpBuilder.build();
        okHttpHandler = new Handler();
    }

    /**
     * 上传图片文件
     *
     * @param requestUrl 接口地址
     * @param paramsMap  参数
     * @param <T>
     */
    public <T> Response upLoadPictureByForm(String requestUrl, HashMap<String, Object> paramsMap,
                                            boolean original, int quality) {
        try {
            //补全请求地址
            final FormBody.Builder builder = new FormBody.Builder();
            //追加参数
            for (final String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (object instanceof File) {
                    File file = (File) object;
                    try {
                        long startTime = System.currentTimeMillis();
                        String code = "";
                        if (original) {
                            code = UploadUtils.bitmapToBase64(BitmapFactory.decodeFile(file
                                    .getPath()));
                        } else {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(file.getPath(), options);
                            Log.d(UPLOAD_TAG, "原始图片尺寸" + options.outWidth + " x " + options
                                    .outHeight);
                            int height = options.outHeight * quality;
                            int width = options.outWidth * quality;
                            Log.d(UPLOAD_TAG, "压缩的图片尺寸" + width + " x " + height);
//                            Bitmap bitmap =
//                                    Glide.with(mContext).load(file.getPath()).
//                                            .centerCrop().into(width, height).get();
//                            code = UploadUtils.bitmapToBase64(bitmap);
                        }
                        long time = System.currentTimeMillis() - startTime;
                        Log.d(UPLOAD_TAG, "图片转换处理时间 ：" + time + " ms");
                        builder.add(key, code);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    builder.add(key, object.toString());
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(requestUrl).post(body).build();
            //单独设置参数 比如读取超时时间
//            final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build
//                    ().newCall(request);
            mCall = mOkHttpClient.newCall(request);
            Response response = mCall.execute();
            return response;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public void cancel() {
        if (null != mCall) {
            mCall.cancel();
        }
    }

    /**
     * 上传文件
     *
     * @param requestUrl 接口地址
     * @param paramsMap  参数
     * @param callBack   回调
     * @param <T>
     */
    public <T> void upLoadFileMultipart(final Item item, String requestUrl, HashMap<String,
            Object> paramsMap,
                                        final ReqCallBack callBack) {
        try {
            //补全请求地址
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(requestUrl).post(body).build();
            //单独设置参数 比如读取超时时间
            final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build
                    ().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.toString());
                    callBack.onFailed("上传失败", item);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.e(TAG, "response ----->" + string);
                        callBack.onSuccess(string, item);
                    } else {
                        callBack.onFailed("上传失败", item);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 上传文件
     *
     * @param requestUrl 接口地址
     * @param paramsMap  参数
     * @param callBack   回调
     * @param <T>
     */
    public <T> void upLoadFileMultipart(final Item item, String requestUrl, HashMap<String,
            Object> paramsMap,
                                        final ReqProgressCallBack<T> callBack) {
        try {
            //补全请求地址
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), createProgressRequestBody
                            (MEDIA_OBJECT_STREAM, file, callBack));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(requestUrl).post(body).build();
            final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build
                    ().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.toString());
                    callBack.onFailed("上传失败", item);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.e(TAG, "response ----->" + string);
                        callBack.onSuccess(string, item);
                    } else {
                        callBack.onFailed("上传失败", item);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 创建带进度的RequestBody
     *
     * @param contentType MediaType
     * @param file        准备上传的文件
     * @param callBack    回调
     * @param <T>
     * @return
     */
    public <T> RequestBody createProgressRequestBody(final MediaType contentType, final File
            file, final ReqProgressCallBack<T> callBack) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) {
                Source source;
                try {
                    source = Okio.source(file);
                    Buffer buf = new Buffer();
                    long remaining = contentLength();
                    long current = 0;
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        current += readCount;
                        Log.e(TAG, "current------>" + current);
                        progressCallBack(remaining, current, callBack);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public interface ReqProgressCallBack<T> extends ReqCallBack {
        /**
         * 响应进度更新
         */
        void onProgress(long total, long current);
    }

    public interface ReqCallBack {
        void onSuccess(String response, Item item);

        void onFailed(String response, Item item);
    }

    /**
     * 统一处理进度信息
     *
     * @param total    总计大小
     * @param current  当前进度
     * @param callBack
     * @param <T>
     */
    private <T> void progressCallBack(final long total, final long current, final
    ReqProgressCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onProgress(total, current);
                }
            }
        });
    }

}
