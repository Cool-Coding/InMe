package cn.yang.inme.utils.colorpallet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import cn.yang.inme.R;

/**
 * Created by Administrator on 14-7-9.
 */
public class ColorpalletView extends View {

    public int color = getResources().getColor(R.color.border_basecolor);
    ;
    private boolean check = false;

    public ColorpalletView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorpalletView(Context context, int color) {
        super(context);
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 首先定义一个paint
        Paint paint = new Paint();

        // 绘制矩形区域-实心矩形
        // 设置颜色
        paint.setColor(color);
        // 设置样式-填充
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        // 绘制一个圆
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        canvas.drawCircle(x, y, x, paint);

        if (check) {
            //绘对勾
            paint.setStrokeWidth(8);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            Path path = new Path();
            path.moveTo(x / 2, y);
            path.lineTo(x, x / 2 + y);
            path.lineTo(3 * x / 2, y - x / 2);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 打勾
     */
    public void checkRight() {
        check = true;
        this.invalidate();
    }
}
