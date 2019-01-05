package com.sparrow.bundle.framework.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.List;

/**
 * Android N 适配工具类
 */
public class NougatTools {

    /**
     * 将普通uri转化成适应7.0的content://形式  针对文件格式
     *
     * @param context    上下文
     * @param file       文件路径
     * @param intent     intent
     * @param intentType intent.setDataAndType
     * @return
     */
    public static Intent formatFileProviderIntent(
            Context context, File file, Intent intent, String intentType, String authority) {

        Uri uri = FileProvider.getUriForFile(context, authority, file);
        // 表示文件类型
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, intentType);

        return intent;
    }

    //获取value
    public static String getMetaDataFromApp(Context context, String name) {
        String value = "";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = appInfo.metaData.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 将普通uri转化成适应7.0的content://形式  针对图片格式
     *
     * @param context 上下文
     * @param file    文件路径
     * @param intent  intent
     */
    public static Intent formatFileProviderPicIntent(
            Context context, File file, Intent intent, String authority) {

        Uri uri = FileProvider.getUriForFile(context, authority, file);
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                uri);
        return intent;
    }

    /**
     * 将普通uri转化成适应7.0的content://形式
     *
     * @return
     */
    public static Uri formatFileProviderUri(Context context, File file, String authority) {
        return FileProvider.getUriForFile(context, authority, file);
    }


    public static void setIntentDataAndType(Context context,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble, String authority) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file, authority), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }

    public static Uri getUriForFile(Context context, File file, String authority) {
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = formatFileProviderUri(context, file, authority);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

}