package com.sparrow.bundle.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.utils.GPSUtil;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MapPopwin extends PopupWindow implements View.OnClickListener {

    public View pickerContainerV;
    public View contentView;//root view
    public Button cancelBtn;
    public Button baiduBtn;
    public Button gaodeBtn;
    public Button tencentBtn;
    private Context mContext;

    private Point mPoint;

    private String mAddress;

    public MapPopwin(Context context) {
        super(context);
        mContext = context;

        initView();
    }

    private void initView() {

        contentView = LayoutInflater.from(mContext).inflate(com.sparrow.bundle.framework.R.layout.layout_map_pop, null);
        pickerContainerV = contentView.findViewById(com.sparrow.bundle.framework.R.id.container_picker);
        cancelBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_cancel);
        baiduBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_baidu);
        gaodeBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_gaode);
        tencentBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_tencent);

        contentView.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        baiduBtn.setOnClickListener(this);
        gaodeBtn.setOnClickListener(this);
        tencentBtn.setOnClickListener(this);

        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(com.sparrow.bundle.framework.R.style.FadeInPopWin);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * Show date picker popWindow
     *
     * @param activity
     */
    public void showPopWin(Activity activity, String address, Point point) {
        if (null != activity) {
            mPoint = point;
            this.mAddress = address;
            showPopWin(activity);
        }
    }

    public void showPopWin(Activity activity, String address) {
        if (null != activity) {
            this.mAddress = address;
            showPopWin(activity);
        }
    }

    private void showPopWin(Activity activity) {
        if (null != activity) {
            TranslateAnimation trans = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                    0, Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0);

            showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,
                    0, 0);
            trans.setDuration(400);
            trans.setInterpolator(new AccelerateDecelerateInterpolator());

            pickerContainerV.startAnimation(trans);
        }
    }

    /**
     * Dismiss date picker popWindow
     */
    public void dismissPopWin() {

        TranslateAnimation trans = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);

        trans.setDuration(400);
        trans.setInterpolator(new AccelerateInterpolator());
        trans.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }
        });
        pickerContainerV.startAnimation(trans);
    }

    public static class Point {
        private double lat;
        private double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    private void openBaiduNav() {

        double[]  point = GPSUtil.gcj02_To_Bd09(mPoint.lat, mPoint.lng);

        if (isAvilible(mContext, "com.baidu.BaiduMap")) {//传入指定应用包名
            try {
                Intent intent = Intent.getIntent("intent://map/direction?" +
                        "destination=latlng:" + point[0] + "," + point[1] + "|name:" + mAddress +   //终点
                        "&mode=driving&" +     //导航路线方式
                        "&src=appname#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                mContext.startActivity(intent); //启动调用
            } catch (URISyntaxException e) {
                Log.e("intent", e.getMessage());
            }
        } else {//未安装
            //market为路径，id为包名
            //显示手机上所有的market商店
            Toast.makeText(mContext, "您尚未安装百度地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
            }
        }
        dismissPopWin();
    }

    private void openBaiduMarker() {
        if (isAvilible(mContext, "com.baidu.BaiduMap")) {//传入指定应用包名
            Intent intent = new Intent();
            intent.setData(Uri.parse("baidumap://map/marker?location=" + mPoint.getLat() + "," + mPoint.getLng() +
                    "&title=" + mAddress + "&content=makeamarker&traffic=on&src=食品安全"));
            mContext.startActivity(intent); //启动调用
        } else {//未安装
            //market为路径，id为包名
            //显示手机上所有的market商店
            Toast.makeText(mContext, "您尚未安装百度地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
            }
        }
        dismissPopWin();
    }


    private void openGaodeNav() {

        if (isAvilible(mContext, "com.autonavi.minimap")) {
            StringBuilder builder = new StringBuilder("amapuri://route/plan?sourceApplication=食品安全");
            builder.append("&dlat=").append(mPoint.getLat())
                    .append("&dlon=").append(mPoint.getLng())
                    .append("&dname=").append(mAddress)
                    .append("&dev=0")
                    .append("&t=0");
            String uriString = builder.toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.autonavi.minimap");
            intent.setData(Uri.parse(uriString));
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, "您尚未安装高德地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
            }
        }
    }

    private void openGaodeMarker() {
        if (isAvilible(mContext, "com.autonavi.minimap")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            //uri.amap.com/marker?position=121.287689,31.234527&name=park&src=mypage&coordinate=gaode&callnative=0

            Intent i = new Intent();
            i.setAction("android.intent.action.VIEW");
            i.addCategory("android.intent.category.DEFAULT");
            i.setPackage("com.autonavi.minimap");
            i.setData(Uri
                    .parse("androidamap://viewMap?sourceApplication=食品安全&poiname=" + mAddress
                            + "&lat=" + mPoint.getLat() + "&lon=" + mPoint.getLng() + "&dev=0")); // softname，开发程序的名称

            //将功能Scheme以URI的方式传入data
//            Uri uri = Uri.parse("androidamap://marker?sourceApplication=appname&position="
//                    + mPoint.getLat() + "," + mPoint.getLng()
//                    + "name=" + address + "&coordinate=gaode&callnative=0");
//            intent.setData(uri);

            //启动该页面即可
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, "您尚未安装高德地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
            }
        }
    }

    private void openTencentNav() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        //将功能Scheme以URI的方式传入data
        Uri uri = Uri.parse("qqmap://map/routeplan?type=drive&to=" + mAddress + "&tocoord=" + mPoint.getLat() + "," + mPoint.getLng());
        intent.setData(uri);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            //启动该页面即可
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, "您尚未安装腾讯地图", Toast.LENGTH_LONG).show();
        }
    }


    private boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//获取所有已安装程序的包信息
        List<String> pName = new ArrayList<String>();//用于存储所有已安装程序的包名
        //从pinfo中将包名字逐一取出，压入pName list中
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }


    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param lat
     * @param lon
     */
    public double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
//        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double z = Math.sqrt(x * x + y * y) + 0.00002;
//        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double theta = Math.atan2(y, x) + 0.000003;
        double tempLat = z * Math.sin(theta) + 0.006;
        double tempLon = z * Math.cos(theta) + 0.0065;
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    @Override
    public void onClick(View v) {
        if (v == contentView || v == cancelBtn) {
            dismissPopWin();
        } else if (v == baiduBtn) {
            openBaiduNav();
        } else if (v == gaodeBtn) {
            openGaodeNav();
        } else if (v == tencentBtn) {
            openTencentNav();
        }
        dismissPopWin();
    }
}
