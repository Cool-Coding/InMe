package cn.yang.inme.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundTuangou;
import cn.yang.inme.asyntask.AsynUpdateAddress;
import cn.yang.inme.asyntask.AsyncImageLoader;
import cn.yang.inme.asyntask.GetAroundTuangou;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.ImageUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 14-6-5.
 */
public class AroundTuangouScrollView extends ScrollView {

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
     * 日志
     */
    private Logger log = Logger.getLogger(AroundTuangouScrollView.class);

    /**
     * 线程池
     */
    private ExecutorService transThread = Executors.newSingleThreadExecutor();
    private Future transPending;

    /**
     * 线程池中的线程
     */
    private ChangeImage changeImage = new ChangeImage();

    /**
     * @param context
     */

    public AroundTuangouScrollView(Context context) {
        super(context);
    }

    public AroundTuangouScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 在Handler中进行图片可见性检查的判断，以及加载更多图片的操作。
     */
    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            AroundTuangouScrollView myScrollView = (AroundTuangouScrollView) msg.obj;
            int scrollY = myScrollView.getScrollY();
            // 如果当前的滚动位置和上次相同，表示已停止滚动
            if (scrollY == lastScrollY) {
                if (transPending != null) transPending.cancel(true);
                changeImage.setScrolly(scrollY);
                transPending = transThread.submit(changeImage);
//                checkVisiblity(scrollY);
            } else {
                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                // 5毫秒后再次对滚动位置进行判断
                handler.sendMessageDelayed(message, 5);
            }
        }

        ;

    };

    private class ChangeImage implements Runnable {
        private int t;

        public void setScrolly(int t) {
            this.t = t;
        }

        public void run() {
            LinearLayout linearLayout = (LinearLayout) AroundTuangouScrollView.this.getChildAt(0);
            int count = linearLayout.getChildCount() - 1;//不包括点评标识
            for (int i = 1; i < count; i++) {
                LinearLayout item_line = (LinearLayout) linearLayout.getChildAt(i);
                if (item_line.getTop() + item_line.getHeight() >= t && item_line.getTop() < t + getHeight()) {
                    int line_itemcount = item_line.getChildCount();
                    for (int j = 0; j < line_itemcount; j++) {
                        ImageView image = (ImageView) ((LinearLayout) item_line.getChildAt(j)).getChildAt(0);
                        if ("0".equals(image.getTag(R.string.image_status))) {
                            image.setTag(R.string.image_status, "1");
                            String tag = (String) image.getTag();
                            int index = tag.lastIndexOf("*");
                            String imageUrl;
                            if (index != -1) {
                                imageUrl = tag.substring(0, index);
                            } else {
                                imageUrl = tag;
                            }
                            Bitmap bitmap = AsyncImageLoader.loadDrawable(imageUrl, (AroundTuangou) context, tag, Constants.AROUNDTUANGOU_IMAGE);
                            if (bitmap != null) {
                                ((AroundTuangou) context).imageLoaded(bitmap, (String) image.getTag());
                            }
                        }
                    }
                } else {
                    int line_itemcount = item_line.getChildCount();
                    for (int j = 0; j < line_itemcount; j++) {
                        ImageView image = (ImageView) ((LinearLayout) item_line.getChildAt(j)).getChildAt(0);
                        if ("1".equals(image.getTag(R.string.image_status))) {
                            image.setTag(R.string.image_status, "0");
                            ((AroundTuangou) context).imageLoaded(ImageUtil.emptyImage, (String) image.getTag());
                        }
                    }
                }
            }
        }
    };

    /**
     * 图片淡出淡入
     *
     * @param imageView
     */
    public void setImageAnimate(ImageView imageView, Bitmap bitmap) {
        imageView.clearAnimation();
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        imageView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
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
            paramMap.put("city", null);
            paramMap.put("latitude", null);
            paramMap.put("longitude", null);
            paramMap.put("sort", "7");//按距离排序
//清除图片Tags集合
            GetAroundTuangou.clearImageTages();
//加载团购
            getNextPage();
            once = false;
        }
    }

    /**
     * 监听用户的触屏事件，如果用户手指离开屏幕则开始进行滚动检测。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Message message = new Message();
            message.obj = this;
            handler.sendMessageDelayed(message, 5);
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    /**
     * 刷新
     */
    public void refresh() {
        search(null);
    }

    /**
     * 从大众点评网上获取团购信息
     */
    public void getNextPage() {
        try {
            page++;
            paramMap.remove("page");
            paramMap.put("page", page + "");

            new GetAroundTuangou(this.context, paramMap).execute(Constants.AROUNDTUANGOU_URL);
        } catch (Exception e) {
//                        Log.e("error", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 过滤
     * @param index
     */
    public void filter(int index){
        String sort=String.valueOf(index);
        paramMap.put("sort",sort);
        refresh();
    }
    /**
     * 用户搜索
     *
     * @param keyword 搜索参数
     */
    public void search(String keyword) {
        page = 1;
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
//        child.removeAllViews();
        //child.removeView();
        View view=child.findViewWithTag(getResources().getString(R.string.search_history_tag));
        if(view==null || ( keyword!=null && !"".equals(keyword))){
            child.removeView(view);
            child.removeViews(1, child.getChildCount() - 1);
        }else{
            child.removeViews(2, child.getChildCount() - 2);
        }

        //重置FootLayout
        ((AroundTuangou) context).getmPullScrollView().resetPullUpState();

        try {
            new GetAroundTuangou(this.context, paramMap).execute(Constants.AROUNDTUANGOU_URL);
        } catch (Exception e) {
//          Log.e("error", e.toString());
            e.printStackTrace();
        }
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
                ((LinearLayout) AroundTuangouScrollView.this.getChildAt(0)).removeView(linearLayout);
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
        linearLayout.setPadding(0, 10, 0, 10);
        linearLayout.setBackgroundResource(R.drawable.tuangou_background_border);
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
