package cn.yang.inme.utils.border;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;

/**
 * Created by yang on 2014/6/13.
 */
public class TopBorderTextView extends BorderTextView {
    private int width = 5;
    //private int color=getResources().getColor(R.color.border_basecolor);
    private int color;

    public TopBorderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int color2 = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        color = Color.argb(153, Color.red(color2), Color.green(color2), Color.blue(color2));

        setBottomBorder(Color.parseColor("#EBEBEB"), 0);
        setTopBorder(color, width);
    }

    public void setWidth(int width) {
        this.width = width;
        setTopBorder(color, width);
        setLeftBorder(color, width);
    }

    public void setColor(int color) {
        this.color = color;
        setTopBorder(color, width);
        setLeftBorder(color, width);
    }

}
