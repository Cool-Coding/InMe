package cn.yang.inme.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShop;
import cn.yang.inme.asyntask.AsynUpdateAddress;
import cn.yang.inme.asyntask.GetAroundShop;
import cn.yang.inme.utils.Constants;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 14-6-5.
 */
public class AroundShopScrollView extends ScrollView {

    /**
     * 所属Activity
     */
    private Context context;

    /**
     * 执行一次标识
     */
    private boolean once = true;

    /**
     * 记录上垂直方向的滚动距离。
     */
    private static int lastScrollY = -1;

    /**
     * MyScrollView布局的高度。
     */
    private static int scrollViewHeight;

    /**
     * MyScrollView下的直接子布局。
     */
    private static LinearLayout scrollLayout;

    /**
     * 记录当前已加载到第几页
     */
    private int page = 0;

    /**
     * 搜索团购的参数
     */
    private Map<String, String> paramMap;

    /**
     * 日志对象
     */
    private Logger log;

    /**
     * @param context
     */

    public AroundShopScrollView(Context context) {
        super(context);
        log = Logger.getLogger(AroundShopScrollView.class.getName());
    }

    public AroundShopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (once) {
            this.context = getContext();
            scrollViewHeight = getHeight();
            scrollLayout = (LinearLayout) getChildAt(0);

            //将地址栏添加到ScrollView中
            addAddress(scrollLayout, context);

            paramMap = new HashMap<String, String>();
            paramMap.put("radius", "5000");
//            paramMap.put("category", "美食");
//            paramMap.put("city", null);
            paramMap.put("latitude", null);
            paramMap.put("longitude", null);
            paramMap.put("offset_type", "1");//偏移类型
            paramMap.put("sort", "7");//按距离排序

            getNextPage();
            once = false;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        ((AroundShop) context).getmPullScrollView().perpareToRefreshing();
    }

    /**
     * 从大众点评网上获取团购信息
     */
    public void getNextPage() {
        try {
            page++;
//            paramMap.remove("page");
            paramMap.put("page", page + "");

            new GetAroundShop(this.context, paramMap).execute(Constants.AROUNDSHOP_URL);
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 过滤
     * @param index
     */
    public void filter(int index,boolean tuan,boolean quan,boolean onLine){
        String sort=String.valueOf(index);
        paramMap.put("sort",sort);
        if (quan) paramMap.put("has_coupon","1");
        else{
            paramMap.remove("has_coupon");
        }

        if(tuan)  paramMap.put("has_deal","1");
        else{
            paramMap.remove("has_deal");
        }

        if (onLine)paramMap.put("has_online_reservation","1");
        else{
            paramMap.remove("has_online_reservation");
        }
        refresh();
    }

    /**
     * 用户搜索
     *
     * @param keyword 搜索参数
     */
    public void search(String keyword) {
        page = 1;
//        paramMap.remove("page");
        paramMap.put("page", page + "");

        if (keyword!=null) {
            if ("".equals(keyword)) {
                paramMap.remove("keyword");
            } else {
                keyword.trim();
                paramMap.put("keyword", keyword);
            }
        }

        //删除搜索历史标签
        LinearLayout child = (LinearLayout) this.getChildAt(0);
        //child.removeView(child.findViewWithTag(getResources().getString(R.string.search_history_tag)));
        View view=child.findViewWithTag(getResources().getString(R.string.search_history_tag));
        if (view==null || (keyword!=null && !"".equals(keyword))){
            child.removeView(view);
            child.removeViews(1, child.getChildCount() - 2);
        }else{
            child.removeViews(2, child.getChildCount() - 3);
        }

        //重置FootLayout
        ((AroundShop) context).getmPullScrollView().resetPullUpState();

        try {
            new GetAroundShop(this.context, paramMap).execute(Constants.AROUNDSHOP_URL);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 刷新
     */
    public void refresh() {
        search(null);
    }

    /**
     * 将搜索历史加入到ScrollView中
     *
     * @param s
     */
    public void addSearchTag(String s) {
        final LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);//行、内容
        params.setMargins(10, 20, 10, 0);

        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.LEFT);
        linearLayout.setTag(getResources().getString(R.string.search_history_tag));
        linearLayout.setBackgroundResource(R.drawable.tuangou_background_border);
        linearLayout.setPadding(0, 10, 0, 0);
        linearLayout.setAlpha(0.5f);

        ImageView imageView = new ImageView(context);
        imageView.setBackgroundResource(R.drawable.ic_action_search);

        TextView textView = new TextView(context);
        textView.setText("当前搜索:" + s);
        textView.setTextSize(getResources().getDimension(R.dimen.search_history_size));
        textView.setTextColor(getResources().getColor(R.color.search_history));

        ImageView imageView2 = new ImageView(context);
        imageView2.setBackgroundResource(R.drawable.ic_action_close);
        imageView2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LinearLayout) AroundShopScrollView.this.getChildAt(0)).removeView(linearLayout);
                search("");
            }
        });

        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        linearLayout.addView(imageView2);

        ((LinearLayout) this.getChildAt(0)).addView(linearLayout, 0);
    }

    /**
     * 添加地址栏
     *
     * @param scrollView
     * @param context
     */
    private void addAddress(LinearLayout scrollView, final Context context) {

        //地址最高层布局
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setVisibility(INVISIBLE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);//行、内容
        params.setMargins(10, 20, 10, 10);
        linearLayout.setLayoutParams(params);
        linearLayout.setTag(getResources().getString(R.string.address_location_tag));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.LEFT);
        linearLayout.setBackgroundResource(R.drawable.tuangou_background_border);
        linearLayout.setPadding(0, 10, 0, 10);
        linearLayout.setAlpha(0.8f);

        //地址文字布局
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params2.leftMargin = 10;
        //地址刷新布局
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        params3.rightMargin = getResources().getInteger(R.integer.aroundtuangou_address_rightMargin);

        //地址文字
        final TextView address = new TextView(context);
        address.setLayoutParams(params2);
        address.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.address_location_fontsize));
        address.setSingleLine(true);
        address.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        //刷新图标
        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setLayoutParams(params3);
        linearLayout1.setGravity(Gravity.RIGHT);

        final ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_END);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsynUpdateAddress(imageView, address, view.getContext()).execute();
            }
        });

        linearLayout.addView(address);
        linearLayout1.addView(imageView);
        linearLayout.addView(linearLayout1);
        scrollView.addView(linearLayout);
    }
}
