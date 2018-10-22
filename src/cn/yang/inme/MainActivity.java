package cn.yang.inme;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import cn.yang.inme.activity.around.AroundShop;
import cn.yang.inme.activity.around.AroundTuangou;
import cn.yang.inme.activity.homepage.MemoActivity;
import cn.yang.inme.activity.tabs.HomePageActivity;
import cn.yang.inme.activity.tabs.HomePageActivity2;
import cn.yang.inme.activity.tabs.MeActivity;
import cn.yang.inme.indicator.AsyncAdapter;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.alarm.AlarmUtil;
import cn.yang.inme.utils.network.LocationSource;
import cn.yang.inme.view.AroundShopScrollView;
import cn.yang.inme.view.AroundTuangouScrollView;

public class MainActivity extends TabActivity implements
        CompoundButton.OnCheckedChangeListener {

    private TabHost tabHost;
    private Intent homepageIntent;
    private Intent aroundIntent;
    private Intent findIntent;
    private Intent meIntent;

    private RadioButton homepageBt;
    private RadioButton aroundBt;
    private RadioButton findBt;
    private RadioButton meBt;

    /**
     * 判断是否在今日待办页
     */
    private boolean is_in_memo=true;
    /**
     * 搜索按钮
     */
    private SearchView searchView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowTitleEnabled(false);
/*        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       // No Titlebar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);*/
//初始化
        setContentView(R.layout.main);
        setActionBarColor();
        setFooterColor();
        /**
         * 计时开始
         */
        ApplicationUtil.setIntervalTime(System.currentTimeMillis());
        /**
         * 加载背景
         */
        ApplicationUtil.loadBackground();
//创建Intent
        homepageIntent = new Intent(this, HomePageActivity2.class);
        aroundIntent = new Intent(this, AroundTuangou.class);
        findIntent = new Intent(this, AroundShop.class);
        meIntent = new Intent(this, MeActivity.class);
//RadioButton
        homepageBt = (RadioButton) findViewById(R.id.homepage);
        aroundBt = (RadioButton) findViewById(R.id.around);
        findBt = (RadioButton) findViewById(R.id.find);
        meBt = (RadioButton) findViewById(R.id.me);
        tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("homepage").setIndicator("homepage")
                .setContent(homepageIntent));
        tabHost.addTab(tabHost.newTabSpec("around").setIndicator("around")
                .setContent(aroundIntent));
        tabHost.addTab(tabHost.newTabSpec("find").setIndicator("find")
                .setContent(findIntent));
        tabHost.addTab(tabHost.newTabSpec("me").setIndicator("me")
                .setContent(meIntent));

        homepageBt.setOnCheckedChangeListener(this);
        homepageBt.setChecked(true);
        aroundBt.setOnCheckedChangeListener(this);
        findBt.setOnCheckedChangeListener(this);
        meBt.setOnCheckedChangeListener(this);


        //启动定位
        LocationSource locate = new LocationSource(this);
        locate.locate();
        if ("true".equals(PropertiesUtil.instance().read(Constants.PATH_LOCATION_ENABLE))) {
            AlarmUtil.startLocateUser(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //保存抬头和选项栏的高度
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.footer);
        Constants.footerHeight = radioGroup.getHeight();
        Constants.headerHeight = getActionBar().getHeight();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.homepage:
                    tabHost.setCurrentTabByTag("homepage");
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_talk_l, 0, 0);
                    break;
                case R.id.around:
                    tabHost.setCurrentTabByTag("around");
                    //设置背景图片
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_address_l, 0, 0);
                    break;
                case R.id.find:
                    tabHost.setCurrentTabByTag("find");
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_find_l, 0, 0);
                    break;
                case R.id.me:
                    tabHost.setCurrentTabByTag("me");
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_me_l, 0, 0);
                    break;
                default:
                    break;
            }
            this.invalidateOptionsMenu();
        } else {
            switch (buttonView.getId()) {
                case R.id.homepage:
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_talk_h, 0, 0);
                    break;
                case R.id.around:
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_address_h, 0, 0);
                    break;
                case R.id.find:
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_find_h, 0, 0);
                    break;
                case R.id.me:
                    buttonView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.n_me_h, 0, 0);
                    break;
                default:
                    break;
            }
        }
    }

    public void setActionBarColor() {
        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(1);
        getActionBar().setBackgroundDrawable(gradientDrawable);
    }

    public void setFooterColor() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.footer);
        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(3);
        radioGroup.setBackgroundDrawable(gradientDrawable);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("around".equals(tabHost.getCurrentTabTag())) {
            getMenuInflater().inflate(R.menu.aroundtuangou_menu, menu);

            //搜索视窗，因为showAsAction="ifRoom"
            final LinearLayout linearLayout=(LinearLayout)menu.findItem(R.id.menu_search)
                    .getActionView();
            searchView = (SearchView)linearLayout.findViewById(R.id.searchview);

            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
               @Override
               public void onFocusChange(View v, boolean hasFocus) {
                   if (hasFocus){
                       Button searchBt=(Button)linearLayout.findViewById(R.id.searchbt);
                       searchBt.setVisibility(View.VISIBLE);
                   }else{
                       Button searchBt=(Button)linearLayout.findViewById(R.id.searchbt);
                       searchBt.setVisibility(View.GONE);
                   }
               }
           });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    View mScrollView = tabHost.getCurrentView().findViewWithTag("aroundtuangouview");
                    ((AroundTuangouScrollView) mScrollView).search(s);
                    ((AroundTuangouScrollView) mScrollView).addSearchTag(s);
                    searchView.setIconified(true);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }

            });
        }else if("find".equals(tabHost.getCurrentTabTag())){
            getMenuInflater().inflate(R.menu.aroundtuangou_menu, menu);

            //搜索视窗，因为showAsAction="ifRoom"
            final LinearLayout linearLayout=(LinearLayout)menu.findItem(R.id.menu_search)
                    .getActionView();

            searchView = (SearchView)linearLayout.findViewById(R.id.searchview);

            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        Button searchBt=(Button)linearLayout.findViewById(R.id.searchbt);
                        searchBt.setVisibility(View.VISIBLE);
                    }else{
                        Button searchBt=(Button)linearLayout.findViewById(R.id.searchbt);
                        searchBt.setVisibility(View.GONE);
                    }
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    View mScrollView = tabHost.getCurrentView().findViewWithTag("aroundshopview");
                    ((AroundShopScrollView) mScrollView).search(s);
                    ((AroundShopScrollView) mScrollView).addSearchTag(s);
                    searchView.setIconified(true);
                    searchView.clearFocus();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }

            });
        }else if("homepage".equals(tabHost.getCurrentTabTag()) && is_in_memo){
            getMenuInflater().inflate(R.menu.showmemo_menu, menu);
        }
        return true;
    }

    public void setIs_in_memo(boolean in){
        is_in_memo=in;
    }

    /**
     * 搜索
     * @param v
     */
    public void start_to_search(View v){
        String s=null;

        AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null));

        Editable editText=search_text.getText();
        if (editText!=null){
            s=search_text.getText().toString();
        }
        if (s==null||"".equals(s))return;

        if ("around".equals(tabHost.getCurrentTabTag())) {
            View mScrollView = tabHost.getCurrentView().findViewWithTag("aroundtuangouview");
            ((AroundTuangouScrollView) mScrollView).search(s);
            ((AroundTuangouScrollView) mScrollView).addSearchTag(s);
            searchView.setIconified(true);
            searchView.clearFocus();
        }else if("find".equals(tabHost.getCurrentTabTag())){
            View mScrollView = tabHost.getCurrentView().findViewWithTag("aroundshopview");
            ((AroundShopScrollView) mScrollView).search(s);
            ((AroundShopScrollView) mScrollView).addSearchTag(s);
            searchView.setIconified(true);
            searchView.clearFocus();
        }
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
            case R.id.menu_creatememo:
                Intent intent = new Intent(this, MemoActivity.class);
                this.startActivityForResult(intent, 10);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            AsyncAdapter asyncAdapter = Constants.adapter;
            if (asyncAdapter != null) {
               asyncAdapter.refresh(0);//刷新待办
            }
        }
    }
}

