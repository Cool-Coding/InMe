package cn.yang.inme.utils;

import android.app.ProgressDialog;
import android.content.Context;
import cn.yang.inme.R;
import cn.yang.inme.utils.network.AMapUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.*;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.*;

/**
 * Created by Administrator on 14-7-14.
 */
public class MapUtils implements GeocodeSearch.OnGeocodeSearchListener {
    private Context context;
    private ProgressDialog progDialog = null;
    private GeocodeSearch geocoderSearch;
    private AMap aMap;
    private Marker geoMarker;
    private Marker regeoMarker;
    private LatLonPoint latLonPoint = null;
    private OnGeocodeSearchListener listener;

    public MapUtils(Context context, AMap aMap) {
        this.context = context;
        this.aMap = aMap;
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        aMap.clear();
        if (geoMarker == null)
            geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        if (regeoMarker == null) {
            regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        if (geocoderSearch == null) {
            geocoderSearch = new GeocodeSearch(context);
            geocoderSearch.setOnGeocodeSearchListener(this);
        }
        if (progDialog == null)
            progDialog = InMeProgressDialog.instance(context);
    }

    /**
     * 显示进度条对话框
     */
    public void showDialog() {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在获取地址");
        progDialog.show();
    }

    /**
     * 隐藏进度条对话框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    /**
     * 响应地理编码
     */
    public void getLatlon(final String name) {
        init();
        showDialog();
        GeocodeQuery query = new GeocodeQuery(name, "010");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        init();
        showDialog();
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
        this.latLonPoint = latLonPoint;
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        dismissDialog();
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(latLonPoint), 15));
                regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
                if (listener != null) {
                    listener.onRegeocodeSearched(aMap, regeoMarker);
                }
            } else {
                ToastUtil.show(context, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.show(context, R.string.error_network);
        } else {
            ToastUtil.show(context,
                    context.getString(R.string.error_other) + rCode);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        dismissDialog();
        if (rCode == 0) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
                geoMarker.setPosition(AMapUtil.convertToLatLng(address
                        .getLatLonPoint()));
                if (listener != null) {
                    listener.onGeocodeSearched(aMap, geoMarker);
                }
            } else {
                ToastUtil.show(context, R.string.no_result);
            }
        } else if (rCode == 27) {
            ToastUtil.show(context, R.string.error_network);
        } else {
            ToastUtil.show(context,
                    context.getString(R.string.error_other) + rCode);
        }
    }

    public void drawPolyLine(final LatLng... latLngs) {
        PolylineOptions polylineOptions = new PolylineOptions();
        LatLngBounds.Builder latLngBounds = LatLngBounds.builder();
        for (LatLng latLng : latLngs) {
            latLngBounds.include(latLng);
            polylineOptions.add(latLng);
        }
        aMap.clear();
        polylineOptions.color(Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR))).width(5);
        aMap.addPolyline(polylineOptions);
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 0));
        aMap.moveCamera(CameraUpdateFactory.zoomBy(-2));

        aMap.invalidate();
    }

    //设置回调函数
    public void setOnGeocodeSearchListener(OnGeocodeSearchListener listener) {
        this.listener = listener;
    }

    public interface OnGeocodeSearchListener {
        //解码回调函数
        public void onRegeocodeSearched(AMap amap, Marker regeoMarker);

        //编码回调函数
        public void onGeocodeSearched(AMap amap, Marker geoMarker);
    }

}
