package cn.yang.inme.utils.border;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;

/**
 * Created by yang on 2014/6/13.
 */
public class TopBorderLinearLayout extends BorderLinearLayout {
    private int width = 5;
    //private int color=getResources().getColor(R.color.border_basecolor);
    private int color;

    public TopBorderLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        int color = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        setBottomBorder(Color.parseColor("#EBEBEB"), 0);
        setTopBorder(color, width);
    }

    public void setWidth(int width) {
        this.width = width;
        setTopBorder(color, width);
    }

    public void setColor(int color) {
        this.color = color;
        setTopBorder(color, width);
    }

    public void drawDashLine() {
        if (paint == null) paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setAntiAlias(true);
        PathEffect effect = new DashPathEffect(new float[]{3,3}, 1);
        //paint.setAlpha(60);
        paint.setPathEffect(effect);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
}
