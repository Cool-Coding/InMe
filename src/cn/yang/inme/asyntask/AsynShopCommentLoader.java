package cn.yang.inme.asyntask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShopDetail;
import cn.yang.inme.activity.around.PurchaseGroupDetail;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.InMeTextProgressDialog;
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
public class AsynShopCommentLoader extends AsyncTask<Integer, Void, ArrayList<LinearLayout>> {

    private AroundShopDetail aroundShop;
    private LinearLayout comments;
    private float size;
    private int content_color;
    private String title;

    public AsynShopCommentLoader(Context context,String title) {
        this.aroundShop = (AroundShopDetail) context;
        this.title=title;
    }

    @Override
    protected void onPreExecute() {
        comments = (LinearLayout) aroundShop.findViewById(R.id.comment_content);

        size = aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_basesize) - 1;
        content_color = aroundShop.getResources().getColor(R.color.around_shop_content);
        TextView tip = InMeTextProgressDialog.instance(aroundShop, new int[]{
                R.string.comment_loading_tip1, R.string.comment_loading_tip2, R.string.comment_loading_tip3
        });
        tip.setTextSize(size);
        tip.setTextColor(content_color);
        comments.addView(tip);
    }

    @Override
    protected ArrayList<LinearLayout> doInBackground(Integer... params) {
        Integer id = params[0];

        ArrayList<LinearLayout> comment_list = new ArrayList<LinearLayout>();

        String url = "http://api.dianping.com/v1/review/get_recent_reviews";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("business_id", id.toString());
        paramMap.put("platform", "2");

        String requestResult = DianpingHttpGet.requestApi(url, Constants.DIANPING_KEY, Constants.DIANPING_SECRET, paramMap);

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap>() {
        }.getType();
        HashMap results = gson.fromJson(requestResult, type);
        if (results == null) return null;//网络连接断开

        String status = (String) results.get("status");
        if ("ERROR".equals(status)) {
            return null;
        } else {
            ArrayList items = (ArrayList) results.get("reviews");
            if (items == null || items.size() == 0) {
                return null;
            } else {
                LinearLayout.LayoutParams comment_item_params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);

                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);//星级评分布局

                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    LinkedTreeMap comment_map = (LinkedTreeMap) iterator.next();

                    LinearLayout comment = new LinearLayout(aroundShop);
                    comment.setOrientation(LinearLayout.VERTICAL);
                    comment.setLayoutParams(comment_item_params);
                    comment.setBackgroundResource(R.drawable.shop_detail_item_border);
                    comment.setPadding(0, 15, 0, 15);

                    LinearLayout title = new LinearLayout(aroundShop);
                    title.setOrientation(LinearLayout.HORIZONTAL);
                    title.setGravity(Gravity.CENTER);
                    title.setPadding(0, 0, 0, 15);

                    TextView nickname = new TextView(aroundShop);
                    nickname.setText(comment_map.get("user_nickname").toString());
                    nickname.setPadding(
                            Math.round(aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_margin)) - 7
                            , 0
                            , 15
                            , 0);
                    nickname.setTextSize(size);

                    TextView create_time = new TextView(aroundShop);
                    String date = comment_map.get("created_time").toString();
                    date = date.substring(0, date.indexOf(" "));
                    create_time.setText(date);
                    create_time.setTextSize(size - 1);

                    LinearLayout rating_layout = new LinearLayout(aroundShop);
                    rating_layout.setLayoutParams(params2);
                    rating_layout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    //星星
                    RatingBar rating = (RatingBar) aroundShop.getLayoutInflater().inflate(R.layout.aroundshop_star, null);
                    rating.setRating(Float.valueOf(comment_map.get("review_rating").toString()).floatValue());
                    rating_layout.addView(rating);

                    title.addView(nickname);
                    title.addView(create_time);
                    title.addView(rating_layout);

                    //评价内容
                    TextView content = new TextView(aroundShop);
                    content.setText(comment_map.get("text_excerpt").toString());
                    content.setTextSize(size);
                    content.setTextColor(content_color);
                    content.setPadding(
                            Math.round(aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_margin))
                            , 0
                            , 0
                            , 10);

                    comment.addView(title);
                    comment.addView(content);

                    comment_list.add(comment);
                }

            }

            //更多点评
            LinkedTreeMap moreComment_map = (LinkedTreeMap) results.get("additional_info");
            final String moreCommentStr = ((String) moreComment_map.get("more_reviews_url")).trim();
            if (moreCommentStr == null || "".equals(moreCommentStr)) {
                return comment_list;
            }

            LinearLayout.LayoutParams morecomment_params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            morecomment_params.topMargin = 15;

            LinearLayout moreComment_layout = new LinearLayout(aroundShop);
            moreComment_layout.setOrientation(LinearLayout.HORIZONTAL);
            moreComment_layout.setLayoutParams(morecomment_params);

            TextView more = new TextView(aroundShop);
            more.setText("更多:");
            more.setTextSize(size - 1);
            more.setPadding(
                    Math.round(aroundShop.getResources().getDimension(R.dimen.aroundshop_detail_margin))
                    , 0
                    , 0
                    , 10);

            TextView moreComment = new TextView(aroundShop);
            moreComment.setText("大众点评");
            moreComment.setTextColor(Color.BLUE);
            moreComment.setTextSize(size - 1);
            moreComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PurchaseGroupDetail.class);
                    intent.putExtra("detail_url", moreCommentStr);
                    intent.putExtra("detail_title",title+"全部点评");
                    v.getContext().startActivity(intent);
                }
            });

            moreComment_layout.addView(more);
            moreComment_layout.addView(moreComment);

            comment_list.add(moreComment_layout);


            return comment_list;
        }

    }

    @Override
    protected void onPostExecute(ArrayList<LinearLayout> linearLayouts) {
        if (linearLayouts == null) {
            comments.removeViewAt(0);
            TextView text = new TextView(aroundShop);
            text.setText(aroundShop.getResources().getString(R.string.comment_loading_result));
            text.setTextSize(size);
            comments.addView(text);
        } else {
            comments.removeAllViews();
            Iterator<LinearLayout> iterator = linearLayouts.iterator();

            while (iterator.hasNext()) {
                comments.addView(iterator.next());
            }

        }
    }
}
