package cn.yang.inme.activity.tabs;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.bean.UserLocation;
import cn.yang.inme.sqlite.LocationDBManager;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.border.BorderLinearLayout;
import cn.yang.inme.view.HomePageTodayMassExpandableListAdapter;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

import java.util.List;

/**
 * Created by yang on 2014/6/1.
 */
public class HomePageActivity extends Activity implements SpinnerUtil.OnSpinnerChanged {

    private HomePageTodayMassExpandableListAdapter adapter;
    private ExpandableListView listView;
    private Bundle saveInstanceState;
    private MapView mapView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        LinearLayout homepage = (LinearLayout) findViewById(R.id.homepage2);
        ApplicationUtil.setThemeBackground(homepage);

        //注册短信变化
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true,
                new SmsObserver(smsHandler));
        //注册通话变化
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true,
                new CallObserver(smsHandler));


        listView = (ExpandableListView) findViewById(R.id.today_mass);
        listView.setGroupIndicator(null);
//        listView.setChildDivider(getResources().getDrawable(R.drawable.transparent));
        listView.setDividerHeight(0);
        adapter = new HomePageTodayMassExpandableListAdapter(this);
        listView.setAdapter(adapter);
        //展开待办
        listView.expandGroup(0);

        //今日路径
        TextView today_path = (TextView) findViewById(R.id.today_path);
        today_path.setTextSize(getResources().getDimension(R.dimen.group_size));
        today_path.setPadding(10, 20, 0, 20);

        int color = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        today_path.setTextColor(color);
        today_path.setAlpha(0.6f);

        BorderLinearLayout parent = (BorderLinearLayout) today_path.getParent();
        parent.setBottomBorder(Color.LTGRAY, 3);
        parent.setBackgroundColor(Color.WHITE);
        parent.invalidate();

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.MATCH_PARENT);

        SpinnerUtil spinnerUtil = new SpinnerUtil(Constants.PATH_LOCATION_FREQUENCY, this);
        Spinner spinner = spinnerUtil.getSpinner();
        spinnerUtil.setOnSpinnerChanged(this);

        params2.rightMargin = 10;
        spinner.setLayoutParams(params2);
        parent.addView(spinner);

        mapView = (MapView) findViewById(R.id.today_path_map);
        mapView.onCreate(savedInstanceState);

        today_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //偶数次点击今日路径按钮时，隐藏地图
                if (mapView.isShown()) {
                    mapView.setVisibility(View.GONE);
                } else {
                    getPathLocation();
                    mapView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public Bundle getSaveInstanceState() {
        return saveInstanceState;
    }

    public ExpandableListView getListView() {
        return listView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 20) {
            String str = data.getStringExtra("memo");
            if (str != null) {
                adapter.addNewMemo(str);
            }
        } else if (resultCode == 400) {
            String str = data.getStringExtra("memo");
            adapter.deleteMemo(str);
        }
    }

    @Override
    public void change(String frequency) {
        getPathLocation();
        if (!mapView.isShown()) {
            mapView.setVisibility(View.VISIBLE);
        }

    }

    public void getPathLocation() {
        LocationDBManager db = new LocationDBManager(HomePageActivity.this);
        List<UserLocation> userLocations = db.queryByIds(1);
        db.closeDB();

        mapView.getMap().clear();

        int len = userLocations.size();
        LatLng[] latLngs = new LatLng[len];
        for (int i = 0; i < len; i++) {
            latLngs[i] = new LatLng(Double.valueOf(userLocations.get(i).latitude), Double.valueOf(userLocations.get(i).longitude));
        }
        if (latLngs.length > 0) {
            MapUtils mapUtils = new MapUtils(HomePageActivity.this, mapView.getMap());
            if (latLngs.length == 1) {
                LatLonPoint latLonPoint = new LatLonPoint(latLngs[0].latitude, latLngs[0].longitude);
                mapUtils.getAddress(latLonPoint);
            } else
                mapUtils.drawPolyLine(latLngs);
        } else {
            ToastUtil.showShort(HomePageActivity.this, getResources().getString(R.string.path_location_nodata));
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
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

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (adapter.ifGoBack()) return true;

            boolean result = ApplicationUtil.isShouldExit(this);
            if (result) onBackPressed();
            else return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public Handler smsHandler = new Handler() {
        //这里可以进行回调的操作
        @Override
        public void handleMessage(Message msg) {
            adapter.setMessage(msg);
        }
    };


    class SmsObserver extends ContentObserver {
        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            //开启一新线程，获取短信
            new Thread() {
                @Override
                public void run() {
                    //每当有新短信到来时，使用我们获取短消息的方法
                    PhoneUtils phoneUtils = new PhoneUtils(HomePageActivity.this);
                    phoneUtils.getSmsInPhone();
                    String sms = phoneUtils.getSmsjson();
                    if (sms != null && !"".equals(sms)) {
                        Message message = smsHandler.obtainMessage(1, phoneUtils);
                        smsHandler.sendMessage(message);
                    }
                }
            }.start();
        }
    }

    class CallObserver extends ContentObserver {
        public CallObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            //开启一新线程，获取短信
            new Thread() {
                @Override
                public void run() {
                    //每当有新短信到来时，使用我们获取短消息的方法
                    PhoneUtils phoneUtils = new PhoneUtils(HomePageActivity.this);
                    phoneUtils.getCallogInPhone();
                    String call = phoneUtils.getCalllogjson();
                    if (call != null && !"".equals(call)) {
                        Message message = smsHandler.obtainMessage(2, phoneUtils);
                        smsHandler.sendMessage(message);
                    }
                }
            }.start();
        }
    }
}