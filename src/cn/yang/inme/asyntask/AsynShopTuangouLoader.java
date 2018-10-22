package cn.yang.inme.asyntask;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 14-6-12.
 */
public class AsynShopTuangouLoader extends AsyncTask<ArrayList, Void, ArrayList<LinearLayout>> {

    private AroundShopDetail aroundShop;
    private LinearLayout tuangou;
    private float size;//团购内容的基准字体
    private int content_color;

    public AsynShopTuangouLoader(Context context) {
        this.aroundShop = (AroundShopDetail) context;
    }

    @Override
    protected void onPreExecute() {
        tuangou = (LinearLayout) aroundShop.findViewById(R.id.tuangou);
        tuangou.setVisibility(View.VISIBLE);

        size = aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_basesize) + 2;
        content_color = aroundShop.getResources().getColor(R.color.around_shop_content);

        TextView tip = InMeTextProgressDialog.instance(aroundShop,new int[]{
                R.string.tuangou_loading_tip1,R.string.tuangou_loading_tip2,R.string.tuangou_loading_tip3
        });
        tip.setTextSize(size);





        tuangou.addView(tip);
    }

    @Override
    protected ArrayList<LinearLayout> doInBackground(ArrayList... params) {

        ArrayList<LinearLayout> tuangou_items = new ArrayList<LinearLayout>();

        LinearLayout.LayoutParams shop_item_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        shop_item_params.setMargins(0, 10, 0, 10);

        LinearLayout.LayoutParams tuan_icon_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tuan_icon_params.topMargin = 10;

        LinearLayout.LayoutParams tuan_dealline_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tuan_dealline_params.topMargin=10;

        LinearLayout.LayoutParams tuan_salescount_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tuan_salescount_params.topMargin=10;

        int tuan_ico_width = BitmapFactory.decodeResource(aroundShop.getResources(), R.drawable.tuan_icon).getWidth();
        ArrayList tuangou_list = params[0];
        Iterator iterator = tuangou_list.iterator();
        while (iterator.hasNext()) {
            final HashMap tuangou_map = (HashMap) iterator.next();

            //得到团购详细信息
            String url = "http://api.dianping.com/v1/deal/get_single_deal";
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("deal_id", tuangou_map.get("id").toString());
            String requestResult = DianpingHttpGet.requestApi(url, Constants.DIANPING_KEY, Constants.DIANPING_SECRET, paramMap);
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap>() {
            }.getType();
            HashMap results = gson.fromJson(requestResult, type);
            if (results==null)return null;//网络不通时
            String status = (String) results.get("status");
            if ("ERROR".equals(status)) {
                return null;
            } else {
                ArrayList items = (ArrayList) results.get("deals");
                final LinkedTreeMap item = (LinkedTreeMap) items.get(0);

                String tuan_str = item.get("description").toString();
                if (tuan_str == null || "".equals(tuan_str)) return null;

                BorderLinearLayout tuangou_layout = new BorderLinearLayout(aroundShop);
                tuangou_layout.setBottomBorder(Color.GRAY,1);

                tuangou_layout.setOrientation(LinearLayout.VERTICAL);
                tuangou_layout.setBackgroundResource(R.drawable.tuangou_item_border);
                tuangou_layout.setLayoutParams(shop_item_params);
                tuangou_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), PurchaseGroupDetail.class);
                        intent.putExtra("detail_url",   item.get("deal_h5_url").toString());
                        intent.putExtra("detail_title", item.get("title").toString());
                        v.getContext().startActivity(intent);
                    }
                });
                //团购描述
                LinearLayout tuangou_text_layout = new LinearLayout(aroundShop);
                tuangou_text_layout.setOrientation(LinearLayout.HORIZONTAL);

                ImageView tuan_ico = new ImageView(aroundShop);
                tuan_ico.setLayoutParams(tuan_icon_params);
                tuan_ico.setBackgroundResource(R.drawable.tuan_icon);

                TextView tuangou_text = new TextView(aroundShop);
                tuangou_text.setTextSize(size);
                tuangou_text.setText(tuan_str);

                tuangou_text_layout.addView(tuan_ico);
                tuangou_text_layout.addView(tuangou_text);

                //已购买团购人数
                LinearLayout tuangou_salescount = new LinearLayout(aroundShop);
                tuangou_salescount.setOrientation(LinearLayout.HORIZONTAL);

                TextView date = new TextView(aroundShop);
                date.setText("截止日期:" + item.get("purchase_deadline").toString());
                date.setLayoutParams(tuan_dealline_params);
                date.setPadding(tuan_ico_width, 0, 0, 0);
                date.setTextColor(content_color);
                date.setTextSize(size - 4);

                int pos = -1;
                TextView salescount = new TextView(aroundShop);
                String person = item.get("purchase_count").toString();
                if ((pos = person.lastIndexOf(".0")) != -1) {
                    person = person.substring(0, pos);
                }
                salescount.setText(person + "人购买");
                salescount.setLayoutParams(tuan_salescount_params);
                salescount.setGravity(Gravity.RIGHT);
                salescount.setTextColor(content_color);
                salescount.setTextSize(size - 4);

                tuangou_salescount.addView(date);
                tuangou_salescount.addView(salescount);

                tuangou_layout.addView(tuangou_text_layout);
                tuangou_layout.addView(tuangou_salescount);

                //添加团购
                tuangou_items.add(tuangou_layout);
            }
        }
        return tuangou_items;
    }

    @Override
    protected void onPostExecute(ArrayList<LinearLayout> s) {

        if (s == null || s.size() == 0) {
            tuangou.removeViewAt(0);
            TextView textView = new TextView(aroundShop);
            textView.setText(aroundShop.getResources().getString(R.string.tuangou_loading_result));
            textView.setTextSize(size);
            tuangou.addView(textView);
        } else {
            tuangou.removeAllViews();
            Iterator<LinearLayout> iterator = s.iterator();
            while (iterator.hasNext()) {
                tuangou.addView(iterator.next());
            }
        }
    }
}
