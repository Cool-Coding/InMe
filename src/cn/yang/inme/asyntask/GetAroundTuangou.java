package cn.yang.inme.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundTuangou;
import cn.yang.inme.activity.around.PurchaseGroupDetail;
import cn.yang.inme.layout.AroundTuangouRefresh;
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
public class GetAroundTuangou extends AsyncTask<String, Integer, String> {

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
     * 所有图片的Tag
     */
    private static HashMap<String, Integer> tags = new HashMap<String, Integer>();

    /**
     * 团购Activity
     */
    private AroundTuangou aroundTuangou;

    /**
     * 当前查询的页
     */
    private int page;


    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param context
     * @param maps
     */
    public GetAroundTuangou(Context context, Map<String, String> maps) {
        this.params = maps;
        this.context = context;
        this.locate = LocationSource.getLocationSource();
        aroundTuangou = (AroundTuangou) context;
    }

    public static void clearImageTages() {
        tags.clear();
    }

    @Override
    protected void onPreExecute() {
        page = Integer.valueOf(params.get("page")).intValue();
        if (page == 1 && !aroundTuangou.getmPullScrollView().isPullRefreshing()) {
            progressDialog = InMeProgressDialog.instance(context);
            aroundTuangou.setProgressdialog(progressDialog);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        GetAroundTuangou.this.cancel(true);
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
//                    log.error("error",e.getMessage());
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
            ArrayList items = (ArrayList) results.get("deals");
            if (items == null || (items.size() == 0 && page == 1)) {
                return "X亲，没有查到相关团购活动";
            } else {
                int border = context.getResources().getInteger(R.integer.aroundtuangou_item_border);//边框
                float size = context.getResources().getDimension(R.dimen.aroundtuangou_basesize);
                int color = context.getResources().getColor(R.color.around_color1);
                int color2 = context.getResources().getColor(R.color.around_color2);
                int color3 = context.getResources().getColor(R.color.around_color3);

                Iterator iterator = items.iterator();
                LinearLayout.LayoutParams tuangou_item_params = new LinearLayout
                        .LayoutParams(Constants.AROUNDTUANGOU_IMAGE - 20,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                tuangou_item_params.setMargins(10, 5, 10, 10);

                LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);//内容
                params5.bottomMargin = border;

                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);//行

                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1);//标题
                params2.setMargins(border, 0, 0, 0);
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 2);//距离、已售使用
                params3.setMargins(0, 0, border, 0);
                LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        200);//图片使用
                params4.setMargins(border, border, border, 10);
                params4.gravity=Gravity.TOP;

                while (iterator.hasNext()) {
                    final LinkedTreeMap item = (LinkedTreeMap) iterator.next();

                    final LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setLayoutParams(tuangou_item_params);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setTag(item.get("deal_h5_url"));
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LinearLayout item2 = (LinearLayout) v;
                            String url = (String) item2.getTag();
                            openWebDialog(context, item.get("title").toString(), url);
                        }
                    });

                    //上面图片
                    ImageView imageView = new ImageView(context);
                    imageView.setLayoutParams(params4);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    String imageUrl = (String) item.get("image_url");
                    if (tags.containsKey(imageUrl)) {
                        int i = tags.get(imageUrl) + 1;
                        imageView.setTag(imageUrl + "*" + i);
//                        tags.remove(imageUrl);
                        tags.put(imageUrl, i);
                    } else {
                        imageView.setTag(imageUrl);
                        tags.put(imageUrl, 0);
                    }
                    //设置图片状态标识为1:有图
                    imageView.setTag(R.string.image_status, "1");

                    Bitmap cachedImage = AsyncImageLoader.loadDrawable(imageUrl, (AroundTuangou) context, (String) imageView.getTag(), Constants.AROUNDTUANGOU_IMAGE);
                    if (cachedImage == null) {
                        imageView.setImageBitmap(ImageUtil.emptyImage);
                    } else {
                        imageView.setImageBitmap(cachedImage);
                    }

                    linearLayout.addView(imageView);

                    //下边内容部分
                    LinearLayout content = new LinearLayout(context);
                    content.setLayoutParams(params5);
                    content.setGravity(Gravity.BOTTOM);
                    content.setOrientation(LinearLayout.VERTICAL);

                    //第一行
                    LinearLayout firstRow = new LinearLayout(context);
                    firstRow.setLayoutParams(params1);
                    firstRow.setOrientation(LinearLayout.HORIZONTAL);

                    //标题
                    TextView title = new TextView(context);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    title.setText(item.get("title").toString());
                    title.setLayoutParams(params2);
                    title.setTextColor(color);

                    int pos = -1;
                    //距离
                    TextView dis_vie = new TextView(context);
                    dis_vie.setLayoutParams(params3);
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


                    firstRow.addView(title);
                    firstRow.addView(dis_vie);
                    content.addView(firstRow);

                    //第二行
                    LinearLayout twoRow = new LinearLayout(context);
                    twoRow.setLayoutParams(params1);
                    twoRow.setOrientation(LinearLayout.HORIZONTAL);


                    //现价
                    String price_txt;
                    TextView now_price = new TextView(context);
                    price_txt = item.get("current_price").toString();
                    if ((pos = price_txt.indexOf(".0")) != -1) {
                        price_txt = price_txt.substring(0, pos);
                    }
                    now_price.setText(price_txt);
                    now_price.setGravity(Gravity.BOTTOM);
                    now_price.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 8);
                    now_price.setTextColor(color3);

                    TextView unit = new TextView(context);
                    unit.setText("元");
                    unit.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 2);

                    twoRow.addView(now_price);
                    twoRow.addView(unit);
                    content.addView(twoRow);

                    //第三行
                    //原价
                    TextView old_price = new TextView(context);
                    price_txt = item.get("list_price").toString();
                    old_price.setPadding(5, 0, 2, 0);
                    if ((pos = price_txt.indexOf(".0")) != -1) {
                        price_txt = price_txt.substring(0, pos);
                    }
                    old_price.setText("原价" + price_txt);
                    old_price.setTextSize(size);
                    old_price.setTextColor(Color.GRAY);
                    old_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    //已售
                    TextView buy_account = new TextView(context);
                    buy_account.setLayoutParams(params3);
                    buy_account.setGravity(Gravity.RIGHT);
                    buy_account.setTextSize(size - 1);
                    String person = item.get("purchase_count").toString();
                    if ((pos = person.lastIndexOf(".0")) != -1) {
                        person = person.substring(0, pos);
                    }
                    buy_account.setText("已售" + person + "人");
                    buy_account.setTextColor(color);

                    LinearLayout threeRow = new LinearLayout(context);
                    threeRow.setLayoutParams(params1);
                    threeRow.setOrientation(LinearLayout.HORIZONTAL);
                    threeRow.setGravity(Gravity.BOTTOM);
                    threeRow.addView(old_price);
                    threeRow.addView(buy_account);
                    content.addView(threeRow);

                    linearLayout.addView(content);
                    linearLayout.setBackgroundResource(R.drawable.tuangou_item_border);
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
            LinearLayout linearLayout = (LinearLayout) aroundTuangou.getmScrollView().getChildAt(0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            int cols = aroundTuangou.getTuangou_cols();
            Iterator<LinearLayout> items_iter = items_list.iterator();
            //判断是否有数据，没数据则提示已经到底
            if (!items_iter.hasNext()) {
                AroundTuangouRefresh mPullScrollView = aroundTuangou.getmPullScrollView();
                mPullScrollView.setHasMoreData(false);
                return;
            }
            //暂时删除大众点评标识
            LinearLayout dp_flag = (LinearLayout) linearLayout.findViewById(R.id.dianping_flag);
            if (dp_flag != null) {
                linearLayout.removeView(dp_flag);
            }
            int load_count = items_list.size();
            //判断最后一个团购行是否添满，如果未满，得继续添加
            //得到最后一个团购行
            if (page > 1) {
                LinearLayout lastchild = (LinearLayout) linearLayout.getChildAt(linearLayout.getChildCount() - 1);
                int count = lastchild.getChildCount();
                if (count < cols) {
                    int gaps = cols - count;
                    for (int i = 0; i < gaps; i++) {
                        if (items_iter.hasNext()) {
                            lastchild.addView(items_iter.next());
                        }
                    }
                }
            } else {
                setLastUpdateTime();
            }
            //添加团购项目
            while (true) {
                LinearLayout linearLayout1 = new LinearLayout(context);
                linearLayout1.setLayoutParams(params);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < cols; i++) {
                    if (items_iter.hasNext()) {
                        linearLayout1.addView(items_iter.next());
                    } else {
                        break;
                    }
                }
                linearLayout.addView(linearLayout1);
                if (!items_iter.hasNext()) break;
            }

            LinearLayout location_address = (LinearLayout) aroundTuangou.getmPullScrollView()
                    .findViewWithTag(context.getResources().getString(R.string.address_location_tag));
            location_address.setVisibility(View.VISIBLE);
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

            //添加大众点评标识
            LinearLayout.LayoutParams dianping_params = new LinearLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            dianping_params.setMargins(15, 5, 15, 10);

            LinearLayout dianping_flag = (LinearLayout) aroundTuangou.getLayoutInflater().inflate(R.layout.dianping_flag, null);
            linearLayout.addView(dianping_flag);


            //加载完成消息
            ToastUtil.showShort(context, "已为您加载" + load_count + "条");
            //发送消息，更新图片
            ((AroundTuangou) context).getMyHandler().sendEmptyMessage(0);
        }


        if (progressDialog != null && progressDialog.isShowing()) {
            //关闭等待框
            progressDialog.dismiss();
        } else {
            AroundTuangouRefresh mPullScrollView = aroundTuangou.getmPullScrollView();
            mPullScrollView.onPullUpRefreshComplete();
            mPullScrollView.onPullDownRefreshComplete();
        }
    }

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        AroundTuangouRefresh mPullScrollView = aroundTuangou.getmPullScrollView();
        mPullScrollView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }

    private void openWebDialog(Context context, String title, String url) {
        Intent intent = new Intent(context, PurchaseGroupDetail.class);
        intent.putExtra("detail_url", url);
        intent.putExtra("detail_title", title);
        context.startActivity(intent);
    }
}
