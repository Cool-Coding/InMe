package cn.yang.inme.activity.around;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.asyntask.AsyncImageLoader;
import cn.yang.inme.layout.AroundShopRefresh;
import cn.yang.inme.refresh.PullToRefreshBase;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.network.NetWorkUtil;
import cn.yang.inme.view.AroundShopScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 14-6-11.
 */
public class AroundShop extends Activity implements AsyncImageLoader.ImageCallback {

    /**
     * 包含ScrollView的容器
     */
    private AroundShopRefresh mPullScrollView;

    /**
     * 控制图片的显示
     */
    private MyHandler myHandler;

    /**
     * 显示团购的ScrollView
     */
    private ScrollView mScrollView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (NetWorkUtil.isNetworkConnected(this)) {
            init();
        } else {
            checkNetwork();
        }
    }

    @Override
    public void imageLoaded(Bitmap imageDrawable, String tag) {
        //如果图片为空，则返回不处理
        if (imageDrawable == null) {
            return;
        }
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("tag", tag);
        BitmapSerialize bitmapSerialize = new BitmapSerialize(imageDrawable);
        bundle.putSerializable("bitmap", bitmapSerialize);
        msg.setData(bundle);
        msg.what = 1;
        myHandler.sendMessage(msg);
    }

    /**
     * 得高屏幕的高度与宽度
     */
    public void init() {
        mPullScrollView = new AroundShopRefresh(this);
        setContentView(mPullScrollView);
        myHandler = new MyHandler();
        if (ImageUtil.emptyImage == null) {
            ImageUtil.emptyImage = ImageUtil.getRoundRect(BitmapFactory.decodeResource(getResources(), R.drawable.default_image), Constants.AROUNDTUANGOU_IMAGE_RADIUS);
        }
        if(ImageUtil.tuanImage==null){
            ImageUtil.tuanImage=ImageUtil.getRoundRect(BitmapFactory.decodeResource(getResources(), R.drawable.tuan_icon),Constants.SHOP_ICON_RADIUS);
        }
        if(ImageUtil.quanImage==null){
            ImageUtil.quanImage=ImageUtil.getRoundRect(BitmapFactory.decodeResource(getResources(), R.drawable.quan_icon),Constants.SHOP_ICON_RADIUS);
        }

        mPullScrollView.setPullLoadEnabled(false);
        mPullScrollView.setScrollLoadEnabled(true);
        mPullScrollView.setPullRefreshEnabled(true);

        mScrollView = mPullScrollView.getRefreshableView();

        mPullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                ((AroundShopScrollView) mScrollView).refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                ((AroundShopScrollView) mScrollView).getNextPage();
            }

            @Override
            public void onFilter(int index,boolean tuan,boolean quan,boolean onLine) {
                ((AroundShopScrollView) mScrollView).filter(index,tuan,quan,onLine);
            }
        });
    }


    /**
     * 检查是否联通网络
     */
    public void checkNetwork() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setLayoutParams(params);

        TextView textView = new TextView(this);
        textView.setText(R.string.network_unavailable);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetWorkUtil.isNetworkConnected(v.getContext())) {
                    init();
                } else {
                    ToastUtil.show(v.getContext(), R.string.network_unavailable_toast);
                }
            }
        });
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Color.GRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(Color.WHITE);
                        break;
                }
                return false;
            }
        });

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        ToastUtil.show(this, R.string.network_unavailable_toast);
        linearLayout.addView(textView);
        setContentView(linearLayout);
    }

    /**
     * 得到Handler
     *
     * @return
     */
    public Handler getMyHandler() {
        return myHandler;
    }

    /**
     * 得到PullScrollView
     *
     * @return
     */
    public AroundShopRefresh getmPullScrollView() {
        return mPullScrollView;
    }

    /**
     * 得到Scrollview
     *
     * @return
     */
    public ScrollView getmScrollView() {
        return mScrollView;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean result = ApplicationUtil.isShouldExit(this);
            if (result) onBackPressed();
            else return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理团购图片异步显示的Handler内部类
     */
    class MyHandler extends Handler {
        private HashMap<String, Bitmap> emptyurl = new HashMap<String, Bitmap>();

        @Override
        public void handleMessage(Message msg) {
            ImageView imageViewByTag;
            LinearLayout items = (LinearLayout) mScrollView.getChildAt(0);
            switch (msg.what) {
                case 0:
                    //将未显示的图片再尝试显示
                    ArrayList<String> urls = new ArrayList<String>();
                    for (Map.Entry<String, Bitmap> entry : emptyurl.entrySet()) {
                        String tag = entry.getKey();
                        imageViewByTag = (ImageView) items.findViewWithTag(tag);
                        if (imageViewByTag != null) {
                            imageViewByTag.setImageBitmap(entry.getValue());
                            urls.add(tag);
                        }
                    }
                    Iterator<String> iterator = urls.iterator();
                    while (iterator.hasNext()) {
                        emptyurl.remove(iterator.next());
                    }
                    break;
                case 1:
                    Bundle bundle = msg.getData();
                    String tag = bundle.getString("tag");
                    Bitmap bitmap = ((BitmapSerialize) bundle.get("bitmap")).getBitmap();
                    imageViewByTag = (ImageView) items.findViewWithTag(tag);
                    if (imageViewByTag != null) {
                        imageViewByTag.setImageBitmap(bitmap);
                    } else {
                        emptyurl.put(tag, bitmap);
                    }
                    break;
            }
        }
    }
}