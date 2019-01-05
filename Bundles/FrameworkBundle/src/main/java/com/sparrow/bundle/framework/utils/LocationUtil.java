package com.sparrow.bundle.framework.utils;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationUtil {

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private static LocationUtil locationUtil;

    private OnLocationListener listener;

    public static LocationUtil getInstance() {
        if (null == locationUtil) {
            locationUtil = new LocationUtil();
        }
        return locationUtil;
    }

    private LocationUtil() {

        mLocationClient = new LocationClient(Utils.getContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，设置定位模式，默认高精度
//LocationMode.Hight_Accuracy：高精度；
//LocationMode. Battery_Saving：低功耗；
//LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("BD09ll");
//可选，设置返回经纬度坐标类型，默认GCJ02
//GCJ02：国测局坐标；
//BD09ll：百度经纬度坐标；
//BD09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

//       option.setScanSpan(1000);
//可选，设置发起定位请求的间隔，int类型，单位ms
//如果设置为0，则代表单次定位，即仅定位一次，默认为0
//如果设置非0，需设置1000ms以上才有效
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
//可选，设置是否使用gps，默认false
//使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
//可选，定位SDK内部是一个service，并放到了独立进程。
//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
//可选，设置是否收集Crash信息，默认收集，即参数为false

//        option.setWifiCacheTimeOut(5 * 60 * 1000);
//可选，V7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
    }

    public void setListener(OnLocationListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (mLocationClient.isStarted()){
            mLocationClient.restart();
        }else {
            mLocationClient.start();
        }
    }

    public void stop() {
        mLocationClient.stop();
    }

    private BDLocation mLocation;

    public BDLocation getLocation() {
        return mLocation;
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocation = location;
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();

            KLog.i("latitude --> " + latitude);
            KLog.i("longitude --> " + longitude);
            KLog.i("message --> " + location.getLocTypeDescription());
            if (null != listener) {
                listener.onReceiveLocation(location);
            }
        }
    }

    public interface OnLocationListener {
        void onReceiveLocation(BDLocation location);
    }
}
