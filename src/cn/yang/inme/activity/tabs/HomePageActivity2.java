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
import cn.yang.inme.indicator.AsyncAdapter;
import cn.yang.inme.indicator.TitleFlowIndicator;
import cn.yang.inme.indicator.ViewFlow;
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
public class HomePageActivity2 extends Activity {

    private ViewFlow mViewFlow;
    private AsyncAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage2);

        LinearLayout layout=(LinearLayout)findViewById(R.id.homepage2);
        ApplicationUtil.setThemeBackground(layout);

        mViewFlow = (ViewFlow) findViewById(R.id.viewflow);
        adapter = new AsyncAdapter(this);
        mViewFlow.setAdapter(adapter, adapter.getTodayId());
        TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
        indicator.setTitleProvider(adapter);
        mViewFlow.setFlowIndicator(indicator);

        Constants.adapter=adapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 20) {
            String str = data.getStringExtra("memo");
            if (str != null) {
               adapter.refresh(0);
            }
        } else if (resultCode == 400) {
            adapter.refresh(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Constants.mActionMode!=null){
                Constants.mActionMode.finish();
                Constants.mActionMode=null;
                return true;
            }
            boolean result = ApplicationUtil.isShouldExit(this);
            if (result) onBackPressed();
            else return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public AsyncAdapter getAdapter(){
        return adapter;
    }
}