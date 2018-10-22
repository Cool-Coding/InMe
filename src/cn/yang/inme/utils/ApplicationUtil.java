package cn.yang.inme.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import cn.yang.inme.R;

/**
 * Created by Administrator on 14-9-26.
 */
public class ApplicationUtil {
    /**
     * 记录时间间隔变量
     */
    private static long intervalTime = 0;
    private static BitmapDrawable backgorund;

    /**
     * 加载背景图片
     */
    public static void loadBackground() {
        String backgroundstr = PropertiesUtil.instance().read(Constants.SET_THEME_BACKGROUND);
        if (backgroundstr == null || "".equals(backgroundstr)) {
            backgorund = null;
        } else {
            Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(backgroundstr);
            backgorund = new BitmapDrawable(bitmap);
        }
    }

    /**
     * 设置背景图片
     *
     * @param view
     */
    public static void setThemeBackground(View view) {
        if (backgorund == null) {
            view.setBackgroundResource(R.drawable.tuangou_background_border);
        } else {
            view.setBackgroundDrawable(backgorund);
            view.getBackground().setAlpha(200);
        }
    }

    /**
     * 设置ActionBar
     *
     * @param activity
     */
    public static void setActionBarColor(Activity activity) {
        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(1);
        activity.getActionBar().setBackgroundDrawable(gradientDrawable);
    }


    /**
     * 判断是否应该退出
     *
     * @param intervalTime
     */
    public static void setIntervalTime(long intervalTime) {
        intervalTime = intervalTime;
    }

    public static boolean isShouldExit(Context context) {
        if (System.currentTimeMillis() - intervalTime <= 3000) {
            return true;
        } else {
            intervalTime = System.currentTimeMillis();
            ToastUtil.showShort(context, "再按一次就退出了");
            return false;
        }
    }
}
