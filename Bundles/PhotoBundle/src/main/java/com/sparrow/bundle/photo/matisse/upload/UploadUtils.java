package com.sparrow.bundle.photo.matisse.upload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Created by WEI on 2017/7/27.
 */

public class UploadUtils {
    public static String videoToBase64(String path) {
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

    public static void thumbnailToBase64(Context context, String path, CallBack callBack) {
        try {
            Glide.with(context).load(path).into(new SimpleTarget<Drawable>(100, 100) {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    if (null != callBack) {
                        callBack.onResource(bitmapToBase64(((BitmapDrawable) resource).getBitmap()));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CallBack {
        void onResource(String base64);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                Log.i(UploadService.UPLOAD_TAG, "图片原始大小 === " + bitmapBytes.length);
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
                Log.i(UploadService.UPLOAD_TAG, "图片转Base64大小 === " + result.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
