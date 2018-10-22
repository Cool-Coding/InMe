package cn.yang.inme.activity.around;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.activity.map.ShopLocation;
import cn.yang.inme.asyntask.AsynShopCommentLoader;
import cn.yang.inme.asyntask.AsynShopTuangouLoader;
import cn.yang.inme.asyntask.AsynShopYouhuiLoader;
import cn.yang.inme.asyntask.AsyncImageLoader;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.ToastUtil;
import cn.yang.inme.utils.border.BottomBorderLinearLayout;
import com.amap.api.services.core.LatLonPoint;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.log4j.Logger;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 14-6-12.
 */
public class AroundShopDetail extends Activity implements AsyncImageLoader.ImageCallback {

    private HashMap shop;
    private MyHandler myHandler;
    private  Bundle saveInstanceState;

    /**
     * 异步加载图片任务
     */
    private static AsyncImageLoader asyncImageLoader = new AsyncImageLoader();

    private Logger log = Logger.getLogger(AroundShopDetail.class.getName());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aroundshopdetail);
        getActionBar().setDisplayShowTitleEnabled(false);

        myHandler = new MyHandler();
        this.saveInstanceState=saveInstanceState;

        //设置ActionBar颜色
        setActionBarColor();
        //设置背景图片
        FrameLayout scrollView = (FrameLayout) findViewById(R.id.aroundshop);
        ApplicationUtil.setThemeBackground(scrollView);

        //得到店铺
        Intent intent = getIntent();
        shop = (HashMap) intent.getSerializableExtra("shop");
        if (shop == null) {
                if (Constants.shop!=null) shop=(HashMap)Constants.shop;
                else {
                    log.error("周边店铺详细信息打开出错");
                    return;
                }
        }
        Constants.shop=shop;
        /**获取店铺信息*/
        //店铺图片
        String imageUrl = shop.get("photo_url").toString();
        if (!"".equals(imageUrl)) {
            ImageView imageView = (ImageView) findViewById(R.id.shopimage);
            ImageView imageView2 = (ImageView) findViewById(R.id.shopimage2);
            imageView.setTag(imageUrl);
            Bitmap cachedImage = asyncImageLoader.loadDrawable(imageUrl, this, (String) imageView.getTag(), Constants.AROUNDSHOP_DETAIL_IMAGE);
            if (cachedImage == null) {
                imageView.setImageResource(R.drawable.default_image);
                imageView2.setImageResource(R.drawable.default_image);
            } else {
                imageView.setImageBitmap(cachedImage);
                imageView2.setImageBitmap(cachedImage);
            }
        }

        float size = getResources().getDimension(R.dimen.aroundshop_detail_basesize);
        int pos = -1;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);//店名
        params1.topMargin = 20;
        params1.bottomMargin = 10;

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);//人均价
        params2.setMargins(0, 10, 0, 5);

        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 2);//几人评价、在线预订区域

        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);//电话、地址
//        params4.setMargins(0, 20, 0, 20);

        LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);//人均、在线预订

        LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 5);//电话、地址右边图标
        params6.setMargins(0, 0, 5, 0);

        int content_color = getResources().getColor(R.color.around_shop_content);

        //评价区域大标题
        TextView comments_title = (TextView) findViewById(R.id.comments_title);
        comments_title.setTextSize(size + 2);
        String comments = shop.get("review_count").toString();
        if ((pos = comments.lastIndexOf(".0")) != -1) {
            comments = comments.substring(0, pos);
        }
        comments_title.setText(comments_title.getText().toString()+"("+comments+")");

        //店名
        BottomBorderLinearLayout shopname = (BottomBorderLinearLayout) findViewById(R.id.shopname);
        shopname.setOrientation(LinearLayout.VERTICAL);
        String background = PropertiesUtil.instance().read(Constants.SET_THEME_BACKGROUND);
        if (background != null && !"".equals(background)) {
            shopname.clearBorder();
        }

        TextView title = new TextView(this);
        title.setTextSize(size + 5);
        title.setGravity(Gravity.CENTER);
        title.setLayoutParams(params1);
        String fendian = shop.get("branch_name").toString();
        fendian = "".equals(fendian) ? "" : "(" + fendian + ")";
        final String shopName=shop.get("name").toString() + fendian;
        title.setText(shopName);

        //设置大标题
        //getActionBar().setTitle(shopName);

        //人均
        TextView ave_price = new TextView(this);
        String price = shop.get("avg_price").toString();
        if ((pos = price.lastIndexOf(".0")) != -1) {
            price = price.substring(0, pos);
        }
        ave_price.setText("人均" + price + "元");
        ave_price.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        ave_price.setLayoutParams(params2);
        ave_price.setTextSize(size);

        shopname.addView(title);
        shopname.addView(ave_price);

        /**获取星级评价详情与人均价格*/
        LinearLayout rating = (LinearLayout) findViewById(R.id.comments);
        rating.setPadding(0, 15, 0, 15);

        //星级
        rating.addView(commentRating("环境", "decoration_grade", size - 1));
        rating.addView(commentRating("服务", "service_grade", size - 1));
        rating.addView(commentRating("品味", "product_grade", size - 1));

        //评价
        LinearLayout comment_layout = (LinearLayout) findViewById(R.id.ave_price);
        comment_layout.setLayoutParams(params3);
        comment_layout.setOrientation(LinearLayout.VERTICAL);


        TextView comment_txt = new TextView(this);
        comment_txt.setText(comments + "人评价");
        comment_txt.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        comment_txt.setTextSize(size);
        comment_txt.setLayoutParams(params5);
        comment_txt.setTextColor(content_color);

        comment_layout.addView(comment_txt);
        //在线预订
        if (Float.valueOf(shop.get("has_online_reservation").toString())==1) {
            final TextView onlinebook = new TextView(this);
            onlinebook.setText("在线预订>>");
            onlinebook.setPadding(0,0,5,0);
            onlinebook.setTextSize(size + 1);
            onlinebook.setLayoutParams(params5);
            onlinebook.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
            onlinebook.setTextColor(this.getResources().getColor(R.color.around_shop_book_up));
//            onlinebook.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            onlinebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PurchaseGroupDetail.class);
                    intent.putExtra("detail_url", shop.get("online_reservation_url").toString());
                    intent.putExtra("detail_title",shop.get("name").toString()+"在线预订服务");
                    v.getContext().startActivity(intent);
                }
            });
            onlinebook.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            onlinebook.setTextColor(AroundShopDetail.this.getResources().getColor(R.color.around_shop_book_down));
                            break;
                        case MotionEvent.ACTION_UP:
                            onlinebook.setTextColor(AroundShopDetail.this.getResources().getColor(R.color.around_shop_book_up));
                            break;
                    }
                    return false;
                }
            });
            comment_layout.addView(onlinebook);
        }
        //电话
        final String tele = shop.get("telephone").toString();
        LinearLayout telephone = (LinearLayout) findViewById(R.id.telephone);
        if (!"".equals(tele)) {
            telephone.setPadding(0, 20, 0, 20);
            telephone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupListDialog("tel:" + tele);
                }
            });

            TextView telephone_text = new TextView(this);
            telephone_text.setLayoutParams(params4);
            telephone_text.setGravity(Gravity.CENTER_VERTICAL);
            telephone_text.setText("电话:" + tele);
            telephone_text.setTextSize(size + 2);

            LinearLayout tele_layout = new LinearLayout(this);
            tele_layout.setLayoutParams(params6);
            tele_layout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            ImageView tele_icon = new ImageView(this);
            tele_icon.setBackgroundResource(R.drawable.shop_detail_phone);
            tele_layout.addView(tele_icon);


            telephone.addView(telephone_text);
            telephone.addView(tele_layout);
        } else {
            telephone.setVisibility(View.GONE);
        }
        //地址
        String add = shop.get("address").toString();
        LinearLayout address = (LinearLayout) findViewById(R.id.address);
        if (!"".equals(add)) {
            address.setPadding(0, 20, 0, 20);
            address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double latitude = Double.valueOf(shop.get("latitude").toString());
                    Double longitude = Double.valueOf(shop.get("longitude").toString());

                    Intent intent = new Intent(AroundShopDetail.this, ShopLocation.class);
                    intent.putExtra(getResources().getString(R.string.search_location_tag)
                            , new LatLonPoint(latitude, longitude));
                    intent.putExtra("name",shopName);
                    startActivity(intent);
                }
            });

            TextView address_text = new TextView(this);
            address_text.setLayoutParams(params4);
            address_text.setText("地址:" + add);
            address_text.setGravity(Gravity.CENTER_VERTICAL);
            address_text.setTextSize(size + 2);

            LinearLayout map = new LinearLayout(this);
            map.setLayoutParams(params6);
            map.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            ImageView map_icon = new ImageView(this);
            map_icon.setBackgroundResource(R.drawable.shop_detail_map);
            map.addView(map_icon);

            address.addView(address_text);
            address.addView(map);
        } else {
            address.setVisibility(View.GONE);
        }
        //团购区域
        if (Float.valueOf(shop.get("has_deal").toString())==1) {
            new AsynShopTuangouLoader(this).execute((ArrayList) shop.get("deals"));
        }
        //券区域
        if (Float.valueOf(shop.get("has_coupon").toString())==1) {
            LinkedTreeMap youhui = new LinkedTreeMap();
            youhui.put("coupon_id", shop.get("coupon_id"));
            youhui.put("coupon_description", shop.get("coupon_description"));
            youhui.put("coupon_url ", shop.get("coupon_url "));

            new AsynShopYouhuiLoader(this).execute(youhui);
        }
        //评价区域
        Double businessid = (Double) shop.get("business_id");
        new AsynShopCommentLoader(this,shopName).execute(businessid.intValue());
    }

    private LinearLayout commentRating(String tip, String dianping, float size) {

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);//店名

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(params1);
        //标题
        TextView envirment_tip = new TextView(this);
        envirment_tip.setText(tip + ":");
        envirment_tip.setTextSize(size);
        //星星
        RatingBar envirmentrating = (RatingBar) getLayoutInflater().inflate(R.layout.aroundshop_star, null);
        envirmentrating.setRating(Float.valueOf(shop.get(dianping).toString()).floatValue());
        linearLayout.addView(envirment_tip);
        linearLayout.addView(envirmentrating);

        return linearLayout;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        if(shop==null && Constants.shop!=null){
            System.out.println("resume");
            onCreate(saveInstanceState);
        }
        System.out.print("resume2");*/
    }

    @Override
    public void imageLoaded(Bitmap imageDrawable, String tag) {
        myHandler.sendMessage(myHandler.obtainMessage(0, imageDrawable));
    }

    /**
     * 电话号码字符串
     *
     * @param url
     */
    public void popupListDialog(String url) {

        String telephone;
        try {
            telephone = URLDecoder.decode(url, "UTF-8");
            final String[] items = telephone.split("[，,、;；]");
            if (items.length > 1) {
                for (int i = 0; i < items.length; i++) {
                    if (items[i].startsWith("tel:")) {
                        items[i] = items[i].substring(4);
                        break;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + items[which]));
                                AroundShopDetail.this.startActivity(intent);
                            }
                        }
                );
                builder.show();
            } else {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                AroundShopDetail.this.startActivity(intent);
            }
        } catch (Exception e) {
            ToastUtil.show(AroundShopDetail.this, "不好意思，拨打电话功能暂时不可用!");
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

    /**
     * 处理团购图片异步显示的Handler内部类
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ImageView imageView = (ImageView) findViewById(R.id.shopimage);
            ImageView imageView2 = (ImageView) findViewById(R.id.shopimage2);
            imageView.setImageBitmap((Bitmap) msg.obj);
            imageView2.setImageBitmap((Bitmap) msg.obj);
        }
    }
}