package cn.yang.inme.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by Administrator on 14-7-9.
 */
public class SureButtonView extends View {

    public int color;
    private int width;
    private int alpha = 255;

    private boolean check = false;
    private boolean wronged = false;

    public SureButtonView(Context context, int color, int width, int alpha) {
        super(context);
        this.color = color;
        this.width = width;
        this.alpha = alpha;
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
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        // 绘制一个圆
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        canvas.drawCircle(x, y, x, paint);

        if (check) {
            //绘对勾
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(alpha);
            Path path = new Path();
            path.moveTo(x / 2, y);
            path.lineTo(x, x / 2 + y);
            path.lineTo(3 * x / 2, y - x / 2);
            canvas.drawPath(path, paint);
        }

        if (wronged) {
            //绘X
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setAlpha(alpha);
            paint.setStyle(Paint.Style.STROKE);
            Path path = new Path();
            path.moveTo(x / 2, y - x / 2);
            path.lineTo((float) 1.5 * x, x / 2 + y);
            path.moveTo((float) 1.5 * x, y - x / 2);
            path.lineTo(x / 2, y + x / 2);
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

    /**
     * 打X
     */
    public void checkWrong() {
        wronged = true;
        this.invalidate();
    }
}
