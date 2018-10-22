package cn.yang.inme.utils.network;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

/**
 * AMapV1地图中简单介绍显示定位小蓝点
 */
public class LocationSource implements AMapLocationListener, Runnable {

    private LocationManagerProxy mAMapLocationManager;
    private AMapLocation aMapLocation;// 用于判断定位超时
    private Handler handler = new Handler();
    private Context context;
    private char result = ' ';
    private Double latitude;
    private Double longitude;
    private String city;
    private String address;
    private float accuracy;//定位精度

    private static LocationSource locationSource = null;

    //创建LocationSource对象
    public LocationSource(Context context) {
        this.context = context;
    }

    //设置LocationSource
    public void setLocationSource(LocationSource locationSource) {
        this.locationSource = locationSource;
    }

    //得到LocationSource对象
    public static LocationSource getLocationSource() {
        return locationSource;
    }

    /**
     * 此方法已经废弃
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        if (aLocation != null) {
            this.aMapLocation = aLocation;// 判断超时机制
            latitude = aLocation.getLatitude();
            longitude = aLocation.getLongitude();
            city = aLocation.getCity();
            city = city.substring(0, city.length() - 1);
            accuracy = aLocation.getAccuracy();
            Bundle locBundle = aLocation.getExtras();
            if (locBundle != null) {
                address = locBundle.getString("desc");
            }
            //将标识放在最后，这样确保上面取到数据
            result = 'X';
        }
    }

    public String getAddress() {
        return address;
    }

    /**
     * 定位
     */
    public void locate() {
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(context);
            /*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
            mAMapLocationManager.requestLocationUpdates(
                    LocationProviderProxy.AMapNetwork, 2000, 0, this);
            handler.postDelayed(this, 12000);// 设置超过12秒还没有定位到就停止定位

            //将当前对象加入静态Location
            setLocationSource(this);
        }
    }

    /**
     * 停止定位
     */
    public void deactivate() {
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;
    }

    @Override
    public void run() {
        if (aMapLocation == null) {
            result = 'A';
//            ToastUtil.show(context, "12秒内还没有定位成功，停止定位");
            deactivate();// 销毁掉定位
        }
    }

    //得到结果
    public char getResult() {
        return result;
    }

    //得到经度
    public String getLatitude() {
        return latitude == null ? null : latitude.toString();
    }

    //得到纬度
    public String getLongitude() {
        return longitude == null ? null : longitude.toString();
    }

    public String getCity() {
        return city;
    }

    //得到定位精度
    public float getAccuracy() {
        return accuracy;
    }
}
