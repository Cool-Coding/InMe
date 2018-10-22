package cn.yang.inme.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShop;
import cn.yang.inme.activity.around.AroundShopDetail;
import cn.yang.inme.layout.AroundShopRefresh;
import cn.yang.inme.refresh.FooterLoadingLayout;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.ImageUtil;
import cn.yang.inme.utils.InMeProgressDialog;
import cn.yang.inme.utils.ToastUtil;
import cn.yang.inme.utils.network.DianpingHttpGet;
import cn.yang.inme.utils.network.LocationSource;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 14-5-23.
 */
public class GetAroundShop extends AsyncTask<String, Integer, String> {

    /**
     * 向大从点评提交URL的参数
     */
    private Map<String, String> params;
    /**
     * 团购上下文环境
     */
    private Context context;
    /**
     * 定位
     */
    private LocationSource locate;
    /**
     * 等待框
     */
    private ProgressDialog progressDialog;
    /**
     * 团购集合
     */
    private LinkedList<LinearLayout> items_list = new LinkedList<LinearLayout>();
    /**
     * 异步加载图片任务
     */
    private static AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
    /**
     * 所有图片的Tag
     */
    private HashMap<String, Integer> tags = new HashMap<String, Integer>();

    /**
     * 店铺Activity
     */
    private AroundShop aroundShop;
    /**
     * 当前页号
     */
    private int page;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param context
     * @param maps
     */
    public GetAroundShop(Context context, Map<String, String> maps) {
        this.params = maps;
        this.context = context;
        this.locate = LocationSource.getLocationSource();
        aroundShop = (AroundShop) context;
    }

    @Override
    protected void onPreExecute() {
        page = Integer.valueOf(params.get("page")).intValue();
        if (page == 1 && !aroundShop.getmPullScrollView().isPullRefreshing()) {
            progressDialog = InMeProgressDialog.instance(context);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        GetAroundShop.this.cancel(true);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected String doInBackground(String... strings) {

        while (locate.getResult() == ' ') ;
        if (locate.getResult() == 'A') {
            return context.getResources().getString(R.string.tuangou_locate_fail);//前面打个‘X’表示错误
        }
        for (Map.Entry<String, String> para : params.entrySet()) {
            String key = para.getKey();
            String value = para.getValue();
            if (value == null || "".equals(value)) {
                try {
                    Method method = LocationSource.class.getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
                    value = (String) method.invoke(locate);
                    para.setValue(value);
                } catch (Exception e) {
//                    Log.e("error",e.getMessage());
                }
            }
        }
        String requestResult = DianpingHttpGet.requestApi(strings[0], Constants.DIANPING_KEY, Constants.DIANPING_SECRET, params);

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap>() {
        }.getType();
        HashMap results = gson.fromJson(requestResult, type);
        if (results == null) {
            return "X请检查网络连接状态";
        }
        String status = (String) results.get("status");
        if ("ERROR".equals(status)) {
            return "X亲，不好意思，出错啦";
        } else {
            int content_color = aroundShop.getResources().getColor(R.color.around_shop_content);

            ArrayList items = (ArrayList) results.get("businesses");
//            Collections.sort(items,new CompareGP());
            if (items == null || (items.size() == 0 && page == 1)) {
                return "X亲，没有搜索到店铺";
            } else {
                int border = context.getResources().getInteger(R.integer.aroundshop_item_border);//边框
                int line_space = context.getResources().getInteger(R.integer.aroundshop_item_linespace);//边框
                float size = context.getResources().getDimension(R.dimen.aroundshop_basesize);
                int color = context.getResources().getColor(R.color.around_color1);
                int color2 = context.getResources().getColor(R.color.around_color2);

                Iterator iterator = items.iterator();
                LinearLayout.LayoutParams shop_item_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                shop_item_params.setMargins(10, 6, 10, 6);

                LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1);//内容
                params5.bottomMargin = border;

                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//行
                params1.bottomMargin = line_space;//行间距
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//标题
                params2.setMargins(border, border, border, 0);
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//人均使用
                params3.setMargins(0, 0, 0,border);

                LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 2);//图片使用
                params4.setMargins(border, border, border, border);

                LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//星级图片、类别使用
                params6.leftMargin = border;
                params6.rightMargin = border;

                LinearLayout.LayoutParams params7 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//评价
                params7.rightMargin = border;

                LinearLayout.LayoutParams params8 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//距离使用
                params8.setMargins(border, 0, border, 0);

                while (iterator.hasNext()) {
                    LinkedTreeMap item = (LinkedTreeMap) iterator.next();

                    final LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setLayoutParams(shop_item_params);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout.setBackgroundResource(R.drawable.shop_item_border);
                    linearLayout.setTag(item);
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LinearLayout item = (LinearLayout) v;
                            LinkedTreeMap uri = (LinkedTreeMap) item.getTag();
                            openWebDialog(context, uri);
                        }
                    });

                    //左边图片
                    ImageView imageView = new ImageView(context);
                    String imageUrl = (String) item.get("s_photo_url");
                    if (tags.containsKey(imageUrl)) {
                        int i = tags.get(imageUrl) + 1;
                        imageView.setTag(imageUrl + i);
                        tags.remove(imageUrl);
                        tags.put(imageUrl, i);
                    } else {
                        imageView.setTag(imageUrl);
                        tags.put(imageUrl, 0);
                    }
                    Bitmap cachedImage = asyncImageLoader.loadDrawable(imageUrl, aroundShop, (String) imageView.getTag(), Constants.AROUNDSHOP_IMAGE);
                    if (cachedImage == null) {
                        imageView.setImageBitmap(ImageUtil.emptyImage);
                    } else {
                        imageView.setImageBitmap(cachedImage);
                    }
                    imageView.setLayoutParams(params4);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    linearLayout.addView(imageView);

                    //右边内容部分
                    LinearLayout content = new LinearLayout(context);
                    content.setLayoutParams(params5);
                    content.setOrientation(LinearLayout.VERTICAL);

                    //第一行
                    LinearLayout firstRow = new LinearLayout(context);
                    firstRow.setLayoutParams(params1);
                    firstRow.setOrientation(LinearLayout.HORIZONTAL);
                    firstRow.setGravity(Gravity.LEFT|Gravity.CENTER);

                    //商户名(分店名)
                    EditText title = new EditText(context);
                    title.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 3);
                    String fendian = item.get("branch_name").toString();
                    fendian = "".equals(fendian) ? "" : "(" + fendian + ")";

                    String text=item.get("name").toString() + fendian;
                    title.setText(text);
                    title.setLayoutParams(params2);
                    title.setTextColor(color);
                    firstRow.addView(title);

                    if (Float.valueOf(item.get("has_deal").toString())==1){
                        Bitmap tuanImage=ImageUtil.tuanImage;
                        SpannableString ss = new SpannableString(text);
                        Drawable d = new BitmapDrawable(tuanImage);
                        d.setBounds(0, 0, tuanImage.getWidth(),tuanImage.getHeight());//设置表情图片的显示大小
                        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                        ss.setSpan(span, 0,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //在光标所在处插入表情
                        title.getText().insert(text.length(), ss);
                    }

                    if (Float.valueOf(item.get("has_coupon").toString())==1) {
                        Bitmap quanImage=ImageUtil.quanImage;
                        SpannableString ss = new SpannableString(title.getText());
                        Drawable d = new BitmapDrawable(quanImage);
                        d.setBounds(0, 0, quanImage.getWidth(), quanImage.getHeight());//设置表情图片的显示大小
                        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                        ss.setSpan(span, 0, title.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //在光标所在处插入表情
                        title.getText().insert(title.length(), ss);
                    }
                    title.setEnabled(false);
                    content.addView(firstRow);

                    //第二行
                    LinearLayout twoRow = new LinearLayout(context);
                    twoRow.setLayoutParams(params1);
                    twoRow.setOrientation(LinearLayout.HORIZONTAL);

                    int pos = -1;
                    //星级
                    RatingBar ratingBar = (RatingBar) aroundShop.getLayoutInflater().inflate(R.layout.aroundshop_star, null);
                    ratingBar.setRating(Float.valueOf(item.get("avg_rating").toString()).floatValue());
                    ratingBar.setLayoutParams(params6);

                    //评价
                    TextView comment_txt = new TextView(context);
                    String comments = item.get("review_count").toString();
                    comment_txt.setLayoutParams(params7);
                    comment_txt.setGravity(Gravity.RIGHT);

                    if ((pos = comments.lastIndexOf(".0")) != -1) {
                        comments = comments.substring(0, pos);
                    }
                    comment_txt.setText(comments + "人评价");
                    comment_txt.setTextColor(content_color);
                    comment_txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

                    twoRow.addView(ratingBar);
                    twoRow.addView(comment_txt);
                    content.addView(twoRow);

                    LinearLayout threeRow = new LinearLayout(context);
                    threeRow.setLayoutParams(params1);
                    threeRow.setOrientation(LinearLayout.HORIZONTAL);
                    threeRow.setGravity(Gravity.LEFT);

                    TextView ren=new TextView(context);
                    ren.setText("人均");
                    ren.setLayoutParams(params3);
                    ren.setPadding(border,0,0,0);
                    ren.setTextColor(content_color);

                    TextView ren2=new TextView(context);
                    ren2.setText("元");
                    ren2.setLayoutParams(params3);
                    ren2.setTextColor(content_color);

                    TextView ave_price = new TextView(context);
                    ave_price.setLayoutParams(params3);
                    ave_price.setGravity(Gravity.LEFT);
                    ave_price.setTextColor(context.getResources().getColor(R.color.around_color3));
                    String price = item.get("avg_price").toString();
                    if ((pos = price.lastIndexOf(".0")) != -1) {
                        price = price.substring(0, pos);
                    }
                    ave_price.setText(price);

                    threeRow.addView(ren);
                    threeRow.addView(ave_price);
                    threeRow.addView(ren2);
                    content.addView(threeRow);

                    //第三行
                    //分类
                    TextView category_txt = new TextView(context);
//                    category_txt.setPadding(border, 0, 0, 0);
                    category_txt.setLayoutParams(params6);
//                  cate=Pattern.compile("[\\[,\\]]").matcher(cate).replaceAll("");
                    category_txt.setText("类别:" + item.get("categories").toString().replaceAll("[\\[,\\]]", ""));
                    category_txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    category_txt.setTextColor(content_color);

                    //距离
                    TextView dis_vie = new TextView(context);
                    dis_vie.setLayoutParams(params8);
                    dis_vie.setGravity(Gravity.RIGHT);
                    dis_vie.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    dis_vie.setTextColor(color2);

                    String distance = item.get("distance").toString();
                    if ((pos = distance.lastIndexOf(".0")) != -1) {
                        distance = distance.substring(0, pos);
                    }
                    Integer dis_int = Integer.parseInt(distance);
                    if (dis_int < 1000) {
                        dis_vie.setText(dis_int + "m");
                    } else {
                        float fdis = (float) dis_int / 1000;
                        int temp = (int) (fdis * 100);
                        fdis = (float) temp / 100;
                        dis_vie.setText(fdis + "km");
                    }

                    LinearLayout fourRow = new LinearLayout(context);
                    fourRow.setLayoutParams(params1);
                    fourRow.setOrientation(LinearLayout.HORIZONTAL);
                    fourRow.setGravity(Gravity.BOTTOM);
                    fourRow.addView(category_txt);
                    fourRow.addView(dis_vie);
                    content.addView(fourRow);

                    linearLayout.addView(content);
                    items_list.add(linearLayout);
                }
            }
        }
        return requestResult;
    }

    @Override
    protected void onPostExecute(String s) {

        //如果有错误出现
        if (s.charAt(0) == 'X') {
            ToastUtil.show(context, s.substring(1));
        } else {
            //得到ScrollView第一子节点LinearLayout
            LinearLayout linearLayout = (LinearLayout) aroundShop.getmScrollView().getChildAt(0);
            Iterator<LinearLayout> items_iter = items_list.iterator();

            //判断是否有数据，没数据则提示已经到底
            if (!items_iter.hasNext()) {
                AroundShopRefresh mPullScrollView = aroundShop.getmPullScrollView();
                mPullScrollView.setHasMoreData(false);
                return;
            }

            //暂时删除地址和刷新区域
            LinearLayout footer = (LinearLayout) linearLayout.findViewWithTag(context.getResources().getString(R.string.aroundshop_footer_tag));
            LinearLayout location_address = (LinearLayout) linearLayout
                    .findViewWithTag(context.getResources().getString(R.string.address_location_tag));
            location_address.setVisibility(View.VISIBLE);
            //linearLayout.removeView(location_address);
            linearLayout.removeView(footer);

            //暂时删除大众点评标识
            LinearLayout dp_flag = (LinearLayout) linearLayout.findViewById(R.id.dianping_flag);
            if (dp_flag != null) {
                linearLayout.removeView(dp_flag);
            }

            //如果是第一页，则删除ScrollView中的店铺
            int page = Integer.valueOf(params.get("page")).intValue();
            if (page == 1) {
                setLastUpdateTime();
            }

            //添加团购项目
            while (items_iter.hasNext()) {
                linearLayout.addView(items_iter.next());
            }
            //linearLayout.addView(location_address, 0);
            //添加大众点评标识
            LinearLayout dianping_flag = (LinearLayout) aroundShop.getLayoutInflater().inflate(R.layout.dianping_flag, null);
            linearLayout.addView(dianping_flag);

            linearLayout.addView(footer, linearLayout.getChildCount());

            //显示地址
            TextView address = (TextView) location_address.getChildAt(0);
            if (address != null) {
                String str = locate.getAddress();
                if (str != null) {
                    address.setText(str.replaceAll(" ", ""));
                }
            }
            //显示刷新图标
            ImageView imageView = (ImageView) ((LinearLayout) location_address.getChildAt(1)).getChildAt(0);
            imageView.setBackgroundResource(R.drawable.ic_refresh);

            //发送消息，更新图片
            aroundShop.getMyHandler().sendEmptyMessage(0);
        }


        if (progressDialog != null && progressDialog.isShowing()) {
            //关闭等待框
            progressDialog.dismiss();
        } else {
            AroundShopRefresh mPullScrollView = aroundShop.getmPullScrollView();
            mPullScrollView.onPullDownRefreshComplete();
            mPullScrollView.onPullUpRefreshComplete();
        }
    }

    private void openWebDialog(Context context, LinkedTreeMap uri) {
        Intent intent = new Intent(context, AroundShopDetail.class);
        intent.putExtra("shop", uri);
        context.startActivity(intent);
    }

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        AroundShopRefresh mPullScrollView = aroundShop.getmPullScrollView();
        mPullScrollView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }
}
