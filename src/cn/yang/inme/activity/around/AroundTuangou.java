package cn.yang.inme.activity.around;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.asyntask.AsyncImageLoader;
import cn.yang.inme.layout.AroundTuangouRefresh;
import cn.yang.inme.refresh.PullToRefreshBase;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.network.NetWorkUtil;
import cn.yang.inme.view.AroundTuangouScrollView;

import java.util.*;

/**
 * Created by yang on 2014/6/4.
 */
public class AroundTuangou extends Activity implements AsyncImageLoader.ImageCallback {

    /**
     * 控制图片的显示
     */
    private MyHandler myHandler;
    /**
     * 一行可以显示的团购数量
     */
    private int tuangou_cols;
    /**
     * 显示团购的ScrollView
     */
    private ScrollView mScrollView;
    /**
     * 包含ScrollView的容器
     */
    private AroundTuangouRefresh mPullScrollView;

    /**
     * 搜索按钮
     */
    private SearchView searchView;

    /**
     * 进度框
     */
    private ProgressDialog progressDialog;

    /**
     * Activity 入口
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (NetWorkUtil.isNetworkConnected(this)) {
            init();
        } else {
            checkNetwork();
        }

/*        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/

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
     * 得高屏幕的高度与宽度
     */
    public void init() {
        mPullScrollView = new AroundTuangouRefresh(this);
        setContentView(mPullScrollView);
        myHandler = new MyHandler();
        if (ImageUtil.emptyImage == null) {
            ImageUtil.emptyImage = ImageUtil.getRoundRect(BitmapFactory.decodeResource(getResources(), R.drawable.default_image), Constants.AROUNDTUANGOU_IMAGE_RADIUS);
        }

        //得到屏幕的宽度与高度
        Display display = this.getWindowManager().getDefaultDisplay();
        //得到列数
        tuangou_cols = Math.round((float) (display.getWidth()) / Constants.AROUNDTUANGOU_IMAGE);
        Constants.AROUNDTUANGOU_IMAGE = display.getWidth() / tuangou_cols;


        mPullScrollView.setPullLoadEnabled(true);
        mPullScrollView.setScrollLoadEnabled(false);
        mPullScrollView.setPullRefreshEnabled(true);
        mScrollView = mPullScrollView.getRefreshableView();

        mPullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                ((AroundTuangouScrollView) mScrollView).refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                ((AroundTuangouScrollView) mScrollView).getNextPage();
            }

            @Override
            public void onFilter(int index,boolean tuan,boolean quan,boolean onLine) {
                ((AroundTuangouScrollView) mScrollView).filter(index);
            }
        });
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

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aroundtuangou_menu, menu);
        //搜索视窗，因为showAsAction="ifRoom"
        searchView = (SearchView) menu.findItem(R.id.menu_search)
                .getActionView();

        AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null));
        search_text.setTextColor(Color.BLUE);
        search_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.search_size));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
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

        return true;
    }*/

/*    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        System.out.println("tuan-a");
        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    /**
     * 得到Handler
     *
     * @return
     */
    public Handler getMyHandler() {
        return myHandler;
    }

    /**
     * 得到列数
     *
     * @return
     */
    public int getTuangou_cols() {
        return tuangou_cols;
    }

    /**
     * 得到PullScrollView
     *
     * @return
     */
    public AroundTuangouRefresh getmPullScrollView() {
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

    public void setProgressdialog(ProgressDialog dialog) {
        this.progressDialog = dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //以免Activity因关闭，progressDialog还没关闭而出现错误
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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
                    Set<Map.Entry<String, Bitmap>> entries = emptyurl.entrySet();
                    for (Map.Entry<String, Bitmap> entry : entries) {
                        String tag = entry.getKey();
                        imageViewByTag = (ImageView) items.findViewWithTag(tag);
                        if (imageViewByTag != null) {
                            setImageAnimate(imageViewByTag, entry.getValue());
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
                        setImageAnimate(imageViewByTag, bitmap);
                    } else {
                        emptyurl.put(tag, bitmap);
                    }
                    break;
            }
        }

        /**
         * 图片淡出淡入
         *
         * @param imageView
         * @param bitmap
         */
        public void setImageAnimate(ImageView imageView, Bitmap bitmap) {
//            imageView.clearAnimation();
            imageView.setImageBitmap(bitmap);
            imageView.setAlpha(0f);
            imageView.setVisibility(View.VISIBLE);
//            imageView.setAnimation(AnimationUtils.loadAnimation(AroundTuangou.this,R.anim.fade_in));

            imageView.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setListener(null);
        }
    }
}