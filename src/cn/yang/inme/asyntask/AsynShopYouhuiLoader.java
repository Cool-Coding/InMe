package cn.yang.inme.asyntask;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShopDetail;
import cn.yang.inme.activity.around.PurchaseGroupDetail;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.InMeTextProgressDialog;
import cn.yang.inme.utils.border.BorderLinearLayout;
import cn.yang.inme.utils.network.DianpingHttpGet;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 14-6-12.
 */
public class AsynShopYouhuiLoader extends AsyncTask<LinkedTreeMap, Void, LinearLayout> {

    private AroundShopDetail aroundShop;
    private LinearLayout youhui;
    private float size;
    private int content_color;
    private Logger log;

    public AsynShopYouhuiLoader(Context context) {
        this.aroundShop = (AroundShopDetail) context;
        log = Logger.getLogger(AsynShopYouhuiLoader.class.getName());
    }

    @Override
    protected void onPreExecute() {
        youhui = (LinearLayout) aroundShop.findViewById(R.id.youhui);
        youhui.setVisibility(View.VISIBLE);

        size = aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_basesize) + 2;
        content_color = aroundShop.getResources().getColor(R.color.around_shop_content);

        TextView tip = InMeTextProgressDialog.instance(aroundShop, new int[]{
                R.string.youhui_loading_tip1, R.string.youhui_loading_tip2, R.string.youhui_loading_tip3
        });
        tip.setTextSize(size);
        youhui.addView(tip);
    }

    @Override
    protected LinearLayout doInBackground(LinkedTreeMap... params) {

        final LinkedTreeMap youhui_map = params[0];

        //得到团购详细信息
        String id = youhui_map.get("coupon_id").toString();
        if (id == null || "0.0".equals(id) || "".equals(id)) return null;
        int index=id.lastIndexOf(".");
        id=id.substring(0,index);

        String url = "http://api.dianping.com/v1/coupon/get_single_coupon";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("coupon_id", id);
        String requestResult = DianpingHttpGet.requestApi(url, Constants.DIANPING_KEY, Constants.DIANPING_SECRET, paramMap);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap>() {}.getType();
        HashMap results = gson.fromJson(requestResult, type);
        String status = (String) results.get("status");
        if ("ERROR".equals(status)) {
            LinkedTreeMap error = (LinkedTreeMap) results.get("error");
            log.error("coupon_id:" + id + ";" + error.get("errorCode") + ":" + error.get("errorMessage"));
            return null;
        } else {
            ArrayList items = (ArrayList) results.get("coupons");
            final LinkedTreeMap item = (LinkedTreeMap) items.get(0);

            String youhui_str = item.get("description").toString();
            if (youhui_str == null || "".equals(youhui_str)) return null;

            LinearLayout.LayoutParams shop_item_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            shop_item_params.setMargins(0, 10, 0, 10);

            LinearLayout.LayoutParams quan_icon_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            quan_icon_params.topMargin = 10;

            LinearLayout.LayoutParams youhui_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            youhui_params.setMargins(0, 10, 0, 0);

            LinearLayout.LayoutParams youhui_dealline_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams youhui_salescount_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            BorderLinearLayout youhui_layout = new BorderLinearLayout(aroundShop);
            youhui_layout.setBottomBorder(Color.GRAY,1);
            youhui_layout.setOrientation(LinearLayout.VERTICAL);
            youhui_layout.setLayoutParams(shop_item_params);
            youhui_layout.setBackgroundResource(R.drawable.tuangou_item_border);
            youhui_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PurchaseGroupDetail.class);
                    intent.putExtra("detail_url", item.get("coupon_h5_url").toString());
                    intent.putExtra("detail_title", item.get("title").toString());
                    v.getContext().startActivity(intent);
                }
            });

            //优惠券描述
            LinearLayout youhui_text_layout = new LinearLayout(aroundShop);
            youhui_text_layout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView juan_ico = new ImageView(aroundShop);
            juan_ico.setLayoutParams(quan_icon_params);
            juan_ico.setBackgroundResource(R.drawable.quan_icon);

            TextView youhui_text = new TextView(aroundShop);
            youhui_text.setText(youhui_str);
            youhui_text.setTextSize(size);

            youhui_text_layout.addView(juan_ico);
            youhui_text_layout.addView(youhui_text);

            //已购买团购人数
            LinearLayout youhui_salescount = new LinearLayout(aroundShop);
            youhui_salescount.setOrientation(LinearLayout.HORIZONTAL);
            youhui_salescount.setLayoutParams(youhui_params);

            TextView date = new TextView(aroundShop);
            date.setText("截止日期:" + item.get("expiration_date").toString());
            date.setLayoutParams(youhui_dealline_params);
            date.setPadding(BitmapFactory.decodeResource(aroundShop.getResources(), R.drawable.quan_icon).getWidth(), 0, 0, 0);
            date.setTextColor(content_color);
            date.setTextSize(size - 4);

            int pos = -1;
            TextView salescount = new TextView(aroundShop);
            String person = item.get("download_count").toString();
            if ((pos = person.lastIndexOf(".0")) != -1) {
                person = person.substring(0, pos);
            }
            salescount.setText(person + "人购买");
            salescount.setLayoutParams(youhui_salescount_params);
            salescount.setGravity(Gravity.RIGHT);
            salescount.setTextColor(content_color);
            salescount.setTextSize(size - 4);

            youhui_salescount.addView(date);
            youhui_salescount.addView(salescount);


            youhui_layout.addView(youhui_text_layout);
            youhui_layout.addView(youhui_salescount);
            return youhui_layout;
        }
    }

    @Override
    protected void onPostExecute(LinearLayout s) {
        if (s == null || s.getChildCount() == 0) {
            youhui.removeViewAt(0);
            TextView textView = new TextView(aroundShop);
            textView.setText(aroundShop.getResources().getString(R.string.youhui_loading_result));
            textView.setTextSize(size);
            youhui.addView(textView);
        } else {
            youhui.removeAllViews();
            youhui.addView(s);
        }
    }
}
