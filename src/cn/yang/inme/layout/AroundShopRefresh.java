package cn.yang.inme.layout;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import cn.yang.inme.R;
import cn.yang.inme.refresh.FooterLoadingLayout;
import cn.yang.inme.refresh.ILoadingLayout;
import cn.yang.inme.refresh.LoadingLayout;
import cn.yang.inme.refresh.PullToRefreshBase;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.view.AroundShopScrollView;

/**
 * 封装了ScrollView的下拉刷新
 *
 * @author Li Hong
 * @since 2013-8-22
 */
public class AroundShopRefresh extends PullToRefreshBase<ScrollView> {

    private ScrollView mscrollView;

    @Override
    public String getClassName() {
        return "AroundShopRefresh";
    }

    /**
     * 用于滑到底部自动加载的Footer
     */
    private LoadingLayout mLoadMoreFooterLayout;

    /**
     * 底部自动加载Footer
     */
    private LinearLayout footLayout;

    /**
     * 构造方法
     *
     * @param context context
     */
    public AroundShopRefresh(Context context) {
        this(context, null);
    }

    @Override
    public boolean isPullRefreshing() {
        return super.isPullRefreshing();
    }

    /**
     * 构造方法
     *
     * @param context context
     * @param attrs   attrs
     */
    public AroundShopRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法
     *
     * @param context  context
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public AroundShopRefresh(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @see
     */
    @Override
    protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {
        ScrollView scrollView = new AroundShopScrollView(context);
        scrollView.setTag("aroundshopview");
        //设置背景图片
        ApplicationUtil.setThemeBackground(scrollView);

        LinearLayout linearLayout = new LinearLayout(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        footLayout = new LinearLayout(context);
        footLayout.setGravity(Gravity.CENTER);
        footLayout.setTag(context.getResources().getString(R.string.aroundshop_footer_tag));

        linearLayout.addView(footLayout);

        scrollView.addView(linearLayout);

        mscrollView = scrollView;
        return scrollView;
    }

    @Override
    public void resetPullUpState() {
        mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
        super.resetPullUpState();
    }

    /**
     * 设置是否有更多数据的标志
     *
     * @param hasMoreData true表示还有更多的数据，false表示没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {

            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
            }

            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
            }
        }
    }
    /**
     * @see
     */
    @Override
    protected boolean isReadyForPullDown() {
        return mRefreshableView.getScrollY() == 0;
    }

    /**
     * @see
     */
    @Override
    protected boolean isReadyForPullUp() {
        View scrollViewChild = mRefreshableView.getChildAt(0);
        if (null != scrollViewChild) {
            boolean result=mRefreshableView.getScrollY() >= (scrollViewChild.getHeight() - getHeight());
            if (result)footLayout.setBackgroundColor(Color.WHITE);
            return result;
        }
        return false;
    }

    @Override
    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        super.setScrollLoadEnabled(scrollLoadEnabled);

        if (scrollLoadEnabled) {
            // 设置Footer
            if (null == mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
            }

            if (null == mLoadMoreFooterLayout.getParent()) {
                footLayout.addView(mLoadMoreFooterLayout);
            }
            mLoadMoreFooterLayout.show(true);
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.show(false);
            }
        }
    }

    @Override
    public LoadingLayout getFooterLoadingLayout() {
        if (isScrollLoadEnabled()) {
            return mLoadMoreFooterLayout;
        }

        return super.getFooterLoadingLayout();
    }

    /**
     * 表示是否还有更多数据
     *
     * @return true表示还有更多数据
     */
    private boolean hasMoreData() {
        if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == ILoadingLayout.State.NO_MORE_DATA)) {
            return false;
        }

        return true;
    }

    /**
     * 准备刷新
     */
    public void perpareToRefreshing() {
        if (isScrollLoadEnabled() && hasMoreData()) {
            if (isReadyForPullUp()) {
                startLoading();
            }
        }
    }

    @Override
    protected void startLoading() {
        super.startLoading();
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(ILoadingLayout.State.REFRESHING);
        }
    }

    @Override
    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();

        if (null != mLoadMoreFooterLayout) {
            footLayout.setBackgroundColor(Color.TRANSPARENT);
            mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
        }
    }
}
