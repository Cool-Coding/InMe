package cn.yang.inme.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import cn.yang.inme.R;
import cn.yang.inme.refresh.ILoadingLayout;
import cn.yang.inme.refresh.LoadingLayout;
import cn.yang.inme.refresh.PullToRefreshBase;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.view.AroundTuangouScrollView;

/**
 * 封装了ScrollView的下拉刷新
 *
 * @author Li Hong
 * @since 2013-8-22
 */
public class AroundTuangouRefresh extends PullToRefreshBase<ScrollView> {

    private ScrollView mscrollView;

    @Override
    public String getClassName() {
        return "AroundTuangouRefresh";
    }

    /**
     * 构造方法
     *
     * @param context context
     */
    public AroundTuangouRefresh(Context context) {
        this(context, null);
    }

    /**
     * 构造方法
     *
     * @param context context
     * @param attrs   attrs
     */
    public AroundTuangouRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法
     *
     * @param context  context
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public AroundTuangouRefresh(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @see
     */
    @Override
    protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {

        ScrollView scrollView = new AroundTuangouScrollView(context);
        //设置背景图片
        ApplicationUtil.setThemeBackground(scrollView);

        scrollView.setTag("aroundtuangouview");//以便MainActivity里OptinMenu调用Scrollview
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(linearLayout);

        mscrollView = scrollView;
        return scrollView;
    }

    /**
     * 设置是否有更多数据的标志
     *
     * @param hasMoreData true表示还有更多的数据，false表示没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {
            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
            }
        }
    }

    @Override
    public boolean isPullRefreshing() {
        return super.isPullRefreshing();
    }

    /**
     * 500ms平滑滚动时间
     *
     * @return
     */
    @Override
    protected long getSmoothScrollDuration() {
        return 500;
    }

    /**
     * @see
     */
    @Override
    protected boolean isReadyForPullDown() {
        return mRefreshableView.getScrollY() == 0;
        //return false;
    }

    /**
     * @see
     */
    @Override
    protected boolean isReadyForPullUp() {
        View scrollViewChild = mRefreshableView.getChildAt(0);
        if (null != scrollViewChild) {
            return mRefreshableView.getScrollY() >= (scrollViewChild.getHeight() - getHeight());
        }

        return false;
    }
}
