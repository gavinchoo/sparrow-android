package com.sparrow.bundle.framework.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author GJ 系统屏幕的一些操作
 * @date 2018/8/15
 * @description
 */
public final class DensityUtils {

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
	 */
	public static int px2sp(Context context, float pxValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 sp 的单位 转成为 px
	 */
	public static int sp2px(Context context, float spValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 获取屏幕宽度
	 */
	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int w = dm.widthPixels;
		return w;
	}

	/**
	 * 获取屏幕高度
	 */
	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int h = dm.heightPixels;
		// int h = aty.getWindowManager().getDefaultDisplay().getHeight();
		return h;
	}
}