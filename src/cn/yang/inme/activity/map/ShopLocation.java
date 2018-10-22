package cn.yang.inme.activity.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShopDetail;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.utils.MapUtils;
import cn.yang.inme.utils.network.AMapUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;

import java.io.Serializable;

/**
 * Created by Administrator on 14-6-13.
 */
public class ShopLocation extends Activity {

    private MapView mapView;
    private LatLonPoint latLonPoint = null;
    private String shopname;
    private MapUtils mapUtils;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showmap);
        getActionBar().setDisplayShowTitleEnabled(false);
        //设置ActionBar颜色
        ApplicationUtil.setActionBarColor(this);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        mapUtils = new MapUtils(this, mapView.getMap());

        //取传过来的经纬度
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        latLonPoint = bundle.getParcelable(getResources().getString(R.string.search_location_tag));
        if (latLonPoint != null) {
            mapUtils.getAddress(latLonPoint);
        }
        shopname=bundle.getString("name");
    }

    public void go_locate_line(View v){
        String url="geo:"+latLonPoint.getLatitude()+","+latLonPoint.getLongitude();
        if (shopname!=null)url=url+"?q="+shopname;

        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(it);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}